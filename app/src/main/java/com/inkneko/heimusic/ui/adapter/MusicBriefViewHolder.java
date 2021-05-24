package com.inkneko.heimusic.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MusicBriefViewHolder extends RecyclerView.ViewHolder {
    private ImageView albumArtImageView;
    public TextView songNameTextView;
    private TextView songInfoTextView;
    private final View itemView;
    private final int defaultTextColor;
    private static int selectedPosition = -1;

    private MusicBriefViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        albumArtImageView = itemView.findViewById(R.id.music_brief_item_album_art);
        songNameTextView = itemView.findViewById(R.id.music_brief_item_songname);
        songInfoTextView = itemView.findViewById(R.id.music_brief_item_songinfo);
        defaultTextColor = songNameTextView.getCurrentTextColor();
    }

    void bind(MusicInfo musicInfo, OnItemClickedListener onItemClickedListener, int position) {
        if (musicInfo instanceof LocalMusicInfo){
            LocalMusicInfo localMusicInfo = (LocalMusicInfo)musicInfo;
            songNameTextView.setText(localMusicInfo.getSongName());
            songInfoTextView.setText(localMusicInfo.getAlbumName() + " - " + localMusicInfo.getArtistName());
            byte[] bitmap = localMusicInfo.getAlbumArtBytes();
            Glide.with(albumArtImageView.getContext()).asBitmap().load(bitmap).placeholder(R.drawable.default_albumart).into(albumArtImageView);
        }else{
            RemoteMusicInfo remoteMusicInfo = (RemoteMusicInfo)musicInfo;
            songNameTextView.setText(remoteMusicInfo.getSongName());
            songInfoTextView.setText(remoteMusicInfo.getAlbumName() + " - " + remoteMusicInfo.getArtistName());
            Glide.with(albumArtImageView.getContext()).load(remoteMusicInfo.getAlbumArtUrl()).placeholder(R.drawable.default_albumart).into(albumArtImageView);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickedListener.onClicked(itemView, position);
            }
        });

        if(selectedPosition == position){
            setItemViewHighLighted(true);
        }else {
            setItemViewHighLighted(false);
        }
    }

    public View getItemView(){
        return itemView;
    }

    public static void setSelectedPosition(int position){
        selectedPosition = position;
    }

    public static int  getSelectedPosition(){
        return selectedPosition;
    }

    public void setItemViewHighLighted(boolean dohighLight){
        if (dohighLight){
            songNameTextView.setTextColor(songNameTextView.getContext().getColor(R.color.colorPrimary));
            songInfoTextView.setTextColor(songNameTextView.getContext().getColor(R.color.colorPrimary));
        }else{
            songNameTextView.setTextColor(defaultTextColor);
            songInfoTextView.setTextColor(defaultTextColor);
        }
    }

    static MusicBriefViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_music_brief_item, parent, false);
        return new MusicBriefViewHolder(view);
    }

    public interface OnItemClickedListener{
        void onClicked(View view, int position);
    }

}