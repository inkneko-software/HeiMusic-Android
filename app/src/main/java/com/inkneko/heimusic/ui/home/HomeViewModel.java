package com.inkneko.heimusic.ui.home;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.storage.localmusic.LocalMusic;
import com.inkneko.heimusic.storage.localmusic.LocalMusicDao;
import com.inkneko.heimusic.storage.localmusic.LocalMusicDatabase;

import java.util.ArrayList;
import java.util.List;


public class HomeViewModel extends AndroidViewModel {
    private LocalMusicDatabase localMusicDatabase;
    private ArrayList<MusicInfo> localMusicInfoList;
    private MutableLiveData<ArrayList<MusicInfo>> mutableLocalMusicInfoList;
    private LocalMusicDao dao;


    public HomeViewModel(Application application) {
        super(application);
        localMusicDatabase = LocalMusicDatabase.getInstance(application.getApplicationContext());
        dao = localMusicDatabase.getDao();

        localMusicInfoList = new ArrayList<>();
        mutableLocalMusicInfoList = new MutableLiveData<>();
        mutableLocalMusicInfoList.setValue(localMusicInfoList);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //fixme: memory leak
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

            byte[] albumArtBitmap = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(getApplication().getApplicationContext(), uriDataSource);
                albumArtBitmap = retriever.getEmbeddedPicture();
            } catch (Exception e) {
                albumArtBitmap = null;
            }finally {
                retriever.release();
            }
            MusicInfo localMusicInfo = new LocalMusicInfo(songName,albumName,artistName,uriDataSource,albumArtBitmap);
            localMusicInfoList.add(localMusicInfo);
            mutableLocalMusicInfoList.postValue(localMusicInfoList);
            albumArtBitmap = null;
        }
    }

}