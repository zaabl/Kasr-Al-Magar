package com.pharaohtech.kasralmakarxx.main;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pharaohtech.kasralmakarxx.R;
import com.pharaohtech.kasralmakarxx.account.LoginActivity;
import com.squareup.picasso.Picasso;
import io.fabric.sdk.android.Fabric;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private BottomNavigationView mMainNav;

    private static final int ACTIVITY_NUM = 0;

    private FeedFragment homeFragment;
    private StoreFragment auctionFragment;
    private ShareFragment shareFragment;
    private LikesFragment likesFragment;
    private ProfileFragment profileFragment;
    private InformationFragment informationFragment;

    private Context mContext = HomeActivity.this;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_home);
        mMainNav = (BottomNavigationView) findViewById(R.id.bottomNavViewBar);

        homeFragment = new FeedFragment();
        auctionFragment = new StoreFragment();
        shareFragment = new ShareFragment();
        likesFragment = new LikesFragment();
        profileFragment = new ProfileFragment();
        informationFragment = new InformationFragment();

        navigate();
        setupFirebaseAuth();
    }

    //---------------------------------------ButtonNavigation--------------------------------------
    //==============================================================================================

    private  void navigate(){
        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
         @Override
         public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                 switch (menuItem.getItemId()){

                     case R.id.navHome :
                         setFragment(homeFragment);
                         return true;
                     case R.id.navAuction:
                         setFragment(auctionFragment);
                         return true;
                     case R.id.navAdd:
                         setFragment(shareFragment);
                         return true;
                     case R.id.navLikes:
                         setFragment(informationFragment);
                         return true;
                     case R.id.navProfile:
                         setFragment(profileFragment);
                         return true;
                     default:
                         return false;

                 }
             }
         }


        );
        mMainNav.setSelectedItemId(R.id.navHome);
    }

    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();

    }

    //-------------------------------------------SignOut--------------------------------------------
    //==============================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.signoutMenu)
        {
            mAuth.signOut();
            Toast.makeText(this,R.string.mainSignout,Toast.LENGTH_SHORT).show();

        }else if(id==R.id.savedMenu)
        {
            setFragment(likesFragment);
        }
        return super.onOptionsItemSelected(item);
    }

    //-------------------------------------------FireBase-------------------------------------------
    //==============================================================================================

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void checkCurrentUser(FirebaseUser user){
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if(user == null) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else if(!user.isEmailVerified()){
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
            finish();
            }
    }
}
