package com.inkneko.heimusic.ui.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.MusicInfo;

import okhttp3.OkHttpClient;

public class MusicBriefAdapter extends ListAdapter<MusicInfo, MusicBriefViewHolder> {

    private MusicBriefViewHolder.OnItemClickedListener onItemClickedListener;

    public MusicBriefAdapter(@NonNull DiffUtil.ItemCallback<MusicInfo> diffCallback, MusicBriefViewHolder.OnItemClickedListener onItemClickedListener) {
        super(diffCallback);
        this.onItemClickedListener = onItemClickedListener;
    }

    @NonNull
    @Override
    public MusicBriefViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MusicBriefViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicBriefViewHolder holder, int position) {
        MusicInfo musicInfo = getItem(position);
        holder.bind(musicInfo, onItemClickedListener, position);
    }

    public static class MusicBriefDiff extends DiffUtil.ItemCallback<MusicInfo>{
        @Override
        public boolean areItemsTheSame(@NonNull MusicInfo oldItem, @NonNull MusicInfo newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull MusicInfo oldItem, @NonNull MusicInfo newItem) {
            return oldItem.equals(newItem);
        }
    }
}