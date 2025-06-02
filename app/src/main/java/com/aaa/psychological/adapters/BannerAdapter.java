package com.aaa.psychological.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aaa.psychological.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private List<Integer> bannerList;

    public BannerAdapter(List<Integer> banners) {
        this.bannerList = banners;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        holder.imgBanner.setImageResource(bannerList.get(position));
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}

