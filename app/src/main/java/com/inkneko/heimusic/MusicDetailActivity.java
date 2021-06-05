package com.inkneko.heimusic;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;
import com.inkneko.heimusic.service.MusicCoreService;
import com.inkneko.heimusic.ui.adapter.MusicBriefViewHolder;
import com.inkneko.heimusic.ui.adapter.PlayListAdapter;
import com.inkneko.heimusic.ui.adapter.PlayListViewHolder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import jp.wasabeef.blurry.internal.Blur;
import jp.wasabeef.blurry.internal.BlurFactor;

public class MusicDetailActivity extends AppCompatActivity {

    private MusicCoreService musicCoreService;
    private List<MusicInfo> musicList;
    private Bitmap defaultAlbumArt;
    private BlurFactor factor;

    private boolean paused = true;
    private boolean stoped = true;

    private int currentCompletionAction = 0;

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

    RecyclerView playlistRecyclerView ;
    PlayListViewHolder lastHighLightedViewHolder;
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

        defaultAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_albumart);
        //高斯模糊相关设定
        factor = new BlurFactor();
        factor.radius = 20;
        factor.sampling = 5;
        factor.color = Color.argb(70, 0, 0, 0);

        factor.height = defaultAlbumArt.getHeight();
        factor.width = defaultAlbumArt.getWidth();
        //https://github.com/wasabeef/Blurry/issues/26
        Bitmap algorithmInit = Blur.of(MusicDetailActivity.this,defaultAlbumArt, factor); //在魅族pro6plus上需要预先加载一遍，否则会花屏
        algorithmInit = null;

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

        returnButton.setOnClickListener(onReturnButtonClicked);
        playMethodButton.setOnClickListener(onPlayMethodButtonListner);
        playActionButton.setOnClickListener(onActionButtonClicked);
        switchNextButton.setOnClickListener(onPlayNextButtonClicked);
        switchPrevButton.setOnClickListener(onPlayPrevButtonClicked);
        playListButton.setOnClickListener(onPlayListButtonClicked);
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);

        songNameTextView.setSelected(true);
        songInfoTextView.setSelected(true);
    }

    /**
     * 返回按钮按下事件
     */
    private View.OnClickListener onReturnButtonClicked = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            MusicDetailActivity.this.finish();
        }
    };

    /**
     * 播放动作按钮按下事件
     */
    private View.OnClickListener onActionButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (paused){
                if (stoped){
                    musicCoreService.seekTo(0);
                }
                musicCoreService.resumeMusic();
            }else{
                musicCoreService.pauseMusic();
            }
        }
    };

    /**
     * 播放上一首按钮按下事件
     */
    private View.OnClickListener onPlayPrevButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            musicCoreService.playPrevious();
        }
    };


    /**
     * 播放下一首按钮按下事件
     */
    private View.OnClickListener onPlayNextButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            musicCoreService.playNext();
        }
    };


    /**
     * 播放模式按钮按下事件
     */
    private View.OnClickListener onPlayMethodButtonListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            currentCompletionAction = (currentCompletionAction + 1) % 4;
            musicCoreService.setPlayMethod(currentCompletionAction);
        }
    };

    /**
     * 播放列表按钮按下事件
     */
    private View.OnClickListener onPlayListButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (musicList != null) {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;

                View contentView = LayoutInflater.from(MusicDetailActivity.this).inflate(R.layout.layout_playlist, null);
                playlistRecyclerView = contentView.findViewById(R.id.playlist_list);
                PlayListAdapter adapter = new PlayListAdapter(new PlayListAdapter.PlayListDiff(), onItemClickListener);
                adapter.submitList(musicList);
                LinearLayoutManager layoutManager = new LinearLayoutManager(MusicDetailActivity.this);
                playlistRecyclerView.setLayoutManager(layoutManager);
                playlistRecyclerView.addItemDecoration(new DividerItemDecoration(playlistRecyclerView.getContext(),layoutManager.getOrientation()));
                playlistRecyclerView.setAdapter(adapter);
                PopupWindow popWnd = new PopupWindow (MusicDetailActivity.this);
                popWnd.setContentView(contentView);
                popWnd.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                popWnd.setHeight(height/3*2);

                popWnd.setFocusable(true);
                popWnd.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                setWindowBackgroundAlpha(0.6f);
                popWnd.showAtLocation(MusicDetailActivity.this.findViewById(R.id.activity_music_detail_wrap), Gravity.BOTTOM, 0, 0);
                popWnd.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        playlistRecyclerView = null;
                        setWindowBackgroundAlpha(1f);
                    }
                });
            }
        }
    };

    private void setWindowBackgroundAlpha(float alpha) {
        Context mContext = MusicDetailActivity.this;
        if (mContext instanceof Activity) {
            Window window = ((Activity) mContext).getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.alpha = alpha;
            window.setAttributes(layoutParams);
        }
    }

    private PlayListViewHolder.OnItemClickedListener onItemClickListener = new PlayListViewHolder.OnItemClickedListener() {
        @Override
        public void onClicked(View view, int position) {
            MusicInfo musicInfo = musicList.get(position);
            musicCoreService.playMusic(musicInfo);
        }
    };

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser){
                musicCoreService.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //ignored
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //ignored

        }
    };


    /**
     * MusicPlayService服务建立成功的事件回调
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicCoreService.MusicCoreServiceBinder binder = (MusicCoreService.MusicCoreServiceBinder)service;
            MusicDetailActivity.this.musicCoreService = binder.getService();
            init(musicCoreService.getCurrentMusic(), musicCoreService.getCurrentPlaying());
            musicList = musicCoreService.getCurrentPlayList();
            musicCoreService.addOnStateChangeListener(onStateChangeListener);
            onStateChangeListener.onPlayMethodChanged(musicCoreService.getPlayMethod());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 设定封面图片以及背景
     */
    private void init(MusicInfo musicInfo, boolean isPlaying) {
        if (musicInfo instanceof RemoteMusicInfo){ //如果音乐是远端源
            RemoteMusicInfo remoteMusicInfo = (RemoteMusicInfo)musicInfo;
            Glide.with(MusicDetailActivity.this).asBitmap().load(remoteMusicInfo.getAlbumArtUrl())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            setBlurBackground(resource);
                            return false;
                        }
                    })
                    .placeholder(R.drawable.default_albumart).into(albumArtImageView);
            songNameTextView.setText(remoteMusicInfo.getSongName());
            songInfoTextView.setText(remoteMusicInfo.getAlbumName() + " - " + remoteMusicInfo.getArtistName());

        }else { //否则是本地源
            LocalMusicInfo localMusicInfo = (LocalMusicInfo)musicInfo;
            songNameTextView.setText(localMusicInfo.getSongName());
            songInfoTextView.setText(localMusicInfo.getAlbumName() + " - " + localMusicInfo.getArtistName());
            byte[] albumArtBytes =localMusicInfo.getAlbumArtBytes();
            Glide.with(MusicDetailActivity.this).load(albumArtBytes).placeholder(R.drawable.default_albumart).into(albumArtImageView);
            setBlurBackground(albumArtBytes);
        }

        if (isPlaying){
            paused = false;
            stoped =false;
            playActionButton.setIcon(ContextCompat.getDrawable(MusicDetailActivity.this, R.drawable.ic_pause_circle_outline_black_24dp));
        }else{
            playActionButton.setIcon(ContextCompat.getDrawable(MusicDetailActivity.this, R.drawable.ic_play_circle_outline_black_24dp));
        }
    }

    private void setBlurBackground(byte[] bytes){
        Bitmap preparedBitmap;
        if (bytes == null){
            preparedBitmap = defaultAlbumArt;
        }else{
            preparedBitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);

        }
        setBlurBackground(preparedBitmap);
    }

    private void setBlurBackground(Bitmap preparedBitmap){
        factor = new BlurFactor();
        //模糊笔刷半径
        factor.radius = 20;
        //采样率
        factor.sampling = 5;
        //透明度和模糊基色，设定为不透明度70%，基色为黑色
        factor.color = Color.argb(70, 0, 0, 0);
        factor.height = preparedBitmap.getHeight();
        factor.width = preparedBitmap.getWidth();
        preparedBitmap = Blur.of(MusicDetailActivity.this,preparedBitmap, factor);
        Drawable drawable = new BitmapDrawable(getResources(), preparedBitmap);
        findViewById(R.id.activity_music_detail_wrap).setBackground(drawable);
    }


    private MusicCoreService.OnStateChangeListener onStateChangeListener = new MusicCoreService.OnStateChangeListener() {
        @Override
        public void onMusicListChanged(List<MusicInfo> musicList) {
            MusicDetailActivity.this.musicList = musicList;
            if(playlistRecyclerView != null){
                PlayListAdapter adapter =(PlayListAdapter) playlistRecyclerView.getAdapter();
                adapter.submitList(musicList);
            }
        }

        @Override
        public void onPositionChanged(int position, int duration) {
            seekBar.setProgress(position);
            seekBar.setMax(duration);
            int positionMinutes = position /1000 / 60;
            int positionSeconds = position / 1000 % 60;
            int durationMinutes = duration /1000 / 60;
            int durationSeconds = duration / 1000 % 60;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentPositionTextView.setText(String.format("%02d:%02d", positionMinutes,positionSeconds));
                    durationTextView.setText(String.format("%02d:%02d", durationMinutes, durationSeconds));
                }
            });
        }

        @Override
        public void onPlayMethodChanged(int method) {
            currentCompletionAction = method;
            //默认为顺序播放， 1为单曲循环，2为列表循环，3为随机播放
            int drawbleId = R.drawable.ic_playlist_add_check_black_24dp;
            switch(method){
                case 1:
                    drawbleId = R.drawable.ic_repeat_one_black_24dp;
                    break;
                case 2:
                    drawbleId = R.drawable.ic_repeat_black_24dp;
                    break;
                case 3:
                    drawbleId = R.drawable.ic_shuffle_black_24dp;
            }
            playMethodButton.setIcon(ContextCompat.getDrawable(MusicDetailActivity.this, drawbleId));
        }

        @Override
        public void onMusicChanged(MusicInfo newMusicInfo, int index) {
            init(newMusicInfo, true);
            MusicBriefViewHolder.setSelectedPosition(index);
            PlayListViewHolder.setSelectedPosition(index);
            if (playlistRecyclerView != null){
                PlayListViewHolder viewHolder = (PlayListViewHolder) playlistRecyclerView.findViewHolderForAdapterPosition(index);
                if (viewHolder != null){
                    viewHolder.setItemViewHighLighted(true);
                }
                if (lastHighLightedViewHolder != null && lastHighLightedViewHolder != viewHolder){
                    lastHighLightedViewHolder.setItemViewHighLighted(false);
                }
                lastHighLightedViewHolder = viewHolder;
            }
            playActionButton.setIcon(ContextCompat.getDrawable(MusicDetailActivity.this, R.drawable.ic_pause_circle_outline_black_24dp));
            paused = false;
            stoped = false;
        }

        @Override
        public void onPaused() {
            paused = true;
            playActionButton.setIcon(ContextCompat.getDrawable(MusicDetailActivity.this, R.drawable.ic_play_circle_outline_black_24dp));
        }

        @Override
        public void onStoped() {
            paused = true;
            stoped = true;
            playActionButton.setIcon(ContextCompat.getDrawable(MusicDetailActivity.this, R.drawable.ic_play_circle_outline_black_24dp));
        }

        @Override
        public void onResumed(MusicInfo resumeMusicInfo) {
            paused = false;
            stoped =false;
            playActionButton.setIcon(ContextCompat.getDrawable(MusicDetailActivity.this, R.drawable.ic_pause_circle_outline_black_24dp));
        }


    };

    @Override
    protected void onDestroy() {
        musicCoreService.removeOnStateChangeListener(onStateChangeListener);
        super.onDestroy();
    }
}
