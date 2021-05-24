package com.inkneko.heimusic.storage.history;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用户历史记录的定义。
 * 实际的读取请使用getSharedPreference，键名为以下的变量名
 */
public abstract class UserHistory {
    public int playMethod;
    public String lastDataSourceUri;
    public String lastAlbumArtUri;
    public String lastSongName;
    public String lastArtistName;
    public String lastAlbumName;
    public int lastDuration;
    public int lastPosition;
}
