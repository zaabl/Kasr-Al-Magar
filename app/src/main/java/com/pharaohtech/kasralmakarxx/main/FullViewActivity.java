package com.pharaohtech.kasralmakarxx.main;

import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.pharaohtech.kasralmakarxx.R;
import com.pharaohtech.kasralmakarxx.models.Comment;
import com.pharaohtech.kasralmakarxx.models.Post;
import com.pharaohtech.kasralmakarxx.models.User;
import com.pharaohtech.kasralmakarxx.utils.FirebaseMethods;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FullViewActivity extends AppCompatActivity {

    private static final String TAG = "FullViewActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseMethods mFirebaseMethods;
    private Context mContext;
    private ImageView fullImageView;
    private String postId;
    private Button commentBtn;
    private EditText commentEtxt;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        postId = intent.getStringExtra("postID");
        Log.d(TAG, "onCreate: will it work ?" + postId);
        setContentView(R.layout.activity_full_view);
        mContext = FullViewActivity.this;
        final RecyclerView recyclerView = findViewById(R.id.recyclerViewComment);
        mFirebaseMethods = new FirebaseMethods(mContext);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        commentEtxt = findViewById(R.id.fullCommentText);
        commentBtn = findViewById(R.id.fullCommentBtn);
        fullImageView = (ImageView) findViewById(R.id.commentImageView);
        getUser();
        load(recyclerView);
        addComment(recyclerView);
        FirebaseFirestore.getInstance().collection("posts").document(postId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Post openedPost = documentSnapshot.toObject(Post.class);
                        Picasso.get().load(openedPost.getThumbnail())
                                .into(fullImageView);
                    }
                });
//        Log.d(TAG, "onCreate: post thumbnail" + openedPost);

    }

    //=======================================CommentOnClick=========================================
    //==============================================================================================

    private void addComment(final RecyclerView recycler){

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        final String formattedDate = df.format(c);

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String commentText = commentEtxt.getText().toString();
                if(!commentText.equals("")){
                    FirebaseFirestore.getInstance()
                            .collection("posts")
                            .document(postId)
                            .collection("comments")
                            .add(new Comment(currentUser.getProfile_photo().toString(), currentUser.getDisplay_name().toString(), commentText, System.currentTimeMillis(), currentUser.getUser_id().toString()))
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(mContext, R.string.commentPosted, Toast.LENGTH_SHORT).show();
                                    commentEtxt.setText("");
                                    load(recycler);
                                }
                            });
                }
            }
        });

    }

    private void getUser(){
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // ...
//                post(mFirebaseMethods.getUser(dataSnapshot));
                currentUser = mFirebaseMethods.getUser(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
    }


    //---------------------------------------RecyclerView-------------------------------------------
    //==============================================================================================

    void load(final RecyclerView recycler) {
        Query query = FirebaseFirestore.getInstance()
                .collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("commentDate", Query.Direction.ASCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Comment> options = new FirestorePagingOptions.Builder<Comment>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Comment.class)
                .build();

        final FirestorePagingAdapter<Comment, ItemViewHolderFull> adapter =
                new FirestorePagingAdapter<Comment, ItemViewHolderFull>(options) {
                    @NonNull
                    @Override
                    public ItemViewHolderFull onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        return new ItemViewHolderFull(LayoutInflater
                                .from(mContext)
                                .inflate(R.layout.layout_commentitem, parent, false));
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final ItemViewHolderFull holder,
                                                    int position,
                                                    @NonNull final Comment model) {
                        holder.nameViewFull.setText(model.getName());
                        holder.captionViewFull.setText(model.getCommentText());
                        Picasso.get().load(model.getProfilePicture()).into(holder.profilePictureViewFull);
                        Log.d(TAG, "onBindViewHolder: timestamp");

                        holder.timeStampViewFull.setText(new Date(model.getCommentDate()).toString());

                        holder.captionViewFull.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                if(checkIfAuthorized(model.getUid().toString())){
                                    FirebaseFirestore.getInstance().collection("posts").document(postId).collection("comments").document(getCurrentList().get(holder.getAdapterPosition()).getId()).delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(FullViewActivity.this, R.string.commentDelted, Toast.LENGTH_SHORT).show();
                                                    load(recycler);
                                                }
                                            });
                                }
                                else{
                                    Toast.makeText(FullViewActivity.this, R.string.homeAllow, Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            }
                        });

                    }

                    @Override
                    protected void onLoadingStateChanged(@NonNull LoadingState state) {
                    }

                };

        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(FullViewActivity.this));
    }

    private boolean checkIfAuthorized(String postUsersId){

        if(currentUser.getUser_type().toString().equals("admin")){
            return true;
        }
        else if(FirebaseAuth.getInstance().getCurrentUser().getUid().toString().equals(postUsersId)){
            return true;
        }
        else{
            return false;
        }
    }
}

class ItemViewHolderFull extends RecyclerView.ViewHolder {
    private static final String TAG = "FullViewActivity";
    CircularImageView profilePictureViewFull;
    TextView nameViewFull;
    TextView captionViewFull;
    TextView timeStampViewFull;

    public ItemViewHolderFull(final View view) {
        super(view);
        profilePictureViewFull = view.findViewById(R.id.recyclerProfilePictureComment);
        nameViewFull = view.findViewById(R.id.recyclerProfileNameComment);
        captionViewFull = view.findViewById(R.id.recyclerComment);
        timeStampViewFull = view.findViewById(R.id.recyclerTimeStampComment);
    }
}
