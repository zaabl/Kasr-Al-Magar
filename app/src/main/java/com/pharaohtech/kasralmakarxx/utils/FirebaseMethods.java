package com.pharaohtech.kasralmakarxx.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pharaohtech.kasralmakarxx.main.ProfileFragment;
import com.pharaohtech.kasralmakarxx.models.User;
import com.pharaohtech.kasralmakarxx.R;

import java.io.ByteArrayOutputStream;


public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String userID;
    private StorageReference mStorageReference;

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";

    private double mPhotoUploadProgress = 0;

    private Context mContext;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mContext = context;

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "FirebaseMethods:  working " + userID);
        }

        Log.d(TAG, "FirebaseMethods: Maybe Not Working " + userID);

    }

    //-------------------------------------------Registration---------------------------------------
    //==============================================================================================

    public void registerNewEmail(final String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, "Please re-check your data",
                                    Toast.LENGTH_SHORT).show();


                        } else if (task.isSuccessful()) {
                            sendVerificationEmail();
                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authstate changed: " + userID);
                        }

                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(mContext, "couldn't send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void addNewUser(String email, String display_name, String profile_photo){

        Log.d(TAG, "addNewUser: here" + userID);
        User user = new User(userID, email, display_name, profile_photo, "user");

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);
        mAuth.signOut();
    }

    //------------------------------------UploadingProfilePhoto-------------------------------------
    //==============================================================================================

    public void uploadNewProfilePhoto(final String state, final String email, final String display_name, Bitmap image) {
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference storageReference = mStorageReference
                .child(FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo" + "/" + System.currentTimeMillis());
        byte[] bytes = getBytesFromBitmap(image, 75);

        storageReference.putBytes(bytes).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUri = task.getResult();

                if(state.equals("profile")) {
                    setProfilePhoto(downloadUri.toString());
                }else if(state.equals("register")){
                    addNewUser(email, display_name, downloadUri.toString());
                }
            }
        });
    }


    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private static byte[] getBytesFromBitmap(Bitmap bm, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Log.d(TAG, "getBytesFromBitmap: == bm === " + bm);
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    //-------------------------------------------GettingUserData------------------------------------
    //==============================================================================================

    public User getUser(DataSnapshot dataSnapshot) {
        User user = new User();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {

                Log.d(TAG, "getUser: datasnapshot: " + ds);
                Log.d(TAG, "getUser: userID                       " + userID);

                user.setDisplay_name(
                        ds.child(userID).getValue(User.class).getDisplay_name());
                user.setEmail(
                        ds.child(userID).getValue(User.class).getEmail());
                user.setProfile_photo(
                        ds.child(userID).getValue(User.class).getProfile_photo());
                user.setUser_id(
                        ds.child(userID).getValue(User.class).getUser_id());
                user.setUser_type(
                        ds.child(userID).getValue(User.class).getUser_type());

                Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString());

            }
        }

        return user;
    }

    public User getUser(DataSnapshot dataSnapshot, String userId) {
        User user = new User();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))) {

                Log.d(TAG, "getUser: there userId" + userId);

                user.setDisplay_name(
                        ds.child(userId).getValue(User.class).getDisplay_name());
                user.setEmail(
                        ds.child(userId).getValue(User.class).getEmail());
                user.setProfile_photo(
                        ds.child(userId).getValue(User.class).getProfile_photo());
                user.setUser_id(
                        ds.child(userId).getValue(User.class).getUser_id());
                user.setUser_type(
                        ds.child(userId).getValue(User.class).getUser_type());

                Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString());

            }
        }

        Log.d(TAG, "getUser: there shit " + user);
        return user;
    }


}