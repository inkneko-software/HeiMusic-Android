package com.inkneko.heimusic.storage.localmusic;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;

import java.util.ArrayList;

public class LocalMusicScanner {
    private MutableLiveData<Pair<Integer, Integer>> mutableScanProgress;
    private Context context;
    private LocalMusicDatabase localMusicDatabase;

    public LocalMusicScanner(Context context) {
        this.context = context;
        localMusicDatabase = LocalMusicDatabase.getInstance(context);
    }

    public MutableLiveData<Pair<Integer, Integer>> scan() {
        mutableScanProgress = new MutableLiveData<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                scanAudioFiles();
            }
        }).start();
        return mutableScanProgress;
    }

    private void scanAudioFiles(){
        LocalMusicDao dao = localMusicDatabase.getDao();
        dao.deleteAll();

        //使用MediaStore.Audio获取当前设备上的音频文件
        //https://developer.android.com/guide/topics/providers/content-providers
        //https://blog.csdn.net/yann02/article/details/92844364
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        //这里认定大于一分钟的音频为音乐，以忽略掉系统铃声和其他的一些提示音效，使用IS_MUSIC不够准确
        String selection = MediaStore.Audio.Media.DURATION + " > 60000";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
        };

        //TODO: use CursorLoader to load in the background
        Cursor cursor = resolver.query(

                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        Integer totalRecordNums = cursor.getCount();
        Integer count = 0;
        mutableScanProgress.postValue(new Pair<>(0, totalRecordNums));

        while(cursor.moveToNext()){
            String songName = cursor.getString(0);
            String albumName = cursor.getString(1);
            String artistName = cursor.getString(2);
            Long timestamp = cursor.getLong(3);
            Integer duration = cursor.getInt(4);
            String resourceId = cursor.getString(5);

            Uri uriDataSource =  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            uriDataSource = Uri.withAppendedPath(uriDataSource, resourceId);

            Bitmap albumArtBitmap = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context.getApplicationContext(), uriDataSource);

            LocalMusic localMusic = new LocalMusic(uriDataSource.toString(),songName, albumName, artistName, duration, timestamp);
            dao.insert(localMusic);
            count += 1;
            mutableScanProgress.postValue(new Pair<>(count, totalRecordNums));
        }
        cursor.close();

    }
}
