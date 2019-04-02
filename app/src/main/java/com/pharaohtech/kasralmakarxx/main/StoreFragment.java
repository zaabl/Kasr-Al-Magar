package com.pharaohtech.kasralmakarxx.main;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.pharaohtech.kasralmakarxx.R;
import com.pharaohtech.kasralmakarxx.utils.RecycleViewAdapterAuction;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class StoreFragment extends Fragment {

    private ImageView mHomeBg;
    private Button mbuyBtn;
    private static final String TAG = "StoreFragment";


    public StoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_store, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Toolbar
        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.storeToolBar);
        setupToolbar(toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        //rest
        mHomeBg = view.findViewById(R.id.storeBg);
        mbuyBtn = view.findViewById(R.id.productBuyButton);
        Picasso.get().load(R.drawable.bg_login).into(mHomeBg);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewAuction);
        RecycleViewAdapterAuction adapter = new RecycleViewAdapterAuction();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

//        buyButton();
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

    //-----------------------------------------BuyButton--------------------------------------------
    //==============================================================================================



//    private void buyButton(){
//        mbuyBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(getActivity(), "This item is currently out of storage", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
}
