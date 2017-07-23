package io.yashshah.bunksheetmanagementsystem;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_bunksheet);

        mArrayList = new ArrayList<>();
        mArrayList.add(getCurrentDate());

        mArrayAdapter =
                new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, mArrayList);

        setupViews();
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
}
