package io.yashshah.bunksheetmanagementsystem;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RequestBunksheetActivity extends AppCompatActivity
        implements CalendarDatePickerDialogFragment.OnDateSetListener {

    private static final String FRAGMENT_DATE_PICKER_TAG = "fragment_date_picker";

    private Spinner mReasonSpinner;
    private Spinner mDateSpinner;

    private CheckBox mFirstSlotCheckBox;
    private CheckBox mSecondSlotCheckBox;
    private CheckBox mThirdSlotCheckBox;

    private TextInputLayout mPlacesVisitedInputLayout;
    private TextInputLayout mNumberOfEntriesInputLayout;

    private Button mRequestBunksheetButton;

    private Calendar mCalendar;

    private ArrayList<String> mArrayList;
    private ArrayAdapter<String> mArrayAdapter;

    private CalendarDatePickerDialogFragment mDatePickerDialogFragment;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;
    private ValueEventListener mSingleValueEventListener;

    private int mUserBunksheetCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_bunksheet);

        mArrayList = new ArrayList<>();
        mArrayList.add(getCurrentDate());

        mArrayAdapter =
                new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, mArrayList);

        setupViews();
        setupFirebase();
    }

    private void setupViews() {
        mReasonSpinner = (Spinner) findViewById(R.id.spinner_reason);

        mDateSpinner = (Spinner) findViewById(R.id.spinner_date);
        mDateSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showDatePicker();
                }
                return true;
            }
        });
        mDateSpinner.setAdapter(mArrayAdapter);

        mFirstSlotCheckBox = (CheckBox) findViewById(R.id.checkbox_firstSlot);
        mSecondSlotCheckBox = (CheckBox) findViewById(R.id.checkbox_secondSLot);
        mThirdSlotCheckBox = (CheckBox) findViewById(R.id.checkbox_thirdSlot);

        mPlacesVisitedInputLayout =
                (TextInputLayout) findViewById(R.id.textInputLayout_placesVisited);
        mNumberOfEntriesInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout_entries);

        mRequestBunksheetButton = (Button) findViewById(R.id.button_request_bunksheet);
        mRequestBunksheetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    createBunksheet();
                } else {
                    Toast.makeText(RequestBunksheetActivity.this,
                            getString(R.string.bunksheet_validation_error),
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    private void setupFirebase() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

    }

    private void showDatePicker() {
        mDatePickerDialogFragment = new CalendarDatePickerDialogFragment();
        mDatePickerDialogFragment.setOnDateSetListener(RequestBunksheetActivity.this);
        mDatePickerDialogFragment.setThemeCustom(R.style.DatePickerStyle);
        mDatePickerDialogFragment.show(getSupportFragmentManager(),
                FRAGMENT_DATE_PICKER_TAG);
    }

    private String getCurrentDate() {
        mCalendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.US);
        return simpleDateFormat.format(mCalendar.getTime());
    }

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear,
                          int dayOfMonth) {
        mCalendar.set(Calendar.MONTH, monthOfYear);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM", Locale.US);
        String month = simpleDateFormat.format(mCalendar.getTime());

        String date = String.valueOf(dayOfMonth) + " " + month + ", " + String.valueOf(year);

        mArrayList.set(0, date);
        mArrayAdapter.notifyDataSetChanged();
    }

    private void createBunksheet() {
        if (mCurrentUser != null) {
            mDatabaseReference = mFirebaseDatabase.getReference()
                    .child("Bunksheets")
                    .child(mCurrentUser.getUid());

            String reason = mReasonSpinner.getSelectedItem().toString();
            String date = mDateSpinner.getSelectedItem().toString();
            String placesVisited =
                    mPlacesVisitedInputLayout.getEditText().getText().toString().trim();
            int numberOfEntries = Integer.parseInt(mNumberOfEntriesInputLayout.getEditText()
                    .getText().toString().trim());

            StringBuilder builder = new StringBuilder();
            if (mFirstSlotCheckBox.isChecked()) {
                builder.append(getString(R.string.slot_first))
                        .append(" ");
            }
            if (mSecondSlotCheckBox.isChecked()) {
                builder.append(getString(R.string.slot_second))
                        .append(" ");
            }
            if (mThirdSlotCheckBox.isChecked()) {
                builder.append(getString(R.string.slot_third))
                        .append(" ");
            }
            String slots = builder.toString();

            Bunksheet bunksheet = new Bunksheet();
            bunksheet.setUserUID(mCurrentUser.getUid());
            bunksheet.setReason(reason);
            bunksheet.setDate(date);
            bunksheet.setTimeSlots(slots);
            bunksheet.setPlacesVisited(placesVisited);
            bunksheet.setNumberOfEntries(numberOfEntries);
            bunksheet.setApprovalLevel(User.PRIVILEGE_STUDENT);

            mDatabaseReference.push().setValue(bunksheet)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            increaseUserBunksheetCount();
                        }
                    });
        }
    }

    private void increaseUserBunksheetCount() {
        mDatabaseReference = mFirebaseDatabase.getReference()
                .child("Users")
                .child(mCurrentUser.getUid())
                .child("bunksheetsRequested");

        setupSingleValueEventListener();
        mDatabaseReference.addListenerForSingleValueEvent(mSingleValueEventListener);
    }

    private void setupSingleValueEventListener() {
        if (mSingleValueEventListener == null) {
            mSingleValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUserBunksheetCount = dataSnapshot.getValue(Integer.class);

                    mDatabaseReference.setValue(mUserBunksheetCount + 1)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RequestBunksheetActivity.this,
                                                getString(R.string.bunksheet_request_successful),
                                                Toast.LENGTH_LONG)
                                                .show();
                                    } else {
                                        Toast.makeText(RequestBunksheetActivity.this,
                                                getString(R.string.bunksheet_request_failed),
                                                Toast.LENGTH_LONG)
                                                .show();
                                    }
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
    }

    private boolean isValidSlot() {
        return (mFirstSlotCheckBox.isChecked() || mSecondSlotCheckBox.isChecked()
                || mThirdSlotCheckBox.isChecked());
    }

    private boolean isValidPlace(CharSequence place) {
        return (!TextUtils.isEmpty(place));
    }

    private boolean isValidNumberOfEntries(CharSequence number) {
        return (!TextUtils.isEmpty(number));
    }

    private boolean isValidInput() {
        CharSequence places = mPlacesVisitedInputLayout.getEditText().getText();
        CharSequence numberOfEntries = mNumberOfEntriesInputLayout.getEditText().getText();

        return (isValidSlot() && isValidPlace(places) && isValidNumberOfEntries(numberOfEntries));
    }

    private void cleanupSingleValueEventListener() {
        if (mSingleValueEventListener != null) {
            mDatabaseReference.removeEventListener(mSingleValueEventListener);
            mSingleValueEventListener = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cleanupSingleValueEventListener();
    }
}
