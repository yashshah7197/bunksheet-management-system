package io.yashshah.bunksheetmanagementsystem;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.yashshah.bunksheetmanagementsystem.data.Bunksheet;
import io.yashshah.bunksheetmanagementsystem.data.User;
import io.yashshah.bunksheetmanagementsystem.viewholders.BunksheetViewHolder;


public class BunksheetsFragment extends Fragment {

    private View mRootView;

    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter mFirebaseRecyclerAdapter;

    private LinearLayoutManager mLinearLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;

    private ItemTouchHelper mItemTouchHelper;
    private ItemTouchHelper.SimpleCallback mItemTouchHelperCallback;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;

    private User mUser;

    private AlertDialog mAlertDialog;

    private Paint mPaint;

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
        setupRecyclerViewSwipeActions();

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

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView_bunksheets);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mRecyclerView.addItemDecoration(mDividerItemDecoration);

    }

    private void setupRecyclerViewSwipeActions() {
        mPaint = new Paint();

        if (mItemTouchHelperCallback == null) {
            mItemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView,
                                      RecyclerView.ViewHolder viewHolder,
                                      RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    String bunksheetKey = mFirebaseRecyclerAdapter.getRef(position).getKey();
                    mFirebaseRecyclerAdapter.notifyItemRemoved(position);
                    showConfirmDeleteAlertDialog(bunksheetKey, position);
                }

                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        View itemView = viewHolder.itemView;

                        if (dX > 0) {
                            mPaint.setColor(ContextCompat.getColor(getActivity(), R.color.colorRed));
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
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
            mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        }
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

    private void deleteBunksheetFromDatabase(String bunksheetKey) {
        DatabaseReference databaseReference =
                mFirebaseDatabase.getReference().child("Bunksheets").child(bunksheetKey);
        databaseReference.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {
                if (isAdded()) {
                    Toast.makeText(getActivity(),
                            getString(R.string.bunksheet_delete_success),
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    private void showConfirmDeleteAlertDialog(final String bunksheetKey, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.confirm_delete_title));
        builder.setMessage(getString(R.string.confirm_delete_message));
        builder.setPositiveButton(getString(R.string.yes_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteBunksheetFromDatabase(bunksheetKey);
                mFirebaseRecyclerAdapter.notifyItemRemoved(position);
                mFirebaseRecyclerAdapter.notifyDataSetChanged();
            }
        });
        builder.setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setCancelable(false);
        builder.show();
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
        builder.setMessage(getString(R.string.bunksheets_offline_message));
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