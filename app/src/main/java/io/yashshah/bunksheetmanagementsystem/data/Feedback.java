package io.yashshah.bunksheetmanagementsystem.data;

/**
 * Created by yashshah on 30/07/17.
 */

public class Feedback {

  private String mUserExperienceRating;
  private String mFunctionalityRating;
  private String mOtherComments;

  public Feedback(String mUserExperienceRating, String mFunctionalityRating,
      String mOtherComments) {
    this.mUserExperienceRating = mUserExperienceRating;
    this.mFunctionalityRating = mFunctionalityRating;
    this.mOtherComments = mOtherComments;
  }

  public String getUserExperienceRating() {
    return mUserExperienceRating;
  }

  public String getFunctionalityRating() {
    return mFunctionalityRating;
  }

  public String getOtherComments() {
    return mOtherComments;
  }
}
