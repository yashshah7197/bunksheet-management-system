package io.yashshah.bunksheetmanagementsystem.viewholders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import io.yashshah.bunksheetmanagementsystem.R;
import io.yashshah.bunksheetmanagementsystem.data.Bunksheet;
import io.yashshah.bunksheetmanagementsystem.data.User;

/**
 * Created by yashshah on 24/07/17.
 */

public class BunksheetViewHolder extends RecyclerView.ViewHolder {

  private Bunksheet bunksheet;
  private User user;
  private TextView mReasonTextView;
  private TextView mPlacesVisitedTextView;
  private TextView mNumberOfEntriesTextView;
  private TextView mSlotsTextView;
  private TextView mDateTextView;
  private TextView mRequestedByTextView;
  private TextView mApprovedByTextView;

  public BunksheetViewHolder(View itemView) {
    super(itemView);

    mReasonTextView = (TextView) itemView.findViewById(R.id.textView_reason);
    mPlacesVisitedTextView = (TextView) itemView.findViewById(R.id.textView_placesVisited);
    mNumberOfEntriesTextView = (TextView) itemView.findViewById(R.id.textView_numberOfEntries);
    mSlotsTextView = (TextView) itemView.findViewById(R.id.textView_slots);
    mDateTextView = (TextView) itemView.findViewById(R.id.textView_date);
    mRequestedByTextView = (TextView) itemView.findViewById(R.id.textView_requestedBy);
    mApprovedByTextView = (TextView) itemView.findViewById(R.id.textView_approvedBy);
  }

  public void bind(Bunksheet bunksheet, User user) {
    this.bunksheet = bunksheet;
    this.user = user;

    setReason(bunksheet.getReason());
    setPlacesVisited(bunksheet.getPlacesVisited());
    setNumberOfEntries(bunksheet.getNumberOfEntries());
    setSlots(bunksheet.getTimeSlots());
    setDate(bunksheet.getDate());
    setRequestedBy();
    setRequestedByVisibility();
    setApprovedBy(bunksheet.getApprovedBy());
    setApprovedByColor(bunksheet.getApprovalLevel());
  }

  private void setReason(String reason) {
    mReasonTextView.setText(reason);
  }

  private void setPlacesVisited(String placesVisited) {
    String placesString = itemView.getResources().getString(R.string.places) + " " + placesVisited;
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

  private void setRequestedByVisibility() {
    if (user.getPrivilegeLevel() == User.PRIVILEGE_STUDENT) {
      mRequestedByTextView.setVisibility(View.GONE);
    }
  }

  private void setRequestedBy() {
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference =
        firebaseDatabase.getReference().child("Users").child(bunksheet.getUserUID()).child("name");

    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        String name = dataSnapshot.getValue(String.class);
        String requestedByString =
            itemView.getResources().getString(R.string.requested_by) + " " + name;
        mRequestedByTextView.setText(requestedByString);
      }

      @Override public void onCancelled(DatabaseError databaseError) {

      }
    });
  }

  private void setApprovedBy(String approvedBy) {
    mApprovedByTextView.setText(approvedBy);
  }

  private void setApprovedByColor(int approvalLevel) {
    if (approvalLevel == User.PRIVILEGE_STUDENT) {
      mApprovedByTextView.setTextColor(
          ContextCompat.getColor(itemView.getContext(), R.color.colorRed));
    } else if (approvalLevel == User.PRIVILEGE_HEAD) {
      mApprovedByTextView.setTextColor(
          ContextCompat.getColor(itemView.getContext(), R.color.colorOrange));
    } else if (approvalLevel == User.PRIVILEGE_TEACHER || approvalLevel == User.PRIVILEGE_HOD) {
      mApprovedByTextView.setTextColor(
          ContextCompat.getColor(itemView.getContext(), R.color.colorGreen));
    } else if (approvalLevel == -1) {
      mApprovedByTextView.setTextColor(
          ContextCompat.getColor(itemView.getContext(), R.color.colorRed));
    }
  }
}
