package io.yashshah.bunksheetmanagementsystem;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mFirstNameInputLayout;
    private TextInputLayout mLastNameInputLayout;
    private TextInputLayout mEmailInputLayout;
    private TextInputLayout mPasswordInputLayout;
    private TextInputLayout mConfirmPasswordInputLayout;

    private Button mRegisterButton;

    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mPassword;
    private String mConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupViews();
        setupTextWatchers();
    }

    private void setupViews() {
        mFirstNameInputLayout =
                (TextInputLayout) findViewById(R.id.textInputLayout_register_firstName);

        mLastNameInputLayout =
                (TextInputLayout) findViewById(R.id.textInputLayout_register_lastName);

        mEmailInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout_register_email);

        mPasswordInputLayout =
                (TextInputLayout) findViewById(R.id.textInputLayout_register_password);

        mConfirmPasswordInputLayout =
                (TextInputLayout) findViewById(R.id.textInputLayout_register_confirmPassword);

        mRegisterButton = (Button) findViewById(R.id.button_register);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userDetailsValid()) {
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
                if (!isValidFirstName(sequence)) {
                    mFirstNameInputLayout.setError(getString(R.string.invalid_first_name));
                } else {
                    mFirstNameInputLayout.setErrorEnabled(false);
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
                if (!isValidLastName(sequence)) {
                    mLastNameInputLayout.setError(getString(R.string.invalid_last_name));
                } else {
                    mLastNameInputLayout.setErrorEnabled(false);
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
                if (!isValidEmail(sequence.toString().trim())) {
                    mEmailInputLayout.setError(getString(R.string.invalid_email));
                } else {
                    mEmailInputLayout.setErrorEnabled(false);
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
                if (!isValidPassword(sequence.toString())) {
                    mPasswordInputLayout.setError(getString(R.string.invalid_password));
                } else {
                    mPasswordInputLayout.setErrorEnabled(false);
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
                if (!passwordsMatch(passwordEditText.getText(), sequence)) {
                    mConfirmPasswordInputLayout
                            .setError(getString(R.string.passwords_not_matching));
                } else {
                    mConfirmPasswordInputLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
        builder.show();
    }
}
