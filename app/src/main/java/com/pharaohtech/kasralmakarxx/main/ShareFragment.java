package com.pharaohtech.kasralmakarxx.main;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pharaohtech.kasralmakarxx.R;
import com.pharaohtech.kasralmakarxx.models.Post;
import com.pharaohtech.kasralmakarxx.models.User;
import com.pharaohtech.kasralmakarxx.utils.FirebaseMethods;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareFragment extends Fragment {

    private static final String TAG = "ShareActivity";

    //constants
    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    private int PReqCode = 1;
    private int REQUESCODE = 1;
    private static final int  CAMERA_REQUEST_CODE = 5;

    Uri pickedImgUri;

    private ImageView mgalleryImageView;
    private Button mBtnGallery;
    private Button mBtnPhoto;
    private ImageView mClose;
    private TextView mPost;
    private EditText mCaption;
    private String imgUrl;

    private FragmentActivity myContext;
    private FeedFragment homeFragment;

    private Context mContext = getContext();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private String userID;

    private String mAppend = "file:/";

    private int imageCount = 0;

    private StorageReference mStorageReference;

    private boolean clicked;




    public ShareFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        clicked = false;
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mgalleryImageView = (ImageView) view.findViewById(R.id.galleryImageView);
        mBtnGallery = (Button) view.findViewById(R.id.btnOpenGallary);
        mPost = (TextView) view.findViewById(R.id.postTv);
        mCaption = (EditText) view.findViewById(R.id.postCaption);
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseMethods = new FirebaseMethods(getContext());
        setupFirebaseAuth();
        homeFragment = new FeedFragment();


        if (checkPermissionsArray(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
        })) {

        } else {
            verifyPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            });
        }

        onOpenGallaryClick();
//        post();
    }

    //-------------------------------------------Posting--------------------------------------------
    //==============================================================================================
    private void post(final User user){

        mPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clicked) {
                    String caption = mCaption.getText().toString();

                    if(!caption.equals("")){
                        clicked = true;
                    Toast.makeText(getActivity(), R.string.shareUploading, Toast.LENGTH_SHORT).show();
                    uploadNewPhoto(user, caption, ((BitmapDrawable) mgalleryImageView.getDrawable()).getBitmap());
                    }else{
                        Toast.makeText(getActivity(), R.string.shareFill, Toast.LENGTH_SHORT).show();
                    }
                } }
        });
    }

    public void uploadNewPhoto(User user, final String caption, final Bitmap image) {
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference storageReference = mStorageReference
                .child("posts/" + user_id + "/" + image.hashCode());

        final String mDisplay_name = user.getDisplay_name().toString();
        final String mProfilePhoto = user.getProfile_photo().toString();

        //convert bitmap to byteArray

        byte[] bytes = getBytesFromBitmap(image, 75);

        storageReference.putBytes(bytes).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    addPhotoToDatabase(mDisplay_name, mProfilePhoto, caption, downloadUri.toString());
                    Toast.makeText(getActivity(), R.string.shareSuccesss, Toast.LENGTH_SHORT).show();
                    mCaption.setText("");
                    setFragment(homeFragment);
                }
            }
        });

    }

    private void addPhotoToDatabase(final String mDisplay_name, final String mProfilePhoto, final String caption, final String url) {
        Log.e(TAG, "addPhotoToDatabase: adding photo to database.");
        FirebaseFirestore.getInstance().collection("posts").add(new Post(
                mDisplay_name,
                mProfilePhoto,
                url,
                caption,
                mAuth.getCurrentUser().getUid(),
                0,
                0,
                System.currentTimeMillis()
        ));
    }

    public static byte[] getBytesFromBitmap(Bitmap bm, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }
    //------------------------------------------Navigation------------------------------------------
    //==============================================================================================

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = myContext.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();

    }

    //-----------------------------------------PickThumbnail----------------------------------------
    //==============================================================================================
    private void onOpenGallaryClick(){

        mBtnGallery.setOnClickListener(new View.OnClickListener() {
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

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getActivity(), R.string.sharePermission, Toast.LENGTH_SHORT).show();
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
            Log.d(TAG, "onActivityResult: " + pickedImgUri);

            mgalleryImageView.setImageURI(pickedImgUri);

//            Picasso.get().load(pickedImgUri).resize(0,1024).into(mgalleryImageView);
//            Picasso.get()
//                    .load(pickedImgUri.parse(getRightAngleImage(pickedImgUri.toString()))) // web image url
//                    .fit().centerInside()
//                    .into(mgalleryImageView);

//            try {
//
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), pickedImgUri);
//                mgalleryImageView.setImageBitmap(bitmap);
//            }catch (IOException ie){
//                ie.printStackTrace();
//            }

        }
    }

//    private String getRightAngleImage(String photoPath) {
//
//        try {
//            ExifInterface ei = new ExifInterface(photoPath);
//            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//            int degree = 0;
//
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_NORMAL:
//                    degree = 0;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    degree = 90;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    degree = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    degree = 270;
//                    break;
//                case ExifInterface.ORIENTATION_UNDEFINED:
//                    degree = 0;
//                    break;
//                default:
//                    degree = 90;
//            }
//
//            return rotateImage(degree,photoPath);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return photoPath;
//    }
//
//    private String rotateImage(int degree, String imagePath){
//
//        if(degree<=0){
//            return imagePath;
//        }
//        try{
//            Bitmap b= BitmapFactory.decodeFile(imagePath);
//
//            Matrix matrix = new Matrix();
//            if(b.getWidth()>b.getHeight()){
//                matrix.setRotate(degree);
//                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
//                        matrix, true);
//            }
//
//            FileOutputStream fOut = new FileOutputStream(imagePath);
//            String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
//            String imageType = imageName.substring(imageName.lastIndexOf(".") + 1);
//
//            FileOutputStream out = new FileOutputStream(imagePath);
//            if (imageType.equalsIgnoreCase("png")) {
//                b.compress(Bitmap.CompressFormat.PNG, 100, out);
//            }else if (imageType.equalsIgnoreCase("jpeg")|| imageType.equalsIgnoreCase("jpg")) {
//                b.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            }
//            fOut.flush();
//            fOut.close();
//
//            b.recycle();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return imagePath;
//    }

    //-------------------------------------------Verifying------------------------------------------
    //==============================================================================================
    public void verifyPermissions(String[] permissions){
        Log.d(TAG, "verifyPermissions: verifying permissions.");

        requestPermissions(
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    /**
     * Check an array of permissions
     * @param permissions
     * @return
     */
    public boolean checkPermissionsArray(String[] permissions){
        Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for(int i = 0; i< permissions.length; i++){
            String check = permissions[i];
            if(!checkPermissions(check)){
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission is it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission){
        Log.d(TAG, "checkPermissions: checking permission: " + permission);

        int permissionRequest = ContextCompat.checkSelfPermission(getActivity(), permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        }
        else{
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    //-------------------------------------------FireBase-------------------------------------------
    //==============================================================================================

    private void setupFirebaseAuth() {
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


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // ...
                post(mFirebaseMethods.getUser(dataSnapshot));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
    }

        @Override
        public void onStart () {
            super.onStart();
            mAuth.addAuthStateListener(mAuthListener);
        }

        @Override
        public void onStop () {
            super.onStop();
            if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
        }

    }