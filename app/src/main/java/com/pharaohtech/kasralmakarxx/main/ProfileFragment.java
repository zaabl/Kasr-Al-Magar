package com.pharaohtech.kasralmakarxx.main;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pharaohtech.kasralmakarxx.R;
import com.pharaohtech.kasralmakarxx.models.User;
import com.pharaohtech.kasralmakarxx.utils.FirebaseMethods;
import com.pharaohtech.kasralmakarxx.utils.UniversalImageLoader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.annotation.Target;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private Context mContext;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private EditText mDisplayName;
    private TextView mEmail;
    private ImageView mProfilePhoto;
    private ProgressBar mProgressbar;
    private Button mEditButton;
    private String userID;
    private TextView mChangePhotoTV;
    private ImageView mProfileBg;

    Uri pickedImgUri;

    private int PReqCode = 1;
    private int REQUESCODE = 1;



    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Toolbar
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        setupToolbar(toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //rest
        mFirebaseMethods = new FirebaseMethods(getActivity());
        mDisplayName = (EditText) view.findViewById(R.id.profileName);
        mEmail = (TextView) view.findViewById(R.id.profileEmail);
        mProfilePhoto = (ImageView) view.findViewById(R.id.profileProfilePhoto);
        mProgressbar = (ProgressBar) view.findViewById(R.id.profileProgressbar);
        mEditButton = (Button) view.findViewById(R.id.profileEditName);
        mChangePhotoTV = (TextView) view.findViewById(R.id.changePhoto);
        mProfileBg = view.findViewById(R.id.profileBg);
        Picasso.get().load(R.drawable.test2).into(mProfileBg);

        Log.d(TAG, "onCreateView: stared.");

        initImageLoader();
        setupFirebaseAuth();
        editProfile();
        onOpenGallaryClick();
    }

    //------------------------------------------Toolbar---------------------------------------------
    //==============================================================================================

    private void setupToolbar(Toolbar toolbar){
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, "onMenuItemClick: clicked menu item: " + item);

                switch (item.getItemId()){
                    case R.id.feedToolBar:
                        Log.d(TAG, "onMenuItemClick: Navigating to Profile Preferences.");
                }

                return false;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    //-----------------------------------------EditProfile------------------------------------------
    //==============================================================================================

    private void editProfile(){
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDisplayName.getText().toString();
                myRef.child("users")
                        .child(userID)
                        .child("display_name")
                        .setValue(mDisplayName.getText().toString());
                Toast.makeText(getActivity(), R.string.profileNameChanged, Toast.LENGTH_SHORT).show();

            }
        });
    }

    //-------------------------------------------GetData--------------------------------------------
    //==============================================================================================
    private void setProfileWidgets(User user){
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + user.toString());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + user.getDisplay_name());

        UniversalImageLoader.setImage(user.getProfile_photo(),mProfilePhoto,mProgressbar, "");
        mEmail.setText(user.getEmail());
        mDisplayName.setText(user.getDisplay_name());
        mProgressbar.setVisibility(View.GONE);

    }

    private  void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    //------------------------------------ChangeProfilePicture--------------------------------------
    //==============================================================================================
    private void onOpenGallaryClick(){

        mChangePhotoTV.setOnClickListener(new View.OnClickListener() {
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

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), R.string.sharePermission, Toast.LENGTH_SHORT).show();
            } else {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PReqCode);
            }

        } else {
            openGallery();
        }

    }


    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {
            pickedImgUri = data.getData();

            Log.d(TAG, "onActivityResult: uri ===== " + pickedImgUri);
            Picasso.get().load(pickedImgUri).resize(0,512).into(new com.squareup.picasso.Target() {
                @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                mProfilePhoto.setImageBitmap(bitmap);
                    Bitmap mbitmap = bitmap;
                    mFirebaseMethods.uploadNewProfilePhoto("profile", "", "", mbitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
            });
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pickedImgUri);
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), pickedImgUri);
//                mProfilePhoto.setImageBitmap(bitmap);
                Toast.makeText(getActivity(), R.string.profileUpdate, Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                Log.e(TAG, e.getMessage());
//            }

        }
    }

    //------------------------------------------FireBase--------------------------------------------
    //==============================================================================================

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in

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

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getUser: datasnapshot test test test test test test: " + mFirebaseMethods.getUser(dataSnapshot));

                setProfileWidgets(mFirebaseMethods.getUser(dataSnapshot));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
