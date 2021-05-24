package com.inkneko.heimusic.entity;

import android.net.Uri;

public class LocalMusicInfo extends MusicInfo {
    private Uri uriDataSource;
    private byte[] albumArtBytes;

    public LocalMusicInfo(String songName, String albumName, String artistName, Uri uriDataSource, byte[] albumArtBytes) {
        super(songName, albumName, artistName);
        this.uriDataSource = uriDataSource;
        this.albumArtBytes = albumArtBytes;
    }

    public Uri getUriDataSource() {
        return uriDataSource;
    }

    public byte[] getAlbumArtBytes() {
        return albumArtBytes;
    }

    public void onDestory(){
        albumArtBytes = null;
    }
}
