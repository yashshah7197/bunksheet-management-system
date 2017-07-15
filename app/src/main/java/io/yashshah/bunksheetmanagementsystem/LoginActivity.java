package io.yashshah.bunksheetmanagementsystem;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatImageButton mFacebookLoginImageButton;
    private AppCompatImageButton mGoogleSignInImageButton;

    private TextInputLayout mEmailInputLayout;
    private TextInputLayout mPasswordInputLayout;

    private TextView mForgotPasswordTextView;
    private TextView mSignUpTextView;

    private Button mLoginButton;

    private ProgressDialog mProgressDialog;

    private String mEmail;
    private String mPassword;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupViews();

        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    private void setupViews() {
        mFacebookLoginImageButton = (AppCompatImageButton) findViewById(R.id.imageButton_fb_login);
        mFacebookLoginImageButton.setOnClickListener(this);

        mGoogleSignInImageButton =
                (AppCompatImageButton) findViewById(R.id.imageButton_google_login);
        mGoogleSignInImageButton.setOnClickListener(this);

        mEmailInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout_email);
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout_password);

        mForgotPasswordTextView = (TextView) findViewById(R.id.textView_forgot_password);
        mForgotPasswordTextView.setOnClickListener(this);

        mSignUpTextView = (TextView) findViewById(R.id.textView_sign_up);
        mSignUpTextView.setOnClickListener(this);

        mLoginButton = (Button) findViewById(R.id.button_log_in);
        mLoginButton.setOnClickListener(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.logging_in_title));
        mProgressDialog.setMessage(getString(R.string.logging_in_message));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
    }

    private void showSignInFailedAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.login_failed_title));
        builder.setMessage(getString(R.string.login_failed_message));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void signInWithEmailPassword() {
        if (TextUtils.isEmpty(mEmailInputLayout.getEditText().getText())
                || TextUtils.isEmpty(mPasswordInputLayout.getEditText().getText())) {
            showSignInFailedAlertDialog();
            return;
        }

        mProgressDialog.show();

        mEmail = mEmailInputLayout.getEditText().getText().toString().trim();
        mPassword = mPasswordInputLayout.getEditText().getText().toString().trim();

        mFirebaseAuth.signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
                            if (currentUser != null) {
                                mProgressDialog.dismiss();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        } else {
                            mProgressDialog.dismiss();
                            showSignInFailedAlertDialog();
                        }
                    }
                });
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_log_in) {
            signInWithEmailPassword();
        }
    }
}

