package com.inkneko.heimusic.service;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.inkneko.heimusic.entity.MusicInfo;

/**
 * MainActivity为Observer，负责observe本类中的livedata，进行UI的设定以及音乐的切换
 * 其他UI作为Provider，负责更新当前应当播放的音乐
 */
public class MusicPlayController {
    private static  MusicPlayController INSTANCE = null;
    public static MusicPlayController getMusicPlayController(){
        synchronized (MusicPlayController.class){
            if (INSTANCE == null){
                INSTANCE = new MusicPlayController();
            }
        }
        return INSTANCE;
    }

    private MutableLiveData<MusicInfo> currentPlayingMusic = new MutableLiveData<>();

    /**
     * 设置当前播放的音乐
     * @param musicInfo 音乐信息
     */
    public void changeMusic(MusicInfo musicInfo){
        currentPlayingMusic.postValue(musicInfo);
    }

    /**
     * 获取当前播放音乐的livedata
     * @return
     */
    public MutableLiveData<MusicInfo> getCurrentPlayingMusic(){
        return currentPlayingMusic;
    }
}
