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

import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.storage.localmusic.LocalMusic;
import com.inkneko.heimusic.storage.localmusic.LocalMusicDao;
import com.inkneko.heimusic.storage.localmusic.LocalMusicDatabase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;


public class HomeViewModel extends AndroidViewModel {
    private LocalMusicDatabase localMusicDatabase;
    private ArrayList<MusicInfo> localMusicInfoList;
    private MutableLiveData<ArrayList<MusicInfo>> mutableLocalMusicInfoList;
    private Bitmap defaultAlbumArtBitmap;
    private LocalMusicDao dao;


    public HomeViewModel(Application application) {
        super(application);
        localMusicDatabase = LocalMusicDatabase.getInstance(application.getApplicationContext());
        dao = localMusicDatabase.getDao();

        localMusicInfoList = new ArrayList<>();
        mutableLocalMusicInfoList = new MutableLiveData<>();
        mutableLocalMusicInfoList.setValue(localMusicInfoList);
        defaultAlbumArtBitmap = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.default_albumart);
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadAudioFiles();
            }
        }).start();
    }

    public LiveData<Integer> getCount(){
        return dao.count();
    }

    public LiveData<ArrayList<MusicInfo>> getLocalMusicInfoList() {
        return mutableLocalMusicInfoList;
    }

    private void loadAudioFiles(){
        List<LocalMusic> musicList = dao.selectAll();
        for(LocalMusic localMusic : musicList){
            String songName = localMusic.getSongName();
            String albumName = localMusic.getAlbumName();
            String artistName = localMusic.getArtistName();

            Uri uriDataSource =  Uri.parse(localMusic.getDataSourceUri());

            Bitmap albumArtBitmap = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(getApplication().getApplicationContext(), uriDataSource);
            try {
                byte[] art = retriever.getEmbeddedPicture();
                albumArtBitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                if (albumArtBitmap == null){
                    albumArtBitmap = defaultAlbumArtBitmap;
                }
            } catch (Exception e) {
                albumArtBitmap = defaultAlbumArtBitmap;
            }
            MusicInfo localMusicInfo = new LocalMusicInfo(songName,albumName,artistName,uriDataSource,albumArtBitmap);
            //去重
            if (localMusicInfoList.indexOf(localMusicInfo) == -1){
                localMusicInfoList.add(localMusicInfo);
                mutableLocalMusicInfoList.postValue(localMusicInfoList);
            }
        }
    }

}