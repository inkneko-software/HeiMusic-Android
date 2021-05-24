package com.inkneko.heimusic.storage.localmusic;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * 单例的LocaMusicDatabase
 */
@Database(entities = {LocalMusic.class}, version = 1, exportSchema = false)
public abstract class LocalMusicDatabase extends RoomDatabase {
    private static volatile LocalMusicDatabase INSTANCE;
    public static LocalMusicDatabase getInstance(final Context context){
        if (INSTANCE == null){
            synchronized (LocalMusicDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), LocalMusicDatabase.class, "LocalMusicDatabase").build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 获取LocalMusicDao
     * @return LocalMusicDao实例
     */
    public abstract LocalMusicDao getDao();


    public void scanLocalFiles(){
        LocalMusicDao dao = getDao();

    }

}
