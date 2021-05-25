package com.inkneko.heimusic.entity;

import java.util.List;

/**
 * 远程歌单的定义
 */
public class MusicAlbum {
    private String albumName;
    private String albumArtUrl;
    private Long createTimestamp;
    private String albumCreator;
    private int songNum;
    private List<MusicInfo> songList;

    public MusicAlbum(String albumName, String albumArtUrl, Long createTimestamp, String albumCreator, int songNum, List<MusicInfo> songList) {
        this.albumName = albumName;
        this.albumArtUrl = albumArtUrl;
        this.createTimestamp = createTimestamp;
        this.songNum = songNum;
        this.songList = songList;
        this.albumCreator = albumCreator;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public int getSongNum() {
        return songNum;
    }

    public List<MusicInfo> getSongList() {
        return songList;
    }

    public String getAlbumCreator() {
        return albumCreator;
    }
}
