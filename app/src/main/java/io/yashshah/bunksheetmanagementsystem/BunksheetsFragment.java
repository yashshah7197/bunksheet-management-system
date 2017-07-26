package io.yashshah.bunksheetmanagementsystem;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


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
        attachRecyclerViewAdapter();

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

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerView_bunksheets);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mRecyclerView.addItemDecoration(mDividerItemDecoration);
    }

    private void attachRecyclerViewAdapter() {
        Query query = mDatabaseReference.orderByChild("userUID").equalTo(mCurrentUser.getUid());
        if (mFirebaseRecyclerAdapter == null) {
            mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Bunksheet, BunksheetViewHolder>(
                    Bunksheet.class,
                    R.layout.bunksheet_list_item,
                    BunksheetViewHolder.class,
                    query) {
                @Override
                protected void populateViewHolder(BunksheetViewHolder holder, Bunksheet bunksheet,
                                                  int position) {
                    holder.bind(bunksheet);
                }
            };
        }
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        attachRecyclerViewAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mFirebaseRecyclerAdapter != null) {
            mFirebaseRecyclerAdapter.cleanup();
        }
    }
}
