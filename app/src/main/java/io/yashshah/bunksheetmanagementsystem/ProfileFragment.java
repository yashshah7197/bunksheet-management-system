package io.yashshah.bunksheetmanagementsystem;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class ProfileFragment extends Fragment {

    private View mRootView;

    private TextInputLayout mFirstNameInputLayout;
    private TextInputLayout mLastNameInputLayout;
    private TextInputLayout mPhoneNumberInputLayout;
    private TextInputLayout mRollNumberInputLayout;

    private Spinner mYearSpinner;
    private Spinner mDivisionSpinner;
    private Spinner mClassTeacherSpinner;
    private Spinner mTeacherGuardianSpinner;

    private Button mSaveProfileButton;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private FirebaseUser mCurrentUser;
    private ValueEventListener mUserValueEventListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {

        Bundle args = new Bundle();

        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_profile, container, false);

        setupViews();
        setupTextWatchers();
        setupFirebase();
        loadProfile();

        return mRootView;
    }

    private void setupViews() {
        mFirstNameInputLayout =
                (TextInputLayout) mRootView.findViewById(R.id.textInputLayout_profile_firstName);
        mLastNameInputLayout
                = (TextInputLayout) mRootView.findViewById(R.id.textInputLayout_profile_lastName);
        mPhoneNumberInputLayout =
                (TextInputLayout) mRootView.findViewById(R.id.textInputLayout_profile_phoneNumber);
        mRollNumberInputLayout =
                (TextInputLayout) mRootView.findViewById(R.id.textInputLayout_profile_rollNumber);

        mYearSpinner = (Spinner) mRootView.findViewById(R.id.spinner_year);
        mDivisionSpinner = (Spinner) mRootView.findViewById(R.id.spinner_division);
        mClassTeacherSpinner = (Spinner) mRootView.findViewById(R.id.spinner_classTeacher);
        mTeacherGuardianSpinner = (Spinner) mRootView.findViewById(R.id.spinner_teacherGuardian);

        mSaveProfileButton = (Button) mRootView.findViewById(R.id.button_save_profile);
        mSaveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    saveProfileInfo();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.profile_validation_error),
                            Toast.LENGTH_LONG)
                            .show();
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

        final EditText phoneNumberEditText = mPhoneNumberInputLayout.getEditText();
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                if (isValidPhoneNumber(sequence)) {
                    mPhoneNumberInputLayout.setErrorEnabled(false);
                } else {
                    mPhoneNumberInputLayout.setError(getString(R.string.invalid_phoneNumber));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final EditText rollNumberEditText = mRollNumberInputLayout.getEditText();
        rollNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                if (isValidRollNumber(sequence)) {
                    mRollNumberInputLayout.setErrorEnabled(false);
                } else {
                    mRollNumberInputLayout.setError(getString(R.string.invalid_rollNumber));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setupFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseReference =
                mFirebaseDatabase.getReference().child("Users").child(mCurrentUser.getUid());
        mUserValueEventListener = null;

        mUserDatabaseReference.keepSynced(true);
    }

    private void loadProfile() {
        if (mUserValueEventListener == null) {
            mUserValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    updateUIWithProfile(user);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
        mUserDatabaseReference.addValueEventListener(mUserValueEventListener);
    }

    private void updateUIWithProfile(User user) {
        String firstName = user.getName().split(" ")[0];
        String lastName = user.getName().split(" ")[1];

        mFirstNameInputLayout.getEditText().setText(firstName);
        mLastNameInputLayout.getEditText().setText(lastName);
        mPhoneNumberInputLayout.getEditText().setText(user.getPhoneNumber());
        mRollNumberInputLayout.getEditText().setText(user.getRollNumber());

        ArrayAdapter<CharSequence> arrayAdapter =
                ArrayAdapter.createFromResource(getActivity(), R.array.year_array,
                        R.layout.support_simple_spinner_dropdown_item);
        mYearSpinner.setSelection(arrayAdapter.getPosition(user.getYear()));

        arrayAdapter =
                ArrayAdapter.createFromResource(getActivity(), R.array.division_array,
                        R.layout.support_simple_spinner_dropdown_item);
        mDivisionSpinner.setSelection(arrayAdapter.getPosition(user.getDivision()));

        arrayAdapter =
                ArrayAdapter.createFromResource(getActivity(), R.array.classTeachers_array,
                        R.layout.support_simple_spinner_dropdown_item);
        mClassTeacherSpinner.setSelection(arrayAdapter.getPosition(user.getClassTeacher()));

        arrayAdapter =
                ArrayAdapter.createFromResource(getActivity(), R.array.teacherGuardians_array,
                        R.layout.support_simple_spinner_dropdown_item);
        mTeacherGuardianSpinner.setSelection(arrayAdapter.getPosition(user.getTeacherGuardian()));
    }

    private void saveProfileInfo() {
        String firstName = mFirstNameInputLayout.getEditText().getText().toString().trim();
        String lastName = mLastNameInputLayout.getEditText().getText().toString().trim();
        String phoneNumber = mPhoneNumberInputLayout.getEditText().getText().toString().trim();
        String rollNumber = mRollNumberInputLayout.getEditText().getText().toString().trim();
        String year = mYearSpinner.getSelectedItem().toString();
        String division = mDivisionSpinner.getSelectedItem().toString();
        String classTeacher = mClassTeacherSpinner.getSelectedItem().toString();
        String teacherGuardian = mTeacherGuardianSpinner.getSelectedItem().toString();

        User user = new User();
        user.setName(firstName + " " + lastName);
        user.setPhoneNumber(phoneNumber);
        user.setRollNumber(rollNumber);
        user.setYear(year);
        user.setDivision(division);
        user.setClassTeacher(classTeacher);
        user.setTeacherGuardian(teacherGuardian);

        mUserDatabaseReference.updateChildren(user.toMap())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(),
                                    getString(R.string.profile_update_successful),
                                    Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.profile_update_failed),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public boolean isValidFirstName(CharSequence firstName) {
        return (!TextUtils.isEmpty(firstName));
    }

    public boolean isValidLastName(CharSequence lastName) {
        return (!TextUtils.isEmpty(lastName));
    }

    public boolean isValidPhoneNumber(CharSequence phoneNumber) {
        return (!TextUtils.isEmpty(phoneNumber) && Pattern.matches("^[789]\\d{9}$", phoneNumber));
    }

    public boolean isValidRollNumber(CharSequence rollNumber) {
        return (!TextUtils.isEmpty(rollNumber) && Pattern.matches("^\\d{3}$", rollNumber));
    }

    public boolean isValidYear() {
        return !mYearSpinner.getSelectedItem().toString().equals(getString(R.string.na));
    }

    public boolean isValidDivision() {
        return !mDivisionSpinner.getSelectedItem().toString().equals(getString(R.string.na));
    }

    public boolean isValidClassTeacher() {
        return !mClassTeacherSpinner.getSelectedItem().toString().equals(R.string.na);
    }

    public boolean isValidTeacherGuardian() {
        return !mTeacherGuardianSpinner.getSelectedItem().toString().equals(R.string.na);
    }

    public boolean isValidInput() {
        CharSequence firstName = mFirstNameInputLayout.getEditText().getText();
        CharSequence lastname = mLastNameInputLayout.getEditText().getText();
        CharSequence phoneNumber = mPhoneNumberInputLayout.getEditText().getText();
        CharSequence rollNumber = mRollNumberInputLayout.getEditText().getText();

        return (isValidFirstName(firstName) && isValidLastName(lastname)
                && isValidPhoneNumber(phoneNumber) && isValidRollNumber(rollNumber) && isValidYear()
                && isValidDivision() && isValidClassTeacher() && isValidTeacherGuardian());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mUserValueEventListener != null) {
            mUserDatabaseReference.removeEventListener(mUserValueEventListener);
            mUserValueEventListener = null;
        }
    }
}
