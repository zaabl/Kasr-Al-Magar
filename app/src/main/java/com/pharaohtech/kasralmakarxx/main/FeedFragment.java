package com.pharaohtech.kasralmakarxx.main;


import android.app.Activity;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.pharaohtech.kasralmakarxx.R;
import com.pharaohtech.kasralmakarxx.models.Post;
import com.pharaohtech.kasralmakarxx.models.User;
import com.pharaohtech.kasralmakarxx.utils.FirebaseMethods;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    private static final String TAG = "FeedFragment";
    private ImageView mHomeBg;
    protected FragmentActivity mActivity;
    private FirebaseFirestore  mFireStore;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseMethods mFirebaseMethods;
    private StorageReference mStorageReference;

    private User postUser;
    private User currentUser;
    private Context mContext;




    public FeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
//        View rootView = inflater.inflate(R.layout.snippet_top_feed, container, false);
//        //YOUR STUFF
//        return rootView;

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerViewHome);
        final SwipeRefreshLayout swipeLayout = view.findViewById(R.id.refreshLayout);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mFirebaseMethods = new FirebaseMethods(getContext());
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = getContext();

        refresh(recyclerView, swipeLayout);
        ((SwipeRefreshLayout) view.findViewById(R.id.refreshLayout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(recyclerView, swipeLayout);
            }
        });

        mHomeBg = view.findViewById(R.id.homeBg);
        Picasso.get().load(R.drawable.bg_login).into(mHomeBg);
        //Toolbar
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.feedToolBar);
        setupToolbar(toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
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

    //----------------------------------------RecyclerView------------------------------------------
    //==============================================================================================

    void refresh(final RecyclerView recycler, final SwipeRefreshLayout swipe) {
        Query query = FirebaseFirestore.getInstance()
                .collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Post> options = new FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Post.class)
                .build();

        final FirestorePagingAdapter<Post, ItemViewHolder> adapter =
        new FirestorePagingAdapter<Post, ItemViewHolder>(options) {
            HashMap<Integer, Boolean> likeChanges = new HashMap<>(0);

            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.layout_listitem, parent, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final ItemViewHolder holder,
                                            int position,
                                            @NonNull final Post model) {
                getUser();
//                getUser(model.getUid().toString());
                holder.nameView.setText(model.getDisplayName());
                holder.captionView.setText(model.getCaption());
                Picasso.get().load(model.getThumbnail()).into(holder.thumbnailView);
                Picasso.get().load(model.getProfilePhoto()).into(holder.profilePictureView);
                holder.timeStampView.setText(new Date(model.getTimestamp()).toString());

                // TODO: Set model.likeCount as score.
                holder.likeCount.setText("" + model.getLikeCount());
                Boolean likeState = likeChanges.get(holder.getAdapterPosition());
                if(likeState != null) {
                    if(likeState) {
                        holder.likeButton.setLiked(true);
                    } else {
                        holder.likeButton.setLiked(false);
                    }
                } else {
                    FirebaseFirestore.getInstance().collection("posts").document(getCurrentList().get(holder.getAdapterPosition()).getId())
                            .collection("likes").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                holder.likeButton.setLiked(true);
                                likeChanges.put(holder.getAdapterPosition(), true);
                            } else {
                                holder.likeButton.setLiked(false);
                                likeChanges.put(holder.getAdapterPosition(), false);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            holder.likeButton.setLiked(false);
                            likeChanges.put(holder.getAdapterPosition(), false);
                        }
                    });
                }

    //====================================LikeOnClickListener=======================================

                holder.likeButton.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        Map<String, Object> map = new HashMap<>(1);
                        map.put("likes", true);
                        FirebaseFirestore.getInstance().collection("posts").document(getCurrentList().get(holder.getAdapterPosition()).getId()).collection("likes").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Long>() {
                                    @Nullable
                                    @Override
                                    public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                        DocumentSnapshot snapshot  = transaction.get(FirebaseFirestore.getInstance().collection("posts").document(getCurrentList().get(holder.getAdapterPosition()).getId()));
                                        if(snapshot.contains("likeCount")) {
                                            long newCount = snapshot.getLong("likeCount") + 1;
                                            transaction.update(FirebaseFirestore.getInstance().collection("posts").document(getCurrentList().get(holder.getAdapterPosition()).getId()), "likeCount", newCount);
                                            return newCount;
                                        }
                                        return 0L;
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Long>() {
                                    @Override
                                    public void onSuccess(Long newCount) {
                                        likeChanges.put(holder.getAdapterPosition(), true);
                                        // TODO: Update score.
                                        holder.likeCount.setText(newCount.toString());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        holder.likeButton.setLiked(false);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                holder.likeButton.setLiked(false);
                            }
                        });
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        FirebaseFirestore.getInstance().collection("posts").document(getCurrentList().get(holder.getAdapterPosition()).getId()).collection("likes").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Long>() {
                                    @Nullable
                                    @Override
                                    public Long apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                        DocumentSnapshot snapshot  = transaction.get(FirebaseFirestore.getInstance().collection("posts").document(getCurrentList().get(holder.getAdapterPosition()).getId()));
                                        if(snapshot.contains("likeCount")) {
                                            long newCount = snapshot.getLong("likeCount") - 1;
                                            transaction.update(FirebaseFirestore.getInstance().collection("posts").document(getCurrentList().get(holder.getAdapterPosition()).getId()), "likeCount", newCount);
                                            return newCount;
                                        }
                                        return 0L;
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Long>() {
                                    @Override
                                    public void onSuccess(Long newCount) {
                                        likeChanges.put(holder.getAdapterPosition(), false);
                                        // TODO: Update score.
                                        holder.likeCount.setText(newCount.toString());
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        holder.likeButton.setLiked(true);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                holder.likeButton.setLiked(true);
                            }
                        });
                    }
                });
    //===================================SettingsOnClickListener====================================

            holder.moreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(holder.moreView.getContext(),view);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Log.d(TAG, "onMenuItemClick: " + currentUser);
                            switch(menuItem.getItemId()){

                                case R.id.feedSave:
                                    saveToDatabase(model.getDisplayName(), model.getProfilePhoto(), model.getCaption(), model.getThumbnail(), model.getUid(), model.getTimestamp());
                                    return true;

                                case R.id.feedReport:
                                    String postId = getCurrentList().get(holder.getAdapterPosition()).getId().toString();
                                    reportToDatabase(model.getDisplayName(), model.getProfilePhoto(), model.getCaption(), model.getThumbnail(), postId, model.getTimestamp());
                                    return true;
                                case R.id.feedDelete:
                                    if(checkIfAuthorized(model.getUid().toString())){
                                    FirebaseFirestore.getInstance().collection("posts").document(getCurrentList().get(holder.getAdapterPosition()).getId()).delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getActivity(), R.string.homeDeleted, Toast.LENGTH_SHORT).show();
                                                    refresh(recycler,swipe);
                                                }
                                            });
                                    }
                                    else{
                                        Toast.makeText(getActivity(), R.string.homeAllow, Toast.LENGTH_SHORT).show();
                                    }
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    Log.d(TAG, "onClick: Button clicked");
                    popup.inflate(R.menu.more_settings);
                    popup.show();
                }
            });

        //====================================CommentOnClickListener================================

                holder.commentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String postId = getCurrentList().get(holder.getAdapterPosition()).getId().toString();
                        Intent intent = new Intent(getActivity(), FullViewActivity.class);
                        intent.putExtra("postID", postId);
                        startActivity(intent);
                    }
                });

            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                        swipe.setRefreshing(true);
                        break;
                    case LOADED:
                        swipe.setRefreshing(false);
                        break;
                    case ERROR:
                        swipe.setRefreshing(false);
                        break;
                }
            }
        };

        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
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

    private void getUser(){

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // ...
                currentUser = mFirebaseMethods.getUser(dataSnapshot);
                Log.d(TAG, "onDataChange: current user" + currentUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
                Log.d(TAG, "onDataChange: current user" + currentUser);
            }
        });
    }

    private void getUser(final String userId){

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // ...
                postUser = mFirebaseMethods.getUser(dataSnapshot, userId);
                Log.d(TAG, "onDataChange: postuser shit" + postUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });

        Log.d(TAG, "onDataChange: postuser shit test" + postUser);

    }

    private void saveToDatabase(final String mDisplay_name, final String mProfilePhoto, final String caption, final String url, String userId, Long time) {
        Log.e(TAG, "addPhotoToDatabase: adding photo to database.");
        FirebaseFirestore.getInstance().collection("saved").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).collection("posts").add(new Post(
                mDisplay_name,
                mProfilePhoto,
                url,
                caption,
                userId,
                0,
                0,
                time
        )).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getActivity(), R.string.homeSaved, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reportToDatabase(final String mDisplay_name, final String mProfilePhoto, final String caption, final String url, String userId, Long time) {
        Log.e(TAG, "addPhotoToDatabase: adding photo to database.");
        FirebaseFirestore.getInstance().collection("reported").add(new Post(
                mDisplay_name,
                mProfilePhoto,
                url,
                caption,
                userId,
                0,
                0,
                time
        )).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getActivity(), R.string.homeReported, Toast.LENGTH_SHORT).show();
            }
        });
    }

}



class ItemViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "FeedFragment";
    CircularImageView profilePictureView;
    TextView nameView;
    TextView captionView;
    ImageView thumbnailView;
    TextView timeStampView;
    ImageButton moreView;
    com.like.LikeButton likeButton;
    TextView likeCount;
    ImageButton commentButton;

    FeedFragment feedFragment = new FeedFragment();

    public ItemViewHolder(final View view) {
        super(view);
        profilePictureView = view.findViewById(R.id.recyclerProfilePicture);
        nameView = view.findViewById(R.id.recyclerProfileName);
        captionView = view.findViewById(R.id.recyclerProfileComment);
        thumbnailView = view.findViewById(R.id.recyclerThumbnail);
        timeStampView = view.findViewById(R.id.recyclerTimeStamp);
        moreView = view.findViewById(R.id.feedMore);
        likeButton = view.findViewById(R.id.recyclerLikeBtn);
        likeCount = view.findViewById(R.id.recyclerLikeCount);
        commentButton = view.findViewById(R.id.recyclerCommentBtn);
    }
}

