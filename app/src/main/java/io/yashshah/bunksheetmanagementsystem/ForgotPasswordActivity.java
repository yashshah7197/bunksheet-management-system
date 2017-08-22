package io.yashshah.bunksheetmanagementsystem;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

  private TextInputLayout mEmailInputLayout;
  private Button mSendResetLinkButton;

  private ProgressDialog mProgressDialog;

  private FirebaseAuth mFirebaseAuth;
  private FirebaseAnalytics mFirebaseAnalytics;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_forgot_password);

    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    mEmailInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout_forgot_password_email);
    mEmailInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence sequence, int start, int before, int count) {
        if (isValidEmail(sequence)) {
          mEmailInputLayout.setErrorEnabled(false);
        } else {
          mEmailInputLayout.setError(getString(R.string.invalid_email));
        }
      }

      @Override public void afterTextChanged(Editable s) {

      }
    });

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(getString(R.string.reset_password_title));
    mProgressDialog.setMessage(getString(R.string.reset_password_progress_message));
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setCancelable(false);

    mSendResetLinkButton = (Button) findViewById(R.id.button_send_reset_link);
    mSendResetLinkButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mProgressDialog.show();
        if (isValidEmail(mEmailInputLayout.getEditText().getText())) {
          mFirebaseAuth = FirebaseAuth.getInstance();
          mFirebaseAuth.sendPasswordResetEmail(
              mEmailInputLayout.getEditText().getText().toString().trim())
              .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                  if (task.isSuccessful()) {
                    mProgressDialog.dismiss();
                    showSuccessAlertDialog();
                  } else {
                    mProgressDialog.dismiss();
                    showFailureDialog();
                  }
                }
              });
        } else {
          mProgressDialog.dismiss();
          showValidationErrorDialog();
        }
      }
    });
  }

  private boolean isValidEmail(CharSequence email) {
    return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
  }

  private void showSuccessAlertDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.reset_password_title));
    builder.setMessage(getString(R.string.reset_password_message));
    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        finish();
      }
    });
    builder.setCancelable(false);
    builder.show();
  }

  private void showFailureDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.reset_password_title));
    builder.setMessage(getString(R.string.reset_password_error));
    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
      }
    });
    builder.setCancelable(false);
    builder.show();
  }

  private void showValidationErrorDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.validation_error_title));
    builder.setMessage(getString(R.string.validation_error_message));
    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
      }
    });
    builder.setCancelable(false);
    builder.show();
  }
}
