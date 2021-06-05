package com.inkneko.heimusic.ui.mymusic;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;

import java.util.ArrayList;

public class MyMusicViewModel extends AndroidViewModel {
    public MyMusicViewModel(Application application) {
        super(application);

    }
}