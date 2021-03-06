package io.yashshah.bunksheetmanagementsystem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import io.yashshah.bunksheetmanagementsystem.data.Bunksheet;
import io.yashshah.bunksheetmanagementsystem.data.User;
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

  private ProgressDialog mProgressDialog;

  private Calendar mCalendar;

  private ArrayList<String> mArrayList;
  private ArrayAdapter<String> mArrayAdapter;

  private CalendarDatePickerDialogFragment mDatePickerDialogFragment;

  private FirebaseAuth mFirebaseAuth;
  private FirebaseDatabase mFirebaseDatabase;
  private DatabaseReference mDatabaseReference;
  private FirebaseUser mCurrentUser;
  private ValueEventListener mSingleValueEventListener;
  private FirebaseAnalytics mFirebaseAnalytics;

  private int mUserBunksheetCount;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_request_bunksheet);

    mArrayList = new ArrayList<>();
    mArrayList.add(getCurrentDate());

    mArrayAdapter =
        new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, mArrayList);

    setupViews();
    setupTextWatchers();
    setupFirebase();
  }

  private void setupViews() {
    mReasonSpinner = (Spinner) findViewById(R.id.spinner_reason);

    mDateSpinner = (Spinner) findViewById(R.id.spinner_date);
    mDateSpinner.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
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

    mPlacesVisitedInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout_placesVisited);
    mNumberOfEntriesInputLayout = (TextInputLayout) findViewById(R.id.textInputLayout_entries);

    mRequestBunksheetButton = (Button) findViewById(R.id.button_request_bunksheet);
    mRequestBunksheetButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (isValidInput()) {
          createBunksheet();
        } else {
          Toast.makeText(RequestBunksheetActivity.this,
              getString(R.string.bunksheet_validation_error), Toast.LENGTH_LONG).show();
        }
      }
    });

    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setTitle(getString(R.string.submitting_bunksheet_title));
    mProgressDialog.setMessage(getString(R.string.submitting_bunksheet_message));
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setCancelable(false);
  }

  private void setupTextWatchers() {
    final EditText placesVisitedEditText = mPlacesVisitedInputLayout.getEditText();
    placesVisitedEditText.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence sequence, int start, int before, int count) {
        if (isValidPlace(sequence)) {
          mPlacesVisitedInputLayout.setErrorEnabled(false);
        } else {
          mPlacesVisitedInputLayout.setError(getString(R.string.invalid_place));
        }
      }

      @Override public void afterTextChanged(Editable s) {

      }
    });

    final EditText numberOfEntriesEditText = mNumberOfEntriesInputLayout.getEditText();
    numberOfEntriesEditText.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence sequence, int start, int before, int count) {
        if (isValidNumberOfEntries(sequence)) {
          mNumberOfEntriesInputLayout.setErrorEnabled(false);
        } else {
          mNumberOfEntriesInputLayout.setError(getString(R.string.invalid_numberEntries));
        }
      }

      @Override public void afterTextChanged(Editable s) {

      }
    });
  }

  private void setupFirebase() {
    mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    mFirebaseAuth = FirebaseAuth.getInstance();
    mFirebaseDatabase = FirebaseDatabase.getInstance();
    mDatabaseReference = mFirebaseDatabase.getReference();
    mCurrentUser = mFirebaseAuth.getCurrentUser();
  }

  private void showDatePicker() {
    mDatePickerDialogFragment = new CalendarDatePickerDialogFragment();
    mDatePickerDialogFragment.setOnDateSetListener(RequestBunksheetActivity.this);
    mDatePickerDialogFragment.setThemeCustom(R.style.DatePickerStyle);
    mDatePickerDialogFragment.show(getSupportFragmentManager(), FRAGMENT_DATE_PICKER_TAG);
  }

  private String getCurrentDate() {
    mCalendar = Calendar.getInstance();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.US);
    return simpleDateFormat.format(mCalendar.getTime());
  }

  @Override
  public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear,
      int dayOfMonth) {
    if (isFutureDate(dayOfMonth, monthOfYear, year)) {
      Toast.makeText(RequestBunksheetActivity.this, getString(R.string.future_date_error),
          Toast.LENGTH_LONG).show();
      return;
    }

    mCalendar.set(Calendar.MONTH, monthOfYear);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM", Locale.US);
    String month = simpleDateFormat.format(mCalendar.getTime());

    String date = String.valueOf(dayOfMonth) + " " + month + ", " + String.valueOf(year);

    mArrayList.set(0, date);
    mArrayAdapter.notifyDataSetChanged();
  }

  private void createBunksheet() {
    mProgressDialog.show();
    if (!isConnected()) {
      mProgressDialog.dismiss();
      showNotConnectedAlertDialog();
    }
    if (mCurrentUser != null) {
      mDatabaseReference = mFirebaseDatabase.getReference().child("Bunksheets");

      String reason = mReasonSpinner.getSelectedItem().toString();
      String date = mDateSpinner.getSelectedItem().toString();
      String placesVisited = mPlacesVisitedInputLayout.getEditText().getText().toString().trim();
      int numberOfEntries =
          Integer.parseInt(mNumberOfEntriesInputLayout.getEditText().getText().toString().trim());

      StringBuilder builder = new StringBuilder();
      if (mFirstSlotCheckBox.isChecked()) {
        builder.append(getString(R.string.one)).append(" ");
      }
      if (mSecondSlotCheckBox.isChecked()) {
        builder.append(getString(R.string.two)).append(" ");
      }
      if (mThirdSlotCheckBox.isChecked()) {
        builder.append(getString(R.string.three)).append(" ");
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
      bunksheet.setApprovedBy(getString(R.string.na));

      mDatabaseReference.push()
          .setValue(bunksheet)
          .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override public void onComplete(@NonNull Task<Void> task) {
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
        @Override public void onDataChange(DataSnapshot dataSnapshot) {
          mUserBunksheetCount = dataSnapshot.getValue(Integer.class);

          mDatabaseReference.setValue(mUserBunksheetCount + 1)
              .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                  if (task.isSuccessful()) {
                    mProgressDialog.dismiss();
                    Toast.makeText(RequestBunksheetActivity.this,
                        getString(R.string.bunksheet_request_successful), Toast.LENGTH_LONG).show();
                    finish();
                  } else {
                    mProgressDialog.dismiss();
                    Toast.makeText(RequestBunksheetActivity.this,
                        getString(R.string.bunksheet_request_failed), Toast.LENGTH_LONG).show();
                    finish();
                  }
                }
              });
        }

        @Override public void onCancelled(DatabaseError databaseError) {

        }
      };
    }
  }

  private boolean isValidSlot() {
    return (mFirstSlotCheckBox.isChecked()
        || mSecondSlotCheckBox.isChecked()
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

  private boolean isFutureDate(int dayOfMonth, int monthOfYear, int year) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    calendar.set(Calendar.MONTH, monthOfYear);
    calendar.set(Calendar.YEAR, year);
    return calendar.getTime().after(Calendar.getInstance().getTime());
  }

  private boolean isConnected() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    return networkInfo != null && networkInfo.isConnectedOrConnecting();
  }

  private void showNotConnectedAlertDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.device_offline));
    builder.setMessage(getString(R.string.request_bunksheet_offline_message));
    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        finish();
      }
    });
    builder.setCancelable(false);
    builder.show();
  }

  private void cleanupSingleValueEventListener() {
    if (mSingleValueEventListener != null) {
      mDatabaseReference.removeEventListener(mSingleValueEventListener);
      mSingleValueEventListener = null;
    }
  }

  @Override protected void onStop() {
    super.onStop();
    cleanupSingleValueEventListener();
  }
}
