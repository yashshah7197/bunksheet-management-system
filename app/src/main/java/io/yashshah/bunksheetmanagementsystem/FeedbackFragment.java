package io.yashshah.bunksheetmanagementsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import io.yashshah.bunksheetmanagementsystem.data.Feedback;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedbackFragment extends Fragment {

  private View mRootView;

  private Spinner mUserExperienceRatingSpinner;
  private Spinner mFunctionalityRatingSpinner;

  private TextInputLayout mOtherCommentsInputLayout;

  private Button mSendFeedbackButton;

  private FirebaseDatabase mFirebaseDatabase;
  private DatabaseReference mFeedbackDatabaseReference;

  private ProgressDialog mProgressDialog;

  public FeedbackFragment() {
    // Required empty public constructor
  }

  public static FeedbackFragment newInstance() {

    Bundle args = new Bundle();

    FeedbackFragment fragment = new FeedbackFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    mRootView = inflater.inflate(R.layout.fragment_feedback, container, false);
    setupViews();
    setupFirebase();
    return mRootView;
  }

  private void setupViews() {
    mUserExperienceRatingSpinner =
        (Spinner) mRootView.findViewById(R.id.spinner_feedback_userExperience);
    mFunctionalityRatingSpinner =
        (Spinner) mRootView.findViewById(R.id.spinner_feedback_functionality);

    mOtherCommentsInputLayout =
        (TextInputLayout) mRootView.findViewById(R.id.textInputLayout_otherComments);

    mSendFeedbackButton = (Button) mRootView.findViewById(R.id.button_send_feedback);
    mSendFeedbackButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        sendFeedback();
      }
    });

    mProgressDialog = new ProgressDialog(getActivity());
    mProgressDialog.setTitle(getString(R.string.submitting_feedback_title));
    mProgressDialog.setMessage(getString(R.string.submitting_feedback_message));
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setCancelable(false);
  }

  private void setupFirebase() {
    mFirebaseDatabase = FirebaseDatabase.getInstance();
    mFeedbackDatabaseReference = mFirebaseDatabase.getReference().child("Feedback");
  }

  private void sendFeedback() {
    mProgressDialog.show();
    if (!isConnected()) {
      mProgressDialog.dismiss();
      showNotConnectedAlertDialog();
    }
    String userExperience = mUserExperienceRatingSpinner.getSelectedItem().toString();
    String functionality = mFunctionalityRatingSpinner.getSelectedItem().toString();
    String otherComments = mOtherCommentsInputLayout.getEditText().getText().toString().trim();

    Feedback feedback = new Feedback(userExperience, functionality, otherComments);

    mFeedbackDatabaseReference.push()
        .setValue(feedback)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
              if (isAdded()) {
                mProgressDialog.dismiss();
                showThankYouAlertDialog();
              }
            } else {
              if (isAdded()) {
                mProgressDialog.dismiss();
                Toast.makeText(getActivity(), getString(R.string.feedback_submit_failed),
                    Toast.LENGTH_LONG).show();
              }
            }
          }
        });
  }

  private void showThankYouAlertDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(getString(R.string.thank_you));
    builder.setMessage(getString(R.string.feedback_sent_successfully));
    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        ((MainActivity) getActivity()).openDrawer();
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
    builder.setMessage(getString(R.string.feedback_offline_message));
    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {

      }
    });
    builder.setCancelable(false);
    builder.show();
  }
}
