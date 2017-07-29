package io.yashshah.bunksheetmanagementsystem;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class ApproveBunksheetsFragment extends Fragment {

    AlertDialog mAlertDialog;
    private View mRootView;
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;
    private ItemTouchHelper mItemTouchHelper;
    private ItemTouchHelper.SimpleCallback mItemTouchHelperCallback;
    private Paint mPaint;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;
    private User mUser;

    public ApproveBunksheetsFragment() {
        // Required empty public constructor
    }

    public static ApproveBunksheetsFragment newInstance() {

        Bundle args = new Bundle();

        ApproveBunksheetsFragment fragment = new ApproveBunksheetsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_approve_bunksheets, container, false);

        setupFirebase();
        setupRecyclerView();
        setupRecyclerViewSwipeGestures();

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
        mAlertDialog.show();
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

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView_approve_bunksheets);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mRecyclerView.addItemDecoration(mDividerItemDecoration);
    }

    private void setupRecyclerViewSwipeGestures() {
        mPaint = new Paint();

        if (mItemTouchHelperCallback == null) {
            mItemTouchHelperCallback =
                    new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP, ItemTouchHelper.LEFT
                            | ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(RecyclerView recyclerView,
                                              RecyclerView.ViewHolder viewHolder,
                                              RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                            int position = viewHolder.getAdapterPosition();
                            String bunksheetKey = mFirebaseRecyclerAdapter
                                    .getRef(position)
                                    .getKey();

                            if (direction == ItemTouchHelper.LEFT) {
                                changeBunksheetApprovalStatus(bunksheetKey, false);
                                mFirebaseRecyclerAdapter.notifyItemRemoved(position);
                                mFirebaseRecyclerAdapter.notifyDataSetChanged();
                            } else if (direction == ItemTouchHelper.RIGHT) {
                                mFirebaseRecyclerAdapter.notifyItemRemoved(position);
                                mFirebaseRecyclerAdapter.notifyDataSetChanged();
                                changeBunksheetApprovalStatus(bunksheetKey, true);
                            }
                        }

                        @Override
                        public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                                RecyclerView.ViewHolder viewHolder, float dX,
                                                float dY, int actionState,
                                                boolean isCurrentlyActive) {
                            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                                View itemView = viewHolder.itemView;

                                if (dX > 0) {
                                    mPaint.setColor(ContextCompat.getColor(getActivity(),
                                            R.color.colorGreen));
                                    c.drawRect((float) itemView.getLeft(), (float)
                                                    itemView.getTop(), dX,
                                            (float) itemView.getBottom(), mPaint);
                                } else {
                                    mPaint.setColor(ContextCompat.getColor(getActivity(),
                                            R.color.colorRed));
                                    c.drawRect((float) itemView.getRight() + dX,
                                            (float) itemView.getTop(), (float) itemView.getRight(),
                                            (float) itemView.getBottom(), mPaint);
                                }
                            }

                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState,
                                    isCurrentlyActive);
                        }
                    };

            mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    private void attachRecyclerViewAdapter() {
        Query query = mDatabaseReference
                .orderByChild("approvalLevel")
                .equalTo(mUser.getPrivilegeLevel() - 1);

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

    private void changeBunksheetApprovalStatus(String bunksheetKey, final boolean approved) {
        final DatabaseReference databaseReference =
                mFirebaseDatabase.getReference().child("Bunksheets").child(bunksheetKey);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Bunksheet bunksheet = dataSnapshot.getValue(Bunksheet.class);
                if (bunksheet.getApprovalLevel() == mUser.getPrivilegeLevel()) {
                    Toast.makeText(getActivity(), getString(R.string.already_approved),
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    if (approved) {
                        StringBuilder approvedBy = new StringBuilder();
                        if (bunksheet.getApprovedBy().equals(getString(R.string.na))) {
                            approvedBy.append(mUser.getName());
                        } else {
                            approvedBy.append(bunksheet.getApprovedBy())
                                    .append(", ")
                                    .append(mUser.getName());
                        }

                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put("approvalLevel", mUser.getPrivilegeLevel());
                        childMap.put("approvedBy", approvedBy.toString());
                        databaseReference.updateChildren(childMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (isAdded()) {
                                            Toast.makeText(getActivity(),
                                                    getString(R.string.approved_successfully),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseReference.addListenerForSingleValueEvent(valueEventListener);
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
        builder.setMessage(getString(R.string.approveBunksheets_offline_message));
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
