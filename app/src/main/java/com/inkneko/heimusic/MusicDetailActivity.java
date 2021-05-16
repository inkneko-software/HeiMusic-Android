package com.inkneko.heimusic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.service.MusicPlayController;
import com.inkneko.heimusic.service.MusicCoreService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import jp.wasabeef.blurry.internal.Blur;
import jp.wasabeef.blurry.internal.BlurFactor;

public class MusicDetailActivity extends AppCompatActivity {

    private MusicCoreService musicCoreService;
    private MusicPlayController controller;
    private List<MusicInfo> localMusicInfoList;
    private Bitmap defaultAlbumArt;

    private Button returnButton;
    private ImageView albumArtImageView;
    private TextView songNameTextView;
    private TextView songInfoTextView;
    private TextView currentPositionTextView;
    private TextView durationTextView;
    private SeekBar seekBar;
    private MaterialButton playMethodButton;
    private MaterialButton switchPrevButton;
    private MaterialButton switchNextButton;
    private MaterialButton playActionButton;
    private MaterialButton playListButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_detail);

        //沉浸式状态栏
        //https://www.jianshu.com/p/50d2024fa60a
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //透明状态栏/导航栏
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        findViewById(R.id.activity_music_detail_wrap).setFitsSystemWindows(true);



        //MusicPlayService服务创建
        Intent intent = new Intent(this, MusicCoreService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        defaultAlbumArt = defaultAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_albumart);

        //事件绑定...
        returnButton = findViewById(R.id.music_detail_return_button);
        albumArtImageView = findViewById(R.id.music_detail_album_art);
        songNameTextView = findViewById(R.id.music_detail_songname);
        songInfoTextView  = findViewById(R.id.music_detail_songinfo);
        currentPositionTextView = findViewById(R.id.music_detail_current_position);
        durationTextView  = findViewById(R.id.music_detail_duration);
        seekBar = findViewById(R.id.music_detail_seek_bar);
        playMethodButton = findViewById(R.id.music_detail_action_panel_play_method);
        switchPrevButton = findViewById(R.id.music_detail_action_panel_switch_previous);
        switchNextButton = findViewById(R.id.music_detail_action_panel_switch_next);
        playActionButton = findViewById(R.id.music_detail_action_panel_action_action);
        playListButton  = findViewById(R.id.music_detail_action_panel_play_list);

        controller = MusicPlayController.getMusicPlayController();
        controller.getCurrentPlayingMusic().observe(this, onMusicChangeRequestListener);
        returnButton.setOnClickListener(onReturnButtonClicked);

    }




    /**
     * MusicPlayService服务建立成功的事件回调
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicCoreService.MusicCoreServiceBinder binder = (MusicCoreService.MusicCoreServiceBinder)service;
            MusicDetailActivity.this.musicCoreService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 更换音乐请求的事件回调
     */
    private Observer<MusicInfo> onMusicChangeRequestListener = new Observer<MusicInfo>() {
        @Override
        public void onChanged(MusicInfo musicInfo) {
            Bitmap newAlbumArtBitmap;
            if (musicInfo instanceof LocalMusicInfo){
                LocalMusicInfo localMusicInfo = (LocalMusicInfo)musicInfo;
                newAlbumArtBitmap = ((LocalMusicInfo) musicInfo).getAlbumArtBitmap();
                if (newAlbumArtBitmap == null){
                    newAlbumArtBitmap = defaultAlbumArt;
                }

                songNameTextView.setText(localMusicInfo.getSongName());
                songInfoTextView.setText(localMusicInfo.getAlbumName() + " - " + localMusicInfo.getArtistName());
                albumArtImageView.setImageBitmap(newAlbumArtBitmap);

                //背景的高斯模糊设置
                Bitmap preparedBitmap = newAlbumArtBitmap;
                //preparedBitmap = Bitmap.createBitmap(preparedBitmap.getWidth(), preparedBitmap.getHeight(),Bitmap.Config.ARGB_8888);
                BlurFactor factor = new BlurFactor();
                factor.radius = 20;
                factor.sampling = 5;
                factor.height = preparedBitmap.getHeight();
                factor.width = preparedBitmap.getWidth();
                factor.color = Color.argb(70, 0, 0, 0);
                preparedBitmap = Blur.of(MusicDetailActivity.this,preparedBitmap, factor);
                Drawable drawable = new BitmapDrawable(getResources(), preparedBitmap);
                findViewById(R.id.activity_music_detail_wrap).setBackground(drawable);



            }

        }
    };

    private View.OnClickListener onReturnButtonClicked = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            MusicDetailActivity.this.finish();
        }
    };

}
