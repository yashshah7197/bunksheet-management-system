package io.yashshah.bunksheetmanagementsystem;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by yashshah on 24/07/17.
 */

public class BunksheetViewHolder extends RecyclerView.ViewHolder {

    private TextView mReasonTextView;
    private TextView mPlacesVisitedTextView;
    private TextView mNumberOfEntriesTextView;
    private TextView mSlotsTextView;
    private TextView mDateTextView;

    public BunksheetViewHolder(View itemView) {
        super(itemView);

        mReasonTextView = (TextView) itemView.findViewById(R.id.textView_reason);
        mPlacesVisitedTextView = (TextView) itemView.findViewById(R.id.textView_placesVisited);
        mNumberOfEntriesTextView = (TextView) itemView.findViewById(R.id.textView_numberOfEntries);
        mSlotsTextView = (TextView) itemView.findViewById(R.id.textView_slots);
        mDateTextView = (TextView) itemView.findViewById(R.id.textView_date);
    }

    public void bind(Bunksheet bunksheet) {
        setReason(bunksheet.getReason());
        setPlacesVisited(bunksheet.getPlacesVisited());
        setNumberOfEntries(bunksheet.getNumberOfEntries());
        setSlots(bunksheet.getTimeSlots());
        setDate(bunksheet.getDate());
    }

    private void setReason(String reason) {
        mReasonTextView.setText(reason);
    }

    private void setPlacesVisited(String placesVisited) {
        String placesString =
                itemView.getResources().getString(R.string.places) + " " + placesVisited;
        mPlacesVisitedTextView.setText(placesString);
    }

    private void setNumberOfEntries(int numberOfEntries) {
        String numberString =
                itemView.getResources().getString(R.string.entries) + " " + numberOfEntries;
        mNumberOfEntriesTextView.setText(String.valueOf(numberString));
    }

    private void setSlots(String slots) {
        String slotsString = itemView.getResources().getString(R.string.slots) + " " + slots;
        mSlotsTextView.setText(slotsString);
    }

    private void setDate(String date) {
        String dateString = itemView.getResources().getString(R.string.date_textView) + " " + date;
        mDateTextView.setText(dateString);
    }
}
