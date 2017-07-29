package io.yashshah.bunksheetmanagementsystem;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by yashshah on 22/07/17.
 */

public class BunksheetManagementSystem extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference().keepSynced(true);
    }
}
