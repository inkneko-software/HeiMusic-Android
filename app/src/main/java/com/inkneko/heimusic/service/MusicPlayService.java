package com.inkneko.heimusic.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import java.io.IOException;


public class MusicPlayService extends Service {
    private MusicPlayServiceBinder musicPlayServiceBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        musicPlayServiceBinder = new MusicPlayServiceBinder();
        return musicPlayServiceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    //服务的核心逻辑

    //播放完成的事件回调
    private MediaPlayer.OnCompletionListener onCompletionListener;
    private MediaPlayer mediaPlayer;
    private int currentDuration = -1;

    /**
     * 通过url播放音乐，可以是file://或https://协议地址
     * @param url 音乐源地址
     * @throws IOException
     */
    public void playMusic(String url) throws IOException{
        if (mediaPlayer!= null){
            try {
                mediaPlayer.stop();
            }catch (IllegalStateException ignored) {}
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(url);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                currentDuration = mediaPlayer.getDuration();
            }
        });
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mediaPlayer.prepareAsync();
    }

    /**
     * 通过uri(通过ContentProvider获取到)播放音乐
     * @param context 调用者的context
     * @param uri 音乐uri
     * @throws IOException
     */
    public void playMusic(Context context, Uri uri) throws IOException{
        if (mediaPlayer!= null){
            try {
                mediaPlayer.stop();
            }catch (IllegalStateException ignored) {}
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(context, uri);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                currentDuration = mediaPlayer.getDuration();
            }
        });
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mediaPlayer.prepareAsync();
    }

    /**
     * 设置音乐播放完成事件回调。本回调只有在playMusic调用前设置才会生效
     * @param onCompletionListener 事件回调
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener){
        this.onCompletionListener = onCompletionListener;
    }

    /**
     * 暂停音乐的播放
     */
    public void pauseMusic(){
        try{
            mediaPlayer.pause();
        }catch (IllegalStateException ignored) {}
    }

    /**
     * 恢复音乐的播放
     * @param position 位置
     * @return 若恢复播放成功，则返回true，其他情况返回false
     */
    public boolean resumeMusic(int position){
        try{
            mediaPlayer.seekTo(position);
            mediaPlayer.start();
            return true;
        }catch (IllegalStateException ignored) {}
        return false;
    }

    /**
     * 停止音乐的播放
     */
    public void stopMusic(){
        try {
            mediaPlayer.stop();
        }catch (IllegalStateException ignored) {}
    }

    /**
     * 获取当前音频文件的时长
     * @return 时长，如果获取失败返回-1
     */
    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    /**
     * 获取当前的播放位置
     * @return 播放位置，如果获取失败返回-1
     */
    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }


    /**
     * 服务binder
     */
    public class MusicPlayServiceBinder extends Binder{
        public MusicPlayService getService(){
            return MusicPlayService.this;
        }
    }
}
