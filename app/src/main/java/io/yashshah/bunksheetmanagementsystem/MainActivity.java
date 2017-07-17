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

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

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
    }

    private void attachAuthStateListener() {
        if (mAuthStateListener == null) {
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    if (currentUser != null) {
                        mCurrentUser = currentUser;
                        updateUIWithUserDetails();
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

    private void updateUIWithUserDetails() {
        View headerLayout = mNavigationView.getHeaderView(0);

        TextView nameTextView = (TextView) headerLayout.findViewById(R.id.textView_header_name);
        nameTextView.setText(mCurrentUser.getDisplayName());
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachAuthStateListener();
    }
}
