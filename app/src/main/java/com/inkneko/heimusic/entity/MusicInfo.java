package com.inkneko.heimusic.entity;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * 音乐信息的基础定义。
 */
public class MusicInfo {
    private String songName;
    private String albumName;
    private String artistName;

    public MusicInfo(String songName, String albumName, String artistName) {
        this.songName = songName;
        this.albumName = albumName;
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj != null
                && this.getClass() == obj.getClass()
                && this.songName.equals(((MusicInfo) obj).songName)
                && this.albumName.equals(((MusicInfo) obj).albumName)
                && this.artistName.equals(((MusicInfo) obj).artistName));
    }
}
