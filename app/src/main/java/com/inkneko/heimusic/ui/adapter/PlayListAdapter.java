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

public class PlayListAdapter extends ListAdapter<MusicInfo, PlayListViewHolder> {

    private PlayListViewHolder.OnItemClickedListener onItemClickedListener;

    public PlayListAdapter(@NonNull DiffUtil.ItemCallback<MusicInfo> diffCallback, PlayListViewHolder.OnItemClickedListener onItemClickedListener) {
        super(diffCallback);
        this.onItemClickedListener = onItemClickedListener;
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PlayListViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        MusicInfo musicInfo = getItem(position);
        holder.bind(musicInfo, onItemClickedListener, position);
    }

    public static class PlayListDiff extends DiffUtil.ItemCallback<MusicInfo>{
        @Override
        public boolean areItemsTheSame(@NonNull MusicInfo oldItem, @NonNull MusicInfo newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MusicInfo oldItem, @NonNull MusicInfo newItem) {
            return oldItem.equals(newItem);
        }
    }


}