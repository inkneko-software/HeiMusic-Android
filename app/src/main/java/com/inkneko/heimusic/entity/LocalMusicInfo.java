package com.inkneko.heimusic.entity;

import android.graphics.Bitmap;
import android.net.Uri;

public class LocalMusicInfo extends MusicInfo {
    private Uri uriDataSource;
    private Bitmap albumArtBitmap;

    public LocalMusicInfo(String songName, String albumName, String artistName, Uri uriDataSource, Bitmap albumArtBitmap) {
        super(songName, albumName, artistName);
        this.uriDataSource = uriDataSource;
        this.albumArtBitmap = albumArtBitmap;
    }

    public Uri getUriDataSource() {
        return uriDataSource;
    }

    public Bitmap getAlbumArtBitmap() {
        return albumArtBitmap;
    }
    }
