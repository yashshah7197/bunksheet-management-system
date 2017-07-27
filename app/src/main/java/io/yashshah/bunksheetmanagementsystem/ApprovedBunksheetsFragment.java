package io.yashshah.bunksheetmanagementsystem;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ApprovedBunksheetsFragment extends Fragment {

    private View mRootView;

    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;

    private LinearLayoutManager mLinearLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;

    private User mUser;

    public ApprovedBunksheetsFragment() {
        // Required empty public constructor
    }

    public static ApprovedBunksheetsFragment newInstance() {

        Bundle args = new Bundle();

        ApprovedBunksheetsFragment fragment = new ApprovedBunksheetsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_approved_bunksheets, container, false);

        setupFirebase();
        setupRecyclerView();
        getUser();

        return mRootView;
    }

    private void setupFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mDatabaseReference = mFirebaseDatabase.getReference().child("Bunksheets");
    }

    private void setupRecyclerView() {
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(false);

        mDividerItemDecoration = new DividerItemDecoration(getActivity(),
                mLinearLayoutManager.getOrientation());

        mRecyclerView =
                (RecyclerView) mRootView.findViewById(R.id.recyclerView_approved_bunksheets);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mRecyclerView.addItemDecoration(mDividerItemDecoration);
    }

    private void attachRecyclerViewAdapter() {
        Query query = null;
        if (mUser.getPrivilegeLevel() == User.PRIVILEGE_HEAD) {
            query = mDatabaseReference
                    .orderByChild("approvalLevel")
                    .startAt(User.PRIVILEGE_HEAD)
                    .endAt(User.PRIVILEGE_HOD);
        } else if (mUser.getPrivilegeLevel() == User.PRIVILEGE_TEACHER) {
            query = mDatabaseReference
                    .orderByChild("approvalLevel")
                    .startAt(User.PRIVILEGE_TEACHER)
                    .endAt(User.PRIVILEGE_HOD);
        } else if (mUser.getPrivilegeLevel() == User.PRIVILEGE_HOD) {
            query = mDatabaseReference
                    .orderByChild("approvalLevel")
                    .equalTo(User.PRIVILEGE_HOD);
        }

        if (mFirebaseRecyclerAdapter == null) {
            mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Bunksheet, BunksheetViewHolder>(
                    Bunksheet.class,
                    R.layout.bunksheet_list_item,
                    BunksheetViewHolder.class,
                    query) {
                @Override
                protected void populateViewHolder(BunksheetViewHolder holder, Bunksheet bunksheet,
                                                  int position) {
                    holder.bind(bunksheet, mUser);
                }
            };
        }
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
    }

    private void getUser() {
        DatabaseReference userDatabaseReference = mFirebaseDatabase.getReference()
                .child("Users")
                .child(mCurrentUser.getUid());

        ValueEventListener singleValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.getValue(User.class);
                attachRecyclerViewAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userDatabaseReference.addValueEventListener(singleValueEventListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        getUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFirebaseRecyclerAdapter != null) {
            mFirebaseRecyclerAdapter.cleanup();
        }
    }

}