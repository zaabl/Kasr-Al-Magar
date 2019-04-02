package com.pharaohtech.kasralmakarxx.account;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.pharaohtech.kasralmakarxx.R;
import com.pharaohtech.kasralmakarxx.account.LoginActivity;
import com.pharaohtech.kasralmakarxx.utils.FirebaseMethods;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    CircularImageView imgUserPhoto;
    private int PReqCode = 1;
    private int REQUESCODE = 1;

    Uri pickedImgUri;

    public Bitmap mbitmap;

    Context mcontext;

    private static final String TAG = "RegisterActivity";

    private Context mContext;
    private String email, password, password2,username;
    private EditText mEmail, mPassword,mpassword2,mUserName;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    private Boolean onDataChangeAuth = false;

    //FIreBase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext = RegisterActivity.this;
        firebaseMethods = new FirebaseMethods(mContext);
        ImageView mregisterBg = findViewById(R.id.registerBg);
        Picasso.get().load(R.drawable.bg_login2).into(mregisterBg);



        onImageClick();
        goTOLogin();
        initWidgets();
        setupFirebaseAuth();
        registerClick();
    }

    //-------------------------------------------Registering----------------------------------------
    //==============================================================================================

    private void registerClick(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                password = mPassword.getText().toString();
                password2 = mpassword2.getText().toString();
                username = mUserName.getText().toString();
                if(checkInputs(email, password, password2,username) && checkPasswordConfirm(password, password2) && checkProfilePhoto(pickedImgUri)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    onDataChangeAuth = true;
                    firebaseMethods.registerNewEmail(email, password);
                }
            }
        });
    }

    private boolean checkPasswordConfirm(String password, String password2){
        if(!password.equals(password2)){
            Toast.makeText(mContext, R.string.registerToastPasswordMatch, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkInputs(String email, String password, String password2, String username){
        Log.d(TAG, "checkInputs: checking inputs for null values.");
        if(email.equals("") || password.equals("") || password2.equals("")|| username.equals("")){
            Toast.makeText(mContext, R.string.registerToastEmpty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkProfilePhoto(Uri image){
        if(image == null){
            Toast.makeText(mContext, R.string.registerToastPhoto, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initWidgets(){
        Log.d(TAG, "initWidgets: Initializing Widgets.");
        mEmail = (EditText) findViewById(R.id.regMail);
        mProgressBar = (ProgressBar) findViewById(R.id.regProgressBar);
        mPassword = (EditText) findViewById(R.id.regPassword);
        mpassword2 = (EditText) findViewById(R.id.regPasswordConfirm);
        mUserName = (EditText) findViewById(R.id.regUserName);
        mContext = RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
        btnRegister = (Button) findViewById(R.id.regBtn);


    }

    //-------------------------------------------Navigation-----------------------------------------
    //==============================================================================================

    public void goTOLogin() {

        TextView goToLoginBtn = (TextView) findViewById(R.id.goToLogInBtn);
        goToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        });

    }

    //-------------------------------------------ProfilePicture-------------------------------------
    //==============================================================================================

    private void onImageClick(){
        imgUserPhoto = findViewById(R.id.regUserPhoto);

        imgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 22) {


                    checkAndRequestForPermission();


                } else {

                    openGallery();

                }


            }
        });
    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    private void checkAndRequestForPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this.getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(RegisterActivity.this, R.string.registerPermission, Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }

        } else {
            openGallery();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {

//            try {
                pickedImgUri = data.getData();
                Log.d(TAG, "onActivityResult: uri ===== " + pickedImgUri);
                Picasso.get().load(pickedImgUri).resize(0,512).into(imgUserPhoto);
//                bitmap = ((BitmapDrawable)imgUserPhoto.getDrawable()).getBitmap();
                //bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pickedImgUri);
                //imgUserPhoto.setImageBitmap(bitmap);

//            }catch (IOException ie){
//                ie.printStackTrace();
//            }

        }
    }

    //-------------------------------------------FireBase-------------------------------------------
    //==============================================================================================



    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null && onDataChangeAuth) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //firebaseMethods.addNewUser(email, username, "");
                            firebaseMethods.uploadNewProfilePhoto("register", email, username, ((BitmapDrawable) imgUserPhoto.getDrawable()).getBitmap());
                            Toast.makeText(mContext, R.string.registerToastSignup, Toast.LENGTH_SHORT).show();
                            //mAuth.signOut();
                            Log.d(TAG, "onDataChange: " + email + username);
                            onDataChangeAuth = false;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
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

