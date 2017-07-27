package io.yashshah.bunksheetmanagementsystem;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mFirstNameInputLayout;
    private TextInputLayout mLastNameInputLayout;
    private TextInputLayout mEmailInputLayout;
    private TextInputLayout mPasswordInputLayout;
    private TextInputLayout mConfirmPasswordInputLayout;

    private Button mRegisterButton;

    private ProgressDialog mProgressDialog;

    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mPassword;
    private String mConfirmPassword;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private ValueEventListener mValueEventListener;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupViews();
        setupTextWatchers();
        setupFirebase();
    }

    private void setupViews() {
        mFirstNameInputLayout =
                (TextInputLayout) findViewById(R.id.textInputLayout_register_firstName);
        mFirstNameInputLayout.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        mLastNameInputLayout =
                (TextInputLayout) findViewById(R.id.textInputLayout_register_lastName);
        mLastNameInputLayout.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        mEmailInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout_register_email);

        mPasswordInputLayout =
                (TextInputLayout) findViewById(R.id.textInputLayout_register_password);

        mConfirmPasswordInputLayout =
                (TextInputLayout) findViewById(R.id.textInputLayout_register_confirmPassword);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.user_registration));
        mProgressDialog.setMessage(getString(R.string.creating_user));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        mRegisterButton = (Button) findViewById(R.id.button_register);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.show();
                if (userDetailsValid()) {
                    registerUser();
                } else {
                    mProgressDialog.dismiss();
                    showValidationErrorDialog();
                }
            }
        });
    }

    private void setupTextWatchers() {
        final EditText firstNameEditText = mFirstNameInputLayout.getEditText();
        firstNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                if (isValidFirstName(sequence)) {
                    mFirstNameInputLayout.setErrorEnabled(false);
                } else {
                    mFirstNameInputLayout.setError(getString(R.string.invalid_first_name));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final EditText lastNameEditText = mLastNameInputLayout.getEditText();
        lastNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                if (isValidLastName(sequence)) {
                    mLastNameInputLayout.setErrorEnabled(false);
                } else {
                    mLastNameInputLayout.setError(getString(R.string.invalid_last_name));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final EditText emailEditText = mEmailInputLayout.getEditText();
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                if (isValidEmail(sequence.toString().trim())) {
                    mEmailInputLayout.setErrorEnabled(false);
                } else {
                    mEmailInputLayout.setError(getString(R.string.invalid_email));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final EditText passwordEditText = mPasswordInputLayout.getEditText();
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                if (isValidPassword(sequence.toString())) {
                    mPasswordInputLayout.setErrorEnabled(false);
                } else {
                    mPasswordInputLayout.setError(getString(R.string.invalid_password));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final EditText confirmPasswordEditText = mConfirmPasswordInputLayout.getEditText();
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                if (passwordsMatch(passwordEditText.getText(), sequence)) {
                    mConfirmPasswordInputLayout.setErrorEnabled(false);
                } else {
                    mConfirmPasswordInputLayout
                            .setError(getString(R.string.passwords_not_matching));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");
        mValueEventListener = null;
        mCurrentUser = null;
    }

    private void attachValueEventListener() {
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(mCurrentUser.getUid())) {
                        User newUser
                                = new User(mCurrentUser.getDisplayName(), mCurrentUser.getEmail());
                        mUsersDatabaseReference.child(mCurrentUser.getUid()).setValue(newUser)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        sendVerificationEmail();
                                    }
                                });
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

    private boolean isValidFirstName(CharSequence firstName) {
        return (!TextUtils.isEmpty(firstName));
    }

    private boolean isValidLastName(CharSequence lastName) {
        return (!TextUtils.isEmpty(lastName));
    }

    private boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private boolean isValidPassword(CharSequence password) {
        return (!TextUtils.isEmpty(password) && password.length() >= 6
                && (!password.toString().contains(" ")));
    }

    private boolean passwordsMatch(CharSequence password, CharSequence confirmPassword) {
        return (confirmPassword.toString().equals(password.toString()));
    }

    private boolean userDetailsValid() {
        mFirstName = mFirstNameInputLayout.getEditText().getText().toString();
        mLastName = mLastNameInputLayout.getEditText().getText().toString();
        mEmail = mEmailInputLayout.getEditText().getText().toString();
        mPassword = mPasswordInputLayout.getEditText().getText().toString();
        mConfirmPassword = mConfirmPasswordInputLayout.getEditText().getText().toString();

        return (isValidFirstName(mFirstName) && isValidLastName(mLastName)
                && isValidEmail(mEmail) && isValidPassword(mPassword)
                && passwordsMatch(mPassword, mConfirmPassword));
    }

    private void showValidationErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.validation_error_title));
        builder.setMessage(getString(R.string.validation_error_message));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showRegistrationSuccessfulDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.registration_successful_title));
        builder.setMessage(getString(R.string.registration_successful_message));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void showRegistrationFailedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.registration_failed_title));
        builder.setMessage(getString(R.string.registration_failed_message));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void updateUserDetails() {
        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(mFirstName + " " + mLastName)
                .build();

        mCurrentUser.updateProfile(changeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            attachValueEventListener();
                        } else {
                            Toast.makeText(RegisterActivity.this, getString(R.string.unknown_error),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void sendVerificationEmail() {
        mCurrentUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgressDialog.dismiss();
                            showRegistrationSuccessfulDialog();
                        }
                    }
                });
    }

    private void registerUser() {
        mFirstName = mFirstNameInputLayout.getEditText().getText().toString().trim();
        mLastName = mLastNameInputLayout.getEditText().getText().toString().trim();
        mEmail = mEmailInputLayout.getEditText().getText().toString().trim();
        mPassword = mPasswordInputLayout.getEditText().getText().toString().trim();

        mFirebaseAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
                            if (currentUser != null) {
                                mCurrentUser = currentUser;
                                updateUserDetails();
                            }
                        } else {
                            mProgressDialog.dismiss();
                            showRegistrationFailedDialog();
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachValueEventListener();
    }
}
