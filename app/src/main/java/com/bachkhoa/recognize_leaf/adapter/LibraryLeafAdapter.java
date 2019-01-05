package com.bachkhoa.recognize_leaf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bachkhoa.recognize_leaf.R;
import com.bachkhoa.recognize_leaf.activity.LibraryLeafActivity;
import com.bumptech.glide.Glide;
import com.theophrast.ui.widget.SquareImageView;

import java.util.List;

public class LibraryLeafAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Bitmap> listImage;
    private Context context;

    public List<Bitmap> getListImage() {
        return listImage;
    }

    public void setListImage(List<Bitmap> listImage) {
        this.listImage = listImage;
    }

    public LibraryLeafAdapter(Context context, List<Bitmap> listImage) {
        this.context = context;
        this.listImage = listImage;
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater;
        View view = null;
        switch (viewType) {
            case 0:
                inflater = LayoutInflater.from(parent.getContext());
                view = inflater.inflate(R.layout.item_left, parent, false);
                return new LibraryLeafAdapter.LeafHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case 0:
                final LeafHolder leafHolder = (LeafHolder) holder;
                Glide.with(context).load(listImage.get(position)).into(leafHolder.image);
                leafHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((LibraryLeafActivity) context).returnBitmapToMainActivity(position);
                    }
                });
                break;
        }
    }


    @Override
    public int getItemCount() {
        return this.listImage.size();
    }

    class LeafHolder extends RecyclerView.ViewHolder {
        private SquareImageView image;

        public LeafHolder(View itemView) {
            super(itemView);
            image = (SquareImageView) itemView.findViewById(R.id.image);
        }
    }

}


