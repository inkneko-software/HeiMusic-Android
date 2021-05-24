package com.inkneko.heimusic.storage.localmusic;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 用于缓存本地的音乐信息，避免每次打开应用需要扫描一遍本地存储
 */
@Entity(tableName = "localmusic")
public class LocalMusic {
    /**
     * 用于唯一标识一条记录，因此该数据应当由数据库自动生成
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "dataSourceUri")
    private String dataSourceUri;

    @ColumnInfo(name = "songName")
    private String songName;

    @ColumnInfo(name = "albumName")
    private String albumName;

    @ColumnInfo(name = "artistName")
    private String artistName;

    @ColumnInfo(name = "duration")
    private int duration;

    @ColumnInfo(name = "createdTimestamp")
    private Long createdTimestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDataSourceUri() {
        return dataSourceUri;
    }

    public void setDataSourceUri(String dataSourceUri) {
        this.dataSourceUri = dataSourceUri;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalMusic(String dataSourceUri, String songName, String albumName, String artistName, int duration, Long createdTimestamp) {
        this.dataSourceUri = dataSourceUri;
        this.songName = songName;
        this.albumName = albumName;
        this.artistName = artistName;
        this.duration = duration;
        this.createdTimestamp = createdTimestamp;
    }
}
