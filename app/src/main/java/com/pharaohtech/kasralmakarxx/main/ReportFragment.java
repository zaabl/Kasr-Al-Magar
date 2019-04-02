package com.pharaohtech.kasralmakarxx.main;


import android.arch.paging.PagedList;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.pharaohtech.kasralmakarxx.R;
import com.pharaohtech.kasralmakarxx.models.Post;
import com.pharaohtech.kasralmakarxx.utils.FirebaseMethods;
import com.squareup.picasso.Picasso;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment {

    private static final String TAG = "ReportFragment";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseMethods mFirebaseMethods;
    private FragmentActivity myContext;
    private ReportFragment reportFragment;

    public ReportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerViewReports);
        final SwipeRefreshLayout swipeLayout = view.findViewById(R.id.reportRefreshLayout);
        mFirebaseMethods = new FirebaseMethods(getContext());
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        refresh(recyclerView,swipeLayout);
        ((SwipeRefreshLayout) view.findViewById(R.id.reportRefreshLayout)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(recyclerView, swipeLayout);
            }
        });

        //Toolbar
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.reportedToolBar);
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

    //---------------------------------------RecyclerView-------------------------------------------
    //==============================================================================================

    void refresh(final RecyclerView recycler, final SwipeRefreshLayout swipe) {
        Query query = FirebaseFirestore.getInstance()
                .collection("reported")
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

        FirestorePagingAdapter<Post, ItemViewHolderReport> adapter =
                new FirestorePagingAdapter<Post, ItemViewHolderReport>(options) {
                    @NonNull
                    @Override
                    public ItemViewHolderReport onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        return new ItemViewHolderReport(LayoutInflater.from(getContext()).inflate(R.layout.layout_saveditem, parent, false));
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final ItemViewHolderReport holder,
                                                    int position,
                                                    @NonNull final Post model) {
                        holder.nameView.setText(model.getDisplayName());
                        holder.captionView.setText(model.getCaption());
                        Picasso.get().load(model.getThumbnail()).into(holder.thumbnailView);
                        Picasso.get().load(model.getProfilePhoto()).into(holder.profilePictureView);
                        holder.timeStampView.setText(new Date(model.getTimestamp()).toString());


                        //===================================SettingsOnClickListener====================================

                        holder.moreView.setOnClickListener(new View.OnClickListener()

                        {
                            @Override
                            public void onClick (View view){
                                PopupMenu popup = new PopupMenu(holder.moreView.getContext(), view);

                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        switch (menuItem.getItemId()) {

                                            case R.id.reportDelete:
                                                FirebaseFirestore.getInstance().collection("reported").document(getCurrentList().get(holder.getAdapterPosition()).getId()).delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(getActivity(), R.string.reportDeleted, Toast.LENGTH_SHORT).show();
                                                                FirebaseFirestore.getInstance().collection("posts").document(model.getUid()).delete();
                                                                refresh(recycler, swipe);
                                                            }
                                                        });
                                                return true;

                                            case R.id.reportIgnore:
                                                FirebaseFirestore.getInstance().collection("reported").document(getCurrentList().get(holder.getAdapterPosition()).getId()).delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(getActivity(), R.string.reportIgnored, Toast.LENGTH_SHORT).show();
                                                                refresh(recycler, swipe);
                                                            }
                                                        });
                                            default:
                                                return false;
                                        }
                                    }
                                });
                                Log.d(TAG, "onClick: Button clicked");
                                popup.inflate(R.menu.more_settings_report);
                                popup.show();
                            }
                        });
                    }

                    @Override
                    protected void onLoadingStateChanged(@NonNull LoadingState state) {
                        switch (state) {
                            case LOADING_INITIAL:
                                swipe.setRefreshing(true);
                            case LOADED:
                                swipe.setRefreshing(false);
                            case ERROR:
                                swipe.setRefreshing(false);
                        }
                    }
                };

        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

}

class ItemViewHolderReport extends RecyclerView.ViewHolder {
    CircularImageView profilePictureView;
    TextView nameView;
    TextView captionView;
    ImageView thumbnailView;
    TextView timeStampView;
    ImageButton moreView;

    public ItemViewHolderReport(View view) {
        super(view);
        profilePictureView = view.findViewById(R.id.srecyclerProfilePicture);
        nameView = view.findViewById(R.id.srecyclerProfileName);
        captionView = view.findViewById(R.id.srecyclerProfileComment);
        thumbnailView = view.findViewById(R.id.srecyclerThumbnail);
        timeStampView = view.findViewById(R.id.srecyclerTimeStamp);
        moreView = view.findViewById(R.id.sfeedMore);
    }
}
