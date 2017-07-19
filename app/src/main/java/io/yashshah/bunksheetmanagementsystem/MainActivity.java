package io.yashshah.bunksheetmanagementsystem;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mUserValueEventListener;

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
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
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
        mDatabaseReference = mDatabase.getReference().child("Users").child(mCurrentUser.getUid());
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
        mDatabaseReference.addListenerForSingleValueEvent(mUserValueEventListener);
    }

    private void updateNavigationDrawer(String name, String year, String division, String rollNo,
                                        int privilegeLevel) {
        View view = mNavigationView.getHeaderView(0);
        TextView nameTextView = (TextView) view.findViewById(R.id.textView_header_name);
        TextView yearDivisionTextView =
                (TextView) view.findViewById(R.id.textView_header_year_division);
        TextView rollNumberTextView = (TextView) view.findViewById(R.id.textView_header_rollNumber);

        StringBuilder yearDivisionStringBuilder = new StringBuilder();
        if (year.equals("2")) {
            yearDivisionStringBuilder.append(getString(R.string.year_se));
        } else if (year.equals("3")) {
            yearDivisionStringBuilder.append(getString(R.string.year_te));
        }
        yearDivisionStringBuilder.append(" - ");
        yearDivisionStringBuilder.append(division);

        nameTextView.setText(name);
        yearDivisionTextView.setText(yearDivisionStringBuilder.toString());
        rollNumberTextView.setText(rollNo);

        if (privilegeLevel > User.PRIVILEGE_STUDENT) {
            mNavigationView
                    .getMenu()
                    .findItem(R.id.navigation_approve_bunksheets)
                    .setVisible(true);
        } else {
            mNavigationView
                    .getMenu()
                    .findItem(R.id.navigation_approve_bunksheets)
                    .setVisible(false);
        }
    }

    private void selectNavigationItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_bunksheets:
                Toast.makeText(MainActivity.this, R.string.bunksheets, Toast.LENGTH_SHORT)
                        .show();
                setTitle(item.getTitle());
                mDrawerLayout.closeDrawers();
                break;

            case R.id.navigation_approve_bunksheets:
                Toast.makeText(MainActivity.this, R.string.approve_bunksheets,
                        Toast.LENGTH_SHORT)
                        .show();
                setTitle(item.getTitle());
                mDrawerLayout.closeDrawers();
                break;

            case R.id.navigation_profile:
                Toast.makeText(MainActivity.this, R.string.profile, Toast.LENGTH_SHORT)
                        .show();
                setTitle(item.getTitle());
                mDrawerLayout.closeDrawers();
                break;

            case R.id.navigation_logout:
                mFirebaseAuth.signOut();
                break;

            case R.id.navigation_feedback:
                Toast.makeText(MainActivity.this, R.string.feedback, Toast.LENGTH_SHORT)
                        .show();
                setTitle(item.getTitle());
                mDrawerLayout.closeDrawers();
                break;
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
            mDatabaseReference.removeEventListener(mUserValueEventListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachAuthStateListener();
    }
}
