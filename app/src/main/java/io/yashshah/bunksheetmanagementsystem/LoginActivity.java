package io.yashshah.bunksheetmanagementsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

import io.yashshah.bunksheetmanagementsystem.data.User;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;

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
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private ValueEventListener mValueEventListener;
    private FirebaseUser mCurrentUser;
    private FirebaseAnalytics mFirebaseAnalytics;

    private GoogleApiClient mGoogleApiClient;

    private CallbackManager mCallbackManager;

    private Intent mAfterLoginIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupViews();
        setupFirebase();
        setupGoogleSignIn();
        registerFacebookCallbackManager();

        mAfterLoginIntent = new Intent(LoginActivity.this, MainActivity.class);
        mAfterLoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        mProgressDialog.setCancelable(false);
    }

    private void setupFirebase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");
        mValueEventListener = null;
        mCurrentUser = null;
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions signInOptions
                = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();

    }

    private void registerFacebookCallbackManager() {
        if (mCallbackManager == null) {
            mCallbackManager = CallbackManager.Factory.create();

            LoginManager.getInstance()
                    .registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            authenticateFirebaseWithFacebook(loginResult.getAccessToken());
                        }

                        @Override
                        public void onCancel() {
                            mProgressDialog.dismiss();
                            showSignInFailedAlertDialog();
                        }

                        @Override
                        public void onError(FacebookException error) {
                            mProgressDialog.dismiss();
                            showSignInFailedAlertDialog();
                        }
                    });
        }
    }

    private void unregisterFaceBookCallbackManager() {
        if (mCallbackManager != null) {
            LoginManager.getInstance().unregisterCallback(mCallbackManager);
            mCallbackManager = null;
        }
    }

    private void signInWithGoogle() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void loginWithFacebook() {
        LoginManager.getInstance()
                .logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
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
                                startActivity(mAfterLoginIntent);
                            }
                        } else {
                            mProgressDialog.dismiss();
                            showSignInFailedAlertDialog();
                        }
                    }
                });
    }

    private void authenticateFirebaseWithFacebook(AccessToken accessToken) {
        AuthCredential authCredential =
                FacebookAuthProvider.getCredential(accessToken.getToken());

        mFirebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
                            if (currentUser != null) {
                                mCurrentUser = currentUser;
                                attachValueEventListener();
                            } else {
                                mProgressDialog.dismiss();
                                showSignInFailedAlertDialog();
                            }
                        }
                    }
                });
    }

    private void authenticateFirebaseWithGoogle(final GoogleSignInAccount googleSignInAccount) {
        AuthCredential authCredential
                = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        mFirebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
                            if (currentUser != null) {
                                mCurrentUser = currentUser;
                                attachValueEventListener();
                            }
                        } else {
                            mProgressDialog.dismiss();
                            showSignInFailedAlertDialog();
                        }
                    }
                });
    }

    private void attachValueEventListener() {
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(mCurrentUser.getUid())) {
                        createUserInDatabase();
                    } else {
                        setSharedPrefsPrivilegeLevel();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mUsersDatabaseReference.addListenerForSingleValueEvent(mValueEventListener);
        }
    }

    private void detachValueEventListener() {
        if (mValueEventListener != null) {
            mUsersDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    private void createUserInDatabase() {
        User newUser = new User(mCurrentUser.getDisplayName(), mCurrentUser.getEmail());

        mUsersDatabaseReference.child(mCurrentUser.getUid()).setValue(newUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            setSharedPrefsPrivilegeLevel();
                        } else {
                            mProgressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, getString(R.string.unknown_error),
                                    Toast.LENGTH_LONG).show();
                            startActivity(mAfterLoginIntent);
                        }
                    }
                });
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

    private void setSharedPrefsPrivilegeLevel() {
        DatabaseReference privilegeDatabaseReference =
                mFirebaseDatabase.getReference()
                        .child("Users")
                        .child(mCurrentUser.getUid())
                        .child("privilegeLevel");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int privilegeLevel = dataSnapshot.getValue(Integer.class);
                SharedPreferences preferences =
                        getSharedPreferences(getString(R.string.shared_prefs_name),
                                Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(getString(R.string.saved_privilegeLevel), privilegeLevel);
                editor.apply();
                mProgressDialog.dismiss();
                startActivity(mAfterLoginIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        privilegeDatabaseReference.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.play_services_error), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mProgressDialog.show();
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (signInResult.isSuccess()) {
                GoogleSignInAccount googleSignInAccount = signInResult.getSignInAccount();
                authenticateFirebaseWithGoogle(googleSignInAccount);
            } else {
                mProgressDialog.dismiss();
                showSignInFailedAlertDialog();
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_log_in) {
            signInWithEmailPassword();
        } else if (view.getId() == R.id.imageButton_google_login) {
            signInWithGoogle();
        } else if (view.getId() == R.id.imageButton_fb_login) {
            loginWithFacebook();
        } else if (view.getId() == R.id.textView_forgot_password) {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        } else if (view.getId() == R.id.textView_sign_up) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerFacebookCallbackManager();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterFaceBookCallbackManager();
        detachValueEventListener();
    }
}

