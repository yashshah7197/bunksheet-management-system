package io.yashshah.bunksheetmanagementsystem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

interface DrawerActionsInterface {
    void selectDrawerItem(int itemId);

    void openDrawer();
}

public class MainActivity extends AppCompatActivity implements DrawerActionsInterface {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserDatabaseReference;
    private ValueEventListener mUserValueEventListener;
    private FirebaseAnalytics mFirebaseAnalytics;

    private FirebaseUser mCurrentUser;

    private Intent mLoginIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
        setupFirebase();

        mLoginIntent = new Intent(this, LoginActivity.class);
        mLoginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    private void setupViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.open_drawer, R.string.close_drawer);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);

        mNavigationView = (NavigationView) findViewById(R.id.navigationView);
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        selectNavigationItem(item);
                        return true;
                    }
                });
    }

    private void setupFirebase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseReference = mDatabase.getReference();
        mAuthStateListener = null;
        mUserValueEventListener = null;
        mCurrentUser = null;
    }

    private void attachAuthStateListener() {
        if (mAuthStateListener == null) {
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    if (currentUser != null) {
                        mCurrentUser = currentUser;
                        goToStartupFragment();
                        loadUserInfo();
                    } else {
                        startActivity(mLoginIntent);
                    }
                }
            };
        }
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void detachAuthStateListener() {
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            mAuthStateListener = null;
        }
    }

    private void loadUserInfo() {
        mUserDatabaseReference =
                mDatabase.getReference().child("Users").child(mCurrentUser.getUid());
        mUserDatabaseReference.keepSynced(true);

        if (mUserValueEventListener == null) {
            mUserValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        updateNavigationDrawer(user.getName(), user.getYear(), user.getDivision(),
                                user.getRollNumber(), user.getPrivilegeLevel());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
        mUserDatabaseReference.addValueEventListener(mUserValueEventListener);
    }

    private void updateNavigationDrawer(String name, String year, String division, String rollNo,
                                        int privilegeLevel) {
        View view = mNavigationView.getHeaderView(0);
        TextView nameTextView = (TextView) view.findViewById(R.id.textView_header_name);
        TextView yearDivisionTextView =
                (TextView) view.findViewById(R.id.textView_header_year_division);
        TextView rollNumberTextView = (TextView) view.findViewById(R.id.textView_header_rollNumber);

        String yearDivisionString = year + " - " + division;

        nameTextView.setText(name);
        yearDivisionTextView.setText(yearDivisionString);
        rollNumberTextView.setText(rollNo);

        if (privilegeLevel > User.PRIVILEGE_STUDENT) {
            mNavigationView
                    .getMenu()
                    .findItem(R.id.navigation_approve_bunksheets)
                    .setVisible(true);
            mNavigationView
                    .getMenu()
                    .findItem(R.id.navigation_bunksheets)
                    .setVisible(false);
            mNavigationView
                    .getMenu()
                    .findItem(R.id.navigation_approved_bunksheets)
                    .setVisible(true);
        } else {
            mNavigationView
                    .getMenu()
                    .findItem(R.id.navigation_approve_bunksheets)
                    .setVisible(false);
        }
    }

    private int getSharedPrefsPrivilegeLevel() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.shared_prefs_name), Context.MODE_PRIVATE);
        return sharedPreferences.getInt(getString(R.string.saved_privilegeLevel), -1);
    }

    private void goToStartupFragment() {
        int privilegeLevel = getSharedPrefsPrivilegeLevel();
        if (privilegeLevel <= User.PRIVILEGE_STUDENT) {
            selectNavigationItem(mNavigationView.getMenu().findItem(R.id.navigation_bunksheets));
        } else {
            selectNavigationItem(mNavigationView.getMenu()
                    .findItem(R.id.navigation_approve_bunksheets));
        }
    }

    private void goToGooglePlayPage() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=io.yashshah.bunksheetmanagementsystem"));
        startActivity(intent);
    }

    private void selectNavigationItem(MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_bunksheets:
                fragment = BunksheetsFragment.newInstance();
                mNavigationView.setCheckedItem(R.id.navigation_bunksheets);
                setTitle(item.getTitle());
                mDrawerLayout.closeDrawers();
                break;

            case R.id.navigation_approve_bunksheets:
                fragment = ApproveBunksheetsFragment.newInstance();
                mNavigationView.setCheckedItem(R.id.navigation_approve_bunksheets);
                setTitle(item.getTitle());
                mDrawerLayout.closeDrawers();
                break;

            case R.id.navigation_approved_bunksheets:
                fragment = ApprovedBunksheetsFragment.newInstance();
                mNavigationView.setCheckedItem(R.id.navigation_approved_bunksheets);
                setTitle(item.getTitle());
                mDrawerLayout.closeDrawers();
                break;

            case R.id.navigation_profile:
                fragment = ProfileFragment.newInstance();
                mNavigationView.setCheckedItem(R.id.navigation_profile);
                setTitle(item.getTitle());
                mDrawerLayout.closeDrawers();
                break;

            case R.id.navigation_logout:
                mNavigationView.setCheckedItem(R.id.navigation_logout);
                mFirebaseAuth.signOut();
                break;

            case R.id.navigation_feedback:
                fragment = FeedbackFragment.newInstance();
                mNavigationView.setCheckedItem(R.id.navigation_feedback);
                setTitle(item.getTitle());
                mDrawerLayout.closeDrawers();
                break;

            case R.id.navigation_rate_us:
                goToGooglePlayPage();
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.frameLayout_fragments, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        detachAuthStateListener();
        if (mUserValueEventListener != null) {
            mUserDatabaseReference.removeEventListener(mUserValueEventListener);
            mUserValueEventListener = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachAuthStateListener();
    }

    @Override
    public void selectDrawerItem(int itemId) {
        selectNavigationItem(mNavigationView.getMenu().findItem(itemId));
    }

    @Override
    public void openDrawer() {
        mDrawerLayout.openDrawer(Gravity.START, true);
    }
}