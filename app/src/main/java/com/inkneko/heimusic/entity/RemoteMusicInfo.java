package com.inkneko.heimusic.entity;

/**
 * 远程音乐信息的定义
 */
public class RemoteMusicInfo extends MusicInfo {
    private String urlDataSrouce;
    private String albumArtUrl;

    public RemoteMusicInfo(String songName, String albumName, String artistName, String urlDataSrouce, String albumArtUrl) {
        super(songName, albumName, artistName);
        this.urlDataSrouce = urlDataSrouce;
        this.albumArtUrl = albumArtUrl;
    }
    public String getUrlDataSrouce() {
        return urlDataSrouce;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }
}
