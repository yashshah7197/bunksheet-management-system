package io.yashshah.bunksheetmanagementsystem;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
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


public class BunksheetsFragment extends Fragment {

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

    public BunksheetsFragment() {
        // Required empty public constructor
    }

    public static BunksheetsFragment newInstance() {

        Bundle args = new Bundle();

        BunksheetsFragment fragment = new BunksheetsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_bunksheets, container, false);
        FloatingActionButton fab =
                (FloatingActionButton) mRootView.findViewById(R.id.fab_new_bunksheet);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), RequestBunksheetActivity.class));
            }
        });

        setupFirebase();
        setupRecyclerView();

        return mRootView;
    }

    private boolean isProfileFilled() {
        if (mUser.getName().equals("") || mUser.getPhoneNumber().equals("")
                || mUser.getYear().equals("") || mUser.getDivision().equals("")
                || mUser.getClassTeacher().equals("") || mUser.getTeacherGuardian().equals("")
                || mUser.getRollNumber().equals("")) {
            return false;
        }
        return true;
    }

    private void showFillProfileAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.profile_incomplete_title));
        builder.setMessage(getString(R.string.profile_incomplete_message));
        builder.setPositiveButton(getString(R.string.go_to_profile),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Fragment fragment = ProfileFragment.newInstance();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager
                                .beginTransaction()
                                .replace(R.id.frameLayout_fragments, fragment)
                                .commit();
                        getActivity().setTitle(R.string.profile);
                        ((MainActivity) getActivity()).setDrawerCheckItem(R.id.navigation_profile);
                    }
                });
        builder.setCancelable(false);
        builder.show();
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

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView_bunksheets);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mRecyclerView.addItemDecoration(mDividerItemDecoration);
    }

    private void attachRecyclerViewAdapter() {
        if (mFirebaseRecyclerAdapter != null) {
            mFirebaseRecyclerAdapter.notifyDataSetChanged();
        }
        Query query = mDatabaseReference.orderByChild("userUID").equalTo(mCurrentUser.getUid());
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
                if (isProfileFilled()) {
                    attachRecyclerViewAdapter();
                } else {
                    showFillProfileAlertDialog();
                }
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