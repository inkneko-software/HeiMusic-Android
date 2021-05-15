package com.inkneko.heimusic.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inkneko.heimusic.MainActivity;
import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MusicBriefAdapter extends BaseAdapter {

    private OkHttpClient httpClient;
    private Context context;
    List<MusicInfo> musicInfoList;
    private Bitmap defaultAlbumArt;

    public MusicBriefAdapter(Context context, List<MusicInfo> musicInfoList){
        this.context = context;
        this.musicInfoList = musicInfoList;
        httpClient = new OkHttpClient();
        defaultAlbumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_albumart);
    }
    @Override
    public int getCount() {
        return musicInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return musicInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        //TODO: 返回unique id以进行性能优化
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_music_brief_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.albumArt = convertView.findViewById(R.id.music_brief_item_album_art);
            viewHolder.songname = convertView.findViewById(R.id.music_brief_item_songname);
            viewHolder.songinfo = convertView.findViewById(R.id.music_brief_item_songinfo);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        MusicInfo musicInfo = musicInfoList.get(position);
        if (musicInfo instanceof LocalMusicInfo){
            LocalMusicInfo localMusicInfo = (LocalMusicInfo)musicInfo;
            viewHolder.songname.setText(localMusicInfo.getSongName());
            viewHolder.songinfo.setText(localMusicInfo.getAlbumName() + " - " + localMusicInfo.getArtistName());
            Bitmap bitmap = localMusicInfo.getAlbumArtBitmap();
            if (bitmap == null){
                bitmap = defaultAlbumArt;
            }
            viewHolder.albumArt.setImageBitmap(bitmap);

        }else{
            RemoteMusicInfo remoteMusicInfo = (RemoteMusicInfo)musicInfo;
            ImageView albumArt = viewHolder.albumArt;
            viewHolder.songname.setText(remoteMusicInfo.getSongName());
            viewHolder.songinfo.setText(remoteMusicInfo.getAlbumName() + " - " + remoteMusicInfo.getArtistName());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        OkHttpClient httpClient = new OkHttpClient();
                        Request request = new Request.Builder().url(remoteMusicInfo.getAlbumArtUrl()).build();
                        Bitmap bitmap = BitmapFactory.decodeStream(httpClient.newCall(request).execute().body().byteStream());
                        albumArt.setImageBitmap(bitmap);
                    }catch (IOException ignored) { Log.e("music panel", "download album art failed");}
                }
            }).start();
        }
        return convertView;
    }

    class ViewHolder{
        ImageView albumArt;
        TextView songname;
        TextView songinfo;
    }
}