package com.inkneko.heimusic.entity;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * 音乐信息的定义。
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
}
