package com.pharaohtech.kasralmakarxx.account;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pharaohtech.kasralmakarxx.R;
import com.pharaohtech.kasralmakarxx.main.HomeActivity;
import com.squareup.picasso.Picasso;

import java.util.Locale;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    //FIreBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Context mContext;
    private ProgressBar mProgressBar;
    private EditText mMail,mPassword;
    private ImageView mLoginBg;
    private TextView language;
    private String currentLanguage = "English";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        loadLocale();
        setContentView(R.layout.activity_login);

        goToReg();
        goToForget();
        mProgressBar = (ProgressBar) findViewById(R.id.logProgressBar);
        mProgressBar.setVisibility(View.GONE);
        mContext = LoginActivity.this;
        mMail = (EditText) findViewById(R.id.logMail);
        mPassword = (EditText) findViewById(R.id.logPass);
        mLoginBg = findViewById(R.id.loginBg);
//        language = findViewById(R.id.loginLanguage);
        setupFirebaseAuth();
        init();
//        changeLanguage();

        Picasso.get().load(R.drawable.bg_login2).into(mLoginBg);

    }

    private void changeLanguage(){
        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLanguage.equals("English")){
                    setLocale("ar-rSA");
                    Toast.makeText(mContext, "yo", Toast.LENGTH_SHORT).show();
                }else{
                    setLocale("en-rUS");
                }
                recreate();
            }
        });
    }

    private void setLocale(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang"," ");
        setLocale(language);
    }

    public void goToReg() {

        TextView goToRegBtn = (TextView) findViewById(R.id.goToRegBtn);
        goToRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void goToForget(){
        TextView goToForgetBtn = (TextView) findViewById(R.id.goToForget);
        goToForgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null.");

        if(string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }
    //--------------------------------------------Login---------------------------------------------
    //==============================================================================================

    private void init(){
        Button btnLogin = (Button) findViewById(R.id.logBtn);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mMail.getText().toString();
                String password = mPassword.getText().toString();

                if(isStringNull(email) && isStringNull(password)){

                    Toast.makeText(mContext,R.string.loginToastFill,Toast.LENGTH_SHORT).show();
                }else{
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        mProgressBar.setVisibility(View.GONE);
                                        if(user.isEmailVerified()){
                                            Log.d(TAG, "onComplete: success. email is verified.");
                                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }else{
                                            Toast.makeText(mContext, R.string.loginToastVerified, Toast.LENGTH_SHORT).show();
                                            mProgressBar.setVisibility(View.GONE);
                                            mAuth.signOut();
                                        }

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, R.string.loginToastFailed,
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);

                                    }

                                    // ...
                                }
                            });

                }
            }
        });
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
