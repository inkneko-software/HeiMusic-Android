package com.inkneko.heimusic.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MusicCoreService extends Service {
    MusicCoreServiceBinder musicCoreServiceBinder;

    //MediaPlayer实例
    private MediaPlayer mediaPlayer;
    //播放列表
    private ArrayList<MusicInfo> musicList;
    //当前播放的音乐
    private MusicInfo currentMusic;
    //当前播放的音乐在播放列表中的位置。下标从0开始
    private int currentMusicPosition = -1;
    //当前的事件监听器
    private List<OnStateChangeListener> onStateChangeListenerList;
    //当前datasource的准备状态
    private boolean prepared = false;
    //当前的播放完成动作,默认为停止， 1为单曲循环，2为列表循环，3为顺序播放，4为随机播放
    int onCompleteAction =0;
    //当前是否正在播放
    private boolean playing = false;
    private Timer notifyProgressTimer;
    private TimerTask notifyProgressTask;


    /**
     * 服务的初始化
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        currentMusic = null;
        musicList = new ArrayList<>();
        onStateChangeListenerList = new LinkedList<>();
        notifyProgressTimer = new Timer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        musicCoreServiceBinder = new MusicCoreServiceBinder();
        return musicCoreServiceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 服务binder
     */
    public class MusicCoreServiceBinder extends Binder{
        public MusicCoreService getService(){
            return MusicCoreService.this;
        }
    }



    /**
     * 添加播放状态的监听器
     * @param listener 监听器对象
     */
    public void addOnStateChangeListener(OnStateChangeListener listener){
        onStateChangeListenerList.add(listener);
    }

    /**
     * 移除播放状态监听
     * @param listener 监听器对象
     */
    public void removeOnStateChangeListener(OnStateChangeListener listener){
        onStateChangeListenerList.remove(listener);
    }




    /**
     * 加载音乐并设置到指定播放位置，但不播放
     * @param musicInfo 音乐信息
     * @param position 播放位置
     */
    public void loadMusic(MusicInfo musicInfo, int position){
        synchronized (this){
            prepared = false;
            playing = false;
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setOnErrorListener(onErrorListener);
            updateCurrentMusicInfo(musicInfo);
            Uri dataSource = null;
            try{
                if (musicInfo instanceof LocalMusicInfo){
                    mediaPlayer.setDataSource(this, ((LocalMusicInfo) musicInfo).getUriDataSource());
                }else{
                    RemoteMusicInfo remoteMusicInfo = (RemoteMusicInfo)musicInfo;
                    mediaPlayer.setDataSource(remoteMusicInfo.getUrlDataSrouce());
                }
                mediaPlayer.setOnPreparedListener((MediaPlayer mp)->{
                    prepared = true;
                    mp.seekTo(position);
                });
                mediaPlayer.prepareAsync();
            }catch (IOException e){
                Toast notify = Toast.makeText(this, "加载音乐失败, 请检查网络设置", Toast.LENGTH_LONG);
                notify.show();
            }
        }
    }

    /**
     * 播放音乐
     * @param musicInfo 音乐信息
     */
    public void playMusic(MusicInfo musicInfo){
        synchronized (this){
            prepared = false;
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setOnErrorListener(onErrorListener);
            updateCurrentMusicInfo(musicInfo);
            try{
                if (musicInfo instanceof LocalMusicInfo){
                    mediaPlayer.setDataSource(this, ((LocalMusicInfo) musicInfo).getUriDataSource());
                }else{
                    RemoteMusicInfo remoteMusicInfo = (RemoteMusicInfo)musicInfo;
                    mediaPlayer.setDataSource(remoteMusicInfo.getUrlDataSrouce());
                }
                mediaPlayer.setOnPreparedListener((MediaPlayer mp)->{
                    prepared = true;
                    playing = true;
                    mp.start();
                    if (notifyProgressTask != null){
                        notifyProgressTask.cancel();
                    }
                    notifyProgressTask = new NotifyProgressTask();
                    notifyProgressTimer.schedule(notifyProgressTask, 1, 100);
                });
                mediaPlayer.prepareAsync();
            }catch (IOException e){
                Toast notify = Toast.makeText(this, "加载音乐失败, 请检查网络设置", Toast.LENGTH_LONG);
                notify.show();
            }
        }
    }

    /**
     * 播放音乐，并更换播放列表
     * @param musicInfo
     * @param playList
     */
    public void playMusic(MusicInfo musicInfo, ArrayList<MusicInfo> playList){
        musicList = playList;
        playMusic(musicInfo);
        notifyMusicListChanged();
    }

    /**
     * 播放播放列表中的下一曲。如果当前音乐为最后一首，则播放第一首音乐
     */
    public void playNext(){
        if (musicList.size() == 0){return;}
        if (currentMusicPosition == musicList.size()-1){
            currentMusicPosition = -1;
        }
        currentMusicPosition += 1;
        playMusic(musicList.get(currentMusicPosition));
    }

    /**
     * 播放播放列表中的上一曲。如果当前音乐为第一首，则播放最后一首音乐
     */
    public void playPrevious(){
        if (musicList.size() == 0){return;}
        if (currentMusicPosition == 0){
            currentMusicPosition = musicList.size();
        }
        currentMusicPosition -= 1;
        playMusic(musicList.get(currentMusicPosition));
    }

    /**
     * 暂停音乐的播放
     */
    public void pauseMusic(){
        try{
            mediaPlayer.pause();
            notifyPaused();
        }catch (IllegalStateException ignored) {}
    }

    /**
     * 从当前播放位置恢复音乐播放
     */
    public void resumeMusic(){
        mediaPlayer.start();
        notifyResumed(currentMusic);
    }

    /**
     * 将当前播放进度移动至指定位置
     * @param posistion 位置
     */
    public void seekTo(int posistion){
        mediaPlayer.seekTo(posistion);
    }

    /**
     * 设定播放顺序模式
     * @param method 模式，默认为顺序播放，1为单曲循环，2为列表循环，3为随机播放
     */
    public void setPlayMothod(int method){
        onCompleteAction = method;
        notifyPlayMethodChanged(method);
    }

    /**
     * 获取当前已加载的音乐
     * @return 当前已加载的(prepared)音乐信息
     */
    public MusicInfo getCurrentMusic(){
        return currentMusic;
    }

    /**
     * 获取当前的播放列表
     * @return 当前的播放列表，为空则为null
     */
    public ArrayList<MusicInfo> getCurrentPlayList(){
        return musicList;
    }

    /**
     * 查询当前是否正在播放
     * @return true如果播放，false其他
     */
    public boolean getCurrentPlaying(){
        return playing;
    }

    /**
     * 维护当前音乐的相关信息
     * @param musicInfo 当前播放的音乐信息
     */
    private void updateCurrentMusicInfo(MusicInfo musicInfo){
        //设定当前播放的音乐
        int pos = musicList.indexOf(musicInfo);
        if(pos == -1){
            musicList.add(musicInfo);
            currentMusicPosition = musicList.size()-1;
            notifyMusicListChanged();
        }else{
            currentMusicPosition = pos;
        }
        currentMusic = musicInfo;
        notifyMusicChanged(musicInfo, currentMusicPosition);
    }

    /**
     * 通知当前播放的音乐发生了改变
     * @param musicInfo 音乐信息
     * @param pos 当前音乐在播放列表的位置
     */
    private void notifyMusicChanged(MusicInfo musicInfo, int pos){
        for(OnStateChangeListener listener : onStateChangeListenerList){
            listener.onMusicChanged(musicInfo, pos);
        }
    }

    /**
     * 通知当前播放列表发生改变
     */
    private void notifyMusicListChanged(){
        for(OnStateChangeListener listener : onStateChangeListenerList){
            listener.onMusicListChanged(musicList);
        }
    }

    /**
     * 通知当前已暂停
     */
    private void notifyPaused(){
        for(OnStateChangeListener listener : onStateChangeListenerList){
            listener.onPaused();
        }
    }

    /**
     * 通知当前已恢复播放
     * @param resumeMusicInfo 播放的音乐信息
     */
    private void notifyResumed(MusicInfo resumeMusicInfo){
        for(OnStateChangeListener listener : onStateChangeListenerList){
            listener.onResumed(resumeMusicInfo);
        }
    }

    /**
     * 通知当前音乐播放进度发生改变
     * @param posotion 播放位置，以毫秒计
     * @param duration 音乐总时长，以毫秒计
     */
    private void notifyPositionChanged(int posotion, int duration){
        for(OnStateChangeListener listener : onStateChangeListenerList){
            listener.onPositionChanged(posotion, duration);
        }
    }

    /**
     * 通知当前音乐播放已停止
     */
    private void notifyStoped(){
        playing = false;
        for(OnStateChangeListener listener : onStateChangeListenerList){
            listener.onStoped();
        }
    }

    /**
     * 通知当前播放顺序已变更
     * @param method
     */
    private void notifyPlayMethodChanged(int method){
        for(OnStateChangeListener listener : onStateChangeListenerList){
            listener.onPlayMethodChanged(method);
        }
    }

    /**
     * 播放完成事件回调
     */
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            //默认为顺序播放， 1为单曲循环，2为列表循环，3为随机播放
            switch (onCompleteAction){
                case 1:
                    mp.seekTo(0);
                    mp.start();
                    break;
                case 2:
                    playNext();
                    break;
                case 3:
                    Random random = new Random();
                    int nextIndex = random.nextInt(musicList.size()) + 1;
                    playMusic(musicList.get(nextIndex));
                    break;
                default:
                    if (currentMusicPosition != musicList.size() - 1){
                        playNext();
                    }else{
                        notifyStoped();
                    }
                    break;
            }
        }
    };

    /**
     * 错误监听器
     */
    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.i("heimusic-MusicCoreService", String.format("what: %d, extra: %d", what, extra));
            return true;
        }
    };

    private class NotifyProgressTask extends TimerTask{
        @Override
        public void run() {
            if (prepared){
                int position = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                if (position == -1){
                    position = 0;
                }
                if (duration == -1){
                    duration = 0;
                }
                notifyPositionChanged(position, duration);
            }
        }
    };

    public interface OnStateChangeListener{
        void onMusicListChanged(ArrayList<MusicInfo> musicList);
        void onMusicChanged(MusicInfo newMusicInfo, int index);
        void onPaused();
        void onStoped();
        void onResumed(MusicInfo resumeMusicInfo);
        void onPositionChanged(int posotion, int duration);
        void onPlayMethodChanged(int method);
    }
}
