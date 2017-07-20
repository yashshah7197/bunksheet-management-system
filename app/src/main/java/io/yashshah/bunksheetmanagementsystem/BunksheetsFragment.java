package io.yashshah.bunksheetmanagementsystem;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class BunksheetsFragment extends Fragment {

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bunksheets, container, false);
    }

}
