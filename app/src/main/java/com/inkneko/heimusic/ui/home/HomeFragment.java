package com.inkneko.heimusic.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.content.CursorLoader;

import com.google.android.material.button.MaterialButton;
import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;
import com.inkneko.heimusic.service.MusicPlayController;
import com.inkneko.heimusic.service.MusicPlayService;
import com.inkneko.heimusic.ui.adapter.MusicBriefAdapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM;
import static android.media.MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST;
import static android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST;
import static android.media.MediaMetadataRetriever.METADATA_KEY_AUTHOR;
import static android.media.MediaMetadataRetriever.METADATA_KEY_TITLE;
import static android.media.MediaMetadataRetriever.METADATA_KEY_WRITER;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private MusicPlayController controller;
    private List<MusicInfo> localMusicInfoList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        controller = MusicPlayController.getMusicPlayController();



        //自安卓6.0起敏感权限需要进一步动态申请
        //https://developer.android.com/training/permissions/requesting
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }else{
            loadLocalMusicResources();
        }
        return root;
    }

    public void openDirectory(Uri uriToLoad) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);;
        //intent.setType("*/*");
        String[] mimetypes = {"image/*", "video/*", "audio/*"};
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

        startActivityForResult(intent, 500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == 500
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
                metaRetriver.setDataSource(getContext(), uri);
                Bitmap songImage = null;
                try {
                    byte[] art = metaRetriver.getEmbeddedPicture();
                    songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
                } catch (Exception e) {
                }

                String author = metaRetriver.extractMetadata(METADATA_KEY_AUTHOR);
                String albumName = metaRetriver.extractMetadata(METADATA_KEY_ALBUM);
                String artist = metaRetriver.extractMetadata(METADATA_KEY_ARTIST);
                String songtitle = metaRetriver.extractMetadata(METADATA_KEY_TITLE);

                if (author == null){
                    author = "未获取到作者";
                }
                if (albumName == null){
                    albumName = "未获取到albumName";
                }
                if (artist == null ){
                    artist = "未获取到albumArtist";
                }
                if (songtitle == null){
                    songtitle = "未获取到songtitle";
                }


                LocalMusicInfo localMusicInfo = new LocalMusicInfo(songtitle, albumName, artist, uri, songImage);
                controller.changeMusic(localMusicInfo);
            }
        }
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadLocalMusicResources();
                } else {
                    homeViewModel = null;
                }
            });

    private void loadLocalMusicResources(){
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getLocalMusicInfoList().observe(getViewLifecycleOwner(), new Observer<List<MusicInfo>>() {
            @Override
            public void onChanged(List<MusicInfo> localMusicInfos) {
                localMusicInfoList = localMusicInfos;
                ListView listView = getView().findViewById(R.id.home_local_music_list);
                listView.setOnItemClickListener(onItemClickListener);
                listView.setAdapter(new MusicBriefAdapter(getContext(), localMusicInfos));
            }
        });
    }

    private ListView.OnItemClickListener onItemClickListener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LocalMusicInfo localMusicInfo = (LocalMusicInfo)localMusicInfoList.get(position);
            controller.changeMusic(localMusicInfo);
        }
    };
}