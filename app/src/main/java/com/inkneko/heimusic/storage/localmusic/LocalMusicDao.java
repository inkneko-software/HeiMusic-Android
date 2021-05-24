package com.inkneko.heimusic.storage.localmusic;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LocalMusicDao {

    /**
     * 插入/更新数据
     * @param localMusic 音乐信息
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(LocalMusic localMusic);

    /**
     * 删除指定的音乐信息
     * @param localMusic 音乐信息
     */
    @Delete
    public void delete(LocalMusic localMusic);

    /**
     * 删除全部的音乐信息
     */
    @Query("DELETE FROM localmusic")
    public void deleteAll();

    /**
     * 查询当前数据库中所有的本地音乐信息，按创建时间的时间戳降序排列
     * @return 本地音乐信息列表
     */
    @Query("SELECT * FROM localmusic ORDER BY createdTimestamp DESC")
    public List<LocalMusic> selectAll();

    /**
     * 选取最新的limit个音乐信息记录
     * @param limit 指定的个数
     * @return 音乐信息列表
     */
    @Query("SELECT * FROM localmusic ORDER BY createdTimestamp DESC LIMIT :limit")
    public List<LocalMusic> select(int limit);

    /**
     * 从指定lastId处开始向过去选择指定limit个音乐信息记录
     * @param lastId 上一次查询中最旧的音乐信息的id
     * @param limit 指定的个数
     * @return 音乐信息列表
     */
    @Query("SELECT * FROM localmusic where id < :lastId ORDER BY createdTimestamp DESC LIMIT :limit")
    public List<LocalMusic> select(int lastId, int limit);

    /**
     * 统计当前数据库中记录的数量
     * @return 数量
     */
    @Query("SELECT COUNT(*) FROM localmusic")
    public LiveData<Integer> count();
}
