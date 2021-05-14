package com.inkneko.heimusic.ui.home;

import android.Manifest;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;

import java.util.LinkedList;
import java.util.List;


public class HomeViewModel extends AndroidViewModel {

    private List<MusicInfo> localMusicInfoList;
    private MutableLiveData<List<MusicInfo>> mutableLocalMusicInfoList;


    public HomeViewModel(Application application) {
        super(application);
        localMusicInfoList = new LinkedList<>();
        mutableLocalMusicInfoList = new MutableLiveData<>();
        mutableLocalMusicInfoList.setValue(localMusicInfoList);


        //使用MediaStore.Audio获取当前设备上的音频文件
        //https://developer.android.com/guide/topics/providers/content-providers
        //https://blog.csdn.net/yann02/article/details/92844364
        ContentResolver resolver = application.getApplicationContext().getContentResolver();
        //这里认定大于一分钟的音频为音乐，以忽略掉系统铃声和其他的一些提示音效，因为使用IS_MUSIC字段在我的手机上不起作用，全都为0，支持太差
        String selection = MediaStore.Audio.Media.DURATION + " > 60000";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };

        //TODO: load in the background
        Cursor cursor = resolver.query(

                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        while(cursor.moveToNext()){

            String songName = cursor.getString(0);
            String albumName = cursor.getString(1);
            String artistName = cursor.getString(2);
            String resourceId = cursor.getString(3);

            Uri uriDataSource =  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            uriDataSource = Uri.withAppendedPath(uriDataSource, resourceId);

            Bitmap albumArtBitmap = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(application.getApplicationContext(), uriDataSource);
            try {
                byte[] art = retriever.getEmbeddedPicture();
                albumArtBitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            } catch (Exception e) {
                //TODO: add default art image
            }
            MusicInfo localMusicInfo = new LocalMusicInfo(songName,albumName,artistName,uriDataSource,albumArtBitmap);
            localMusicInfoList.add(localMusicInfo);
            mutableLocalMusicInfoList.postValue(localMusicInfoList);
        }
        cursor.close();
    }

    public LiveData<List<MusicInfo>> getLocalMusicInfoList() {
        return mutableLocalMusicInfoList;
    }


}