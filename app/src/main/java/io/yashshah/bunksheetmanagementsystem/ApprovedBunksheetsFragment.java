package io.yashshah.bunksheetmanagementsystem;


import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private AlertDialog mAlertDialog;

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
        builder.setMessage(getString(R.string.profile_incomplete_message_approveBunksheets));
        builder.setPositiveButton(getString(R.string.go_to_profile),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).selectDrawerItem(R.id.navigation_profile);
                    }
                });
        builder.setCancelable(false);

        mAlertDialog = builder.create();
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

    private boolean isConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void showNotConnectedAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.device_offline));
        builder.setMessage(getString(R.string.approvedBunksheets_offline_message));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(false);

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }

        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isConnected()) {
            showNotConnectedAlertDialog();
        }
        getUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        if (mFirebaseRecyclerAdapter != null) {
            mFirebaseRecyclerAdapter.cleanup();
        }
    }

}
