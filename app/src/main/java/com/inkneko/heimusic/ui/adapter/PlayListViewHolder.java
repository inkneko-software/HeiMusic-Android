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

import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class PlayListViewHolder extends RecyclerView.ViewHolder {
    TextView songNameTextView;
    TextView songInfoTextView;
    private final View itemView;
    private int defaultTextColor;
    public static int selectedPosition = -1;

    PlayListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        songNameTextView = itemView.findViewById(R.id.playlist_item_songname);
        songInfoTextView = itemView.findViewById(R.id.playlist_item_songinfo);
        defaultTextColor = songNameTextView.getCurrentTextColor();
    }

    void bind(MusicInfo musicInfo, OnItemClickedListener onItemClickedListener, int position) {
        if (musicInfo instanceof LocalMusicInfo){
            LocalMusicInfo localMusicInfo = (LocalMusicInfo)musicInfo;
            songNameTextView.setText(localMusicInfo.getSongName());
            songInfoTextView.setText(localMusicInfo.getAlbumName() + " - " + localMusicInfo.getArtistName());
            Bitmap bitmap = localMusicInfo.getAlbumArtBitmap();
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
                    }catch (IOException ignored) {
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

    public static PlayListViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_playlist_item, parent, false);
        return new PlayListViewHolder(view);
    }

    public interface OnItemClickedListener{
        void onClicked(View view, int position);
    }

}