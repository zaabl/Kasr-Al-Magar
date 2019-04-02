package com.pharaohtech.kasralmakarxx.utils;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pharaohtech.kasralmakarxx.R;

public class RecycleViewAdapterAuction extends RecyclerView.Adapter<RecycleViewAdapterAuction.ViewHolder> {

    //private List<ProductsItems> listItems;
    private int[] productImageName = {R.drawable.item1n,R.drawable.item2n,R.drawable.item3n,R.drawable.item4n,R.drawable.item5n,R.drawable.item6n};
    private String[] productName = {"اناء عربى", "خنجر عربى", "سيف عربى", "درع عربى", "خنجر عربى ذهبى", "تمثال الفارس العربى"};
    private String[] productPrice = {"75,000 ر.س","150,000 ر.س","250,000 ر.س","200,000 ر.س","450,000 ر.س","300,000 ر.س"};

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_auctionlist,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int image_id = productImageName[position];
        holder.imgviewAuction.setImageResource(image_id);
        holder.txtName.setText(productName[position]);
        holder.txtPrice.setText(productPrice[position]);
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imgviewAuction;
        public TextView txtName;
        public TextView txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgviewAuction = (ImageView) itemView.findViewById(R.id.productImage);
            txtName = (TextView) itemView.findViewById(R.id.productName);
            txtPrice = (TextView) itemView.findViewById(R.id.productPrice);
        }
    }
}
