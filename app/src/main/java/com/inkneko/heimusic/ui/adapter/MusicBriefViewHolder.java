package com.inkneko.heimusic.ui.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MusicBriefViewHolder extends RecyclerView.ViewHolder {
    private ImageView albumArtImageView;
    TextView songNameTextView;
    TextView songInfoTextView;
    Bitmap defaultAlbumArtBitmap;
    private final View itemView;
    private int defaultTextColor;
    public static int selectedPosition = -1;

    MusicBriefViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        albumArtImageView = itemView.findViewById(R.id.music_brief_item_album_art);
        songNameTextView = itemView.findViewById(R.id.music_brief_item_songname);
        songInfoTextView = itemView.findViewById(R.id.music_brief_item_songinfo);
        defaultAlbumArtBitmap = BitmapFactory.decodeResource(albumArtImageView.getResources(), R.drawable.default_albumart);
        defaultTextColor = songNameTextView.getCurrentTextColor();
    }

    void bind(MusicInfo musicInfo, OnItemClickedListener onItemClickedListener, int position) {
        if (musicInfo instanceof LocalMusicInfo){
            LocalMusicInfo localMusicInfo = (LocalMusicInfo)musicInfo;
            songNameTextView.setText(localMusicInfo.getSongName());
            songInfoTextView.setText(localMusicInfo.getAlbumName() + " - " + localMusicInfo.getArtistName());
            Bitmap bitmap = localMusicInfo.getAlbumArtBitmap();
            albumArtImageView.setImageBitmap(bitmap);
        }else{
            RemoteMusicInfo remoteMusicInfo = (RemoteMusicInfo)musicInfo;
            songNameTextView.setText(remoteMusicInfo.getSongName());
            songInfoTextView.setText(remoteMusicInfo.getAlbumName() + " - " + remoteMusicInfo.getArtistName());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        OkHttpClient httpClient = new OkHttpClient();
                        Request request = new Request.Builder().url(remoteMusicInfo.getAlbumArtUrl()).build();
                        Bitmap bitmap = BitmapFactory.decodeStream(httpClient.newCall(request).execute().body().byteStream());
                        albumArtImageView.setImageBitmap(bitmap);
                    }catch (IOException ignored) {
                        albumArtImageView.setImageBitmap(defaultAlbumArtBitmap);
                    }
                }
            }).start();
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

    public void setItemViewHighLighted(boolean dohighLight){
        if (dohighLight){
            songNameTextView.setTextColor(songNameTextView.getContext().getColor(R.color.colorPrimary));
            songInfoTextView.setTextColor(songNameTextView.getContext().getColor(R.color.colorPrimary));
        }else{
            songNameTextView.setTextColor(defaultTextColor);
            songInfoTextView.setTextColor(defaultTextColor);
        }
    }

    public static MusicBriefViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_music_brief_item, parent, false);
        return new MusicBriefViewHolder(view);
    }

    public interface OnItemClickedListener{
        void onClicked(View view, int position);
    }

}