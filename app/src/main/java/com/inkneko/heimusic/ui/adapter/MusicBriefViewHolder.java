package com.inkneko.heimusic.ui.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;
import com.inkneko.heimusic.storage.localmusic.LocalMusic;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MusicBriefViewHolder extends RecyclerView.ViewHolder {
    private ImageView albumArtImageView;
    public TextView songNameTextView;
    private TextView songInfoTextView;
    private CardView cardView;
    private final View itemView;
    //保存默认的文字颜色
    private final int defaultTextColor;
    //当前应当高亮的物品在列表中的索引
    private static int selectedPosition = -1;
    //当前列表显示的专辑的id。如果当前展示的列表不是正在播放的，则不进行高亮
    private static int selectedAlbumId = -1;
    //当前正在播放的专辑id
    private static int playingAlbumId = -1;

    private MusicBriefViewHolder(@NonNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        albumArtImageView = itemView.findViewById(R.id.music_brief_item_album_art);
        cardView = itemView.findViewById(R.id.music_brief_item_album_art_wrap);
        songNameTextView = itemView.findViewById(R.id.music_brief_item_songname);
        songInfoTextView = itemView.findViewById(R.id.music_brief_item_songinfo);
        defaultTextColor = songNameTextView.getCurrentTextColor();
    }

    @SuppressLint("SetTextI18n")
    void bind(MusicInfo musicInfo, OnItemClickedListener onItemClickedListener, int position) {
        if (musicInfo instanceof LocalMusicInfo){
            LocalMusicInfo localMusicInfo = (LocalMusicInfo)musicInfo;
            songNameTextView.setText(localMusicInfo.getSongName());
            songInfoTextView.setText(localMusicInfo.getAlbumName() + " - " + localMusicInfo.getArtistName());
            byte[] bitmap = localMusicInfo.getAlbumArtBytes();
            cardView.setVisibility(View.VISIBLE);
            Glide.with(albumArtImageView.getContext()).asBitmap().load(bitmap).placeholder(R.drawable.default_albumart).into(albumArtImageView);
        }else{
            //远端音乐不显示封面
            RemoteMusicInfo remoteMusicInfo = (RemoteMusicInfo)musicInfo;
            songNameTextView.setText(remoteMusicInfo.getSongName());
            songInfoTextView.setText(remoteMusicInfo.getAlbumName() + " - " + remoteMusicInfo.getArtistName());
            cardView.setVisibility(View.GONE);
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickedListener.onClicked(itemView, position);
            }
        });

        //由于共用viewholder，保存高亮位置比较复杂，因此远程端音乐不进行高亮显示
        if(selectedPosition == position && musicInfo instanceof LocalMusicInfo && playingAlbumId == selectedAlbumId){
            setItemViewHighLighted(true);
        }else {
            setItemViewHighLighted(false);
        }
    }
    //设置物品高亮
    public void setItemViewHighLighted(boolean dohighLight){
        if (dohighLight){
            songNameTextView.setTextColor(songNameTextView.getContext().getColor(R.color.colorPrimary));
            songInfoTextView.setTextColor(songNameTextView.getContext().getColor(R.color.colorPrimary));
        }else{
            songNameTextView.setTextColor(defaultTextColor);
            songInfoTextView.setTextColor(defaultTextColor);
        }
    }
    //设置应当进行高亮的物品View在列表中的索引
    public static void setSelectedPosition(int position){
        selectedPosition = position;
    }

    public static int  getSelectedPosition(){
        return selectedPosition;
    }

    public View getItemView(){
        return itemView;
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