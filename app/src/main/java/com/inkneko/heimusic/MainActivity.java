package com.inkneko.heimusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;
import com.inkneko.heimusic.service.MusicCoreService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity  {
    private MusicCoreService musicCoreService;
    private boolean noSongDataSource = true;
    private boolean paused = true;
    private boolean stoped = true;
    private int lastPausePosition = 0;

    private Bitmap defaltAlbumArt;
    private ArrayList<MusicInfo> musicList;
    private MusicInfo musicInfo;
    MaterialButton actionButton;
    MaterialButton listButton;
    ProgressBar progressBar;
    TextView songNameTextView;
    TextView songInfoTextView;
    ImageView albumArtImageView;
    View panelView;


    Timer positionUpdateTimer = new Timer();
    TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //导航UI行为设定
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //MusicPlayService服务创建
        Intent intent = new Intent(this, MusicCoreService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        //底部音乐控制面板的各种绑定...
        actionButton = findViewById(R.id.player_panel_action_button);
        listButton = findViewById(R.id.player_panel_playlist_button);
        progressBar = findViewById(R.id.player_panel_progress_bar);
        songNameTextView = findViewById(R.id.player_panel_song_name);
        songInfoTextView = findViewById(R.id.player_panel_song_info);
        albumArtImageView = findViewById(R.id.player_panel_album_image);
        panelView = findViewById(R.id.player_panel);

        actionButton.setOnClickListener(onActionButtonListener);
        listButton.setOnClickListener(onPlayListButtonListener);
        panelView.setOnClickListener(onPanelViewListener);
        songInfoTextView.setSelected(true);

        defaltAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_albumart);
    }

    /**
     * MusicPlayService服务建立成功的事件回调
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicCoreService.MusicCoreServiceBinder binder = (MusicCoreService.MusicCoreServiceBinder)service;
            MainActivity.this.musicCoreService = binder.getService();
            musicCoreService.addOnStateChangeListener(onStateChangeListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 播放动作按钮按下的事件回调
     */
    private View.OnClickListener onActionButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (!noSongDataSource){
                if (paused){
                    if (stoped){
                        musicCoreService.seekTo(0);
                    }
                    musicCoreService.resumeMusic();
                }else{
                    musicCoreService.pauseMusic();
                }
            }
        }
    };

    /**
     * 播放列表按钮按下的事件回调
     */
    private View.OnClickListener onPlayListButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO: add view
        }
    };

    private View.OnClickListener onPanelViewListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!noSongDataSource){
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, MusicDetailActivity.class);
                    startActivity(intent);
                }
        }
    };

    private MusicCoreService.OnStateChangeListener onStateChangeListener = new MusicCoreService.OnStateChangeListener() {
        @Override
        public void onMusicListChanged(ArrayList<MusicInfo> musicList) {
            MainActivity.this.musicList = musicList;
        }

        @Override
        public void onMusicChanged(MusicInfo newMusicInfo, int index) {
            if (newMusicInfo instanceof RemoteMusicInfo){ //如果音乐是远端源
                RemoteMusicInfo remoteMusicInfo = (RemoteMusicInfo)newMusicInfo;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            OkHttpClient httpClient = new OkHttpClient();
                            Request request = new Request.Builder().url(remoteMusicInfo.getAlbumArtUrl()).build();
                            Bitmap bitmap = BitmapFactory.decodeStream(httpClient.newCall(request).execute().body().byteStream());
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    albumArtImageView.setImageBitmap(bitmap);
                                }
                            });
                        }catch (IOException ignored) {

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    albumArtImageView.setImageBitmap(defaltAlbumArt);
                                }
                            });
                            Log.e("heimusic-music-panel", "download album art failed");
                        }
                    }
                }).start();
                songNameTextView.setText(remoteMusicInfo.getSongName());
                songInfoTextView.setText(remoteMusicInfo.getAlbumName() + " - " + remoteMusicInfo.getArtistName());

            }else { //否则是本地源
                LocalMusicInfo localMusicInfo = (LocalMusicInfo)newMusicInfo;
                songNameTextView.setText(localMusicInfo.getSongName());
                songInfoTextView.setText(localMusicInfo.getAlbumName() + " - " + localMusicInfo.getArtistName());
                Bitmap albumArtBitmap=localMusicInfo.getAlbumArtBitmap();
                if (albumArtBitmap != null){
                    albumArtImageView.setImageBitmap(albumArtBitmap);
                }else{
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_albumart);
                    albumArtImageView.setImageBitmap(bitmap);
                }
            }
            actionButton.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_pause_circle_outline_black_24dp));
            paused = false;
            stoped = false;
            noSongDataSource = false;
        }

        @Override
        public void onPaused() {
            paused = true;
            actionButton.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_play_circle_outline_black_24dp));
        }

        @Override
        public void onStoped() {
            paused = true;
            stoped = true;
            actionButton.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_play_circle_outline_black_24dp));
        }

        @Override
        public void onResumed(MusicInfo resumeMusicInfo) {
            paused = false;
            stoped =false;
            actionButton.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_pause_circle_outline_black_24dp));
        }

        @Override
        public void onPositionChanged(int posotion, int duration) {
            progressBar.setProgress(posotion);
            progressBar.setMax(duration);
        }

        @Override
        public void onPlayMethodChanged(int method) {
            //ignored
        }
    };
}
