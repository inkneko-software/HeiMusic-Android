package com.inkneko.heimusic.ui.explor;

import android.app.Application;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.inkneko.heimusic.entity.MusicAlbum;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ExplorViewModel extends AndroidViewModel {

    //当前fragment所展示的音乐歌单
    private MutableLiveData<MusicAlbum> mutableMusicAlbum;

    private Application application;
    public ExplorViewModel(Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<MusicAlbum> getMusicAlbum(final Consumer<String> onFailedCallback){
        mutableMusicAlbum = new MutableLiveData<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                fetchMusicAlbum(onFailedCallback);
            }
        }).start();
        return mutableMusicAlbum;
    }

    private void fetchMusicAlbum(final Consumer<String> onFailedCallback){
        //采集自https://music.163.com/#/playlist?id=749733462
        //https://music.inkneko.com/heimusic.json
        try{
            String albumName;
            String albumArtUrl;
            Long createTime;
            String albumCreator;
            List<MusicInfo> musicList = new LinkedList<>();

            SimpleDateFormat fmt;

            OkHttpClient httpClient = new OkHttpClient().newBuilder().followRedirects(false).build();

            Request request = new Request.Builder().url("https://music.inkneko.com/heimusic.json").build();
            JSONObject jsonObject = new JSONObject(httpClient.newCall(request).execute().body().string());
            albumName = jsonObject.getString("album_name");
            albumArtUrl = jsonObject.getString("album_art");
            createTime = jsonObject.getLong("create_timestamp");
            albumCreator = jsonObject.getString("album_creator");

            JSONArray jsonMusicList = jsonObject.getJSONArray("songlist");
            for (int i = 0; i < jsonMusicList.length(); ++i){
                JSONObject songItem = jsonMusicList.getJSONObject(i);
                //解析音乐文件地址
                request = new Request.Builder().url(songItem.getString("url")).head().header("user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36").build();
                String convertedUrl = httpClient.newCall(request).execute().header("location", null);
                if (convertedUrl != null && !convertedUrl.contains("http://music.163.com/404")){
                    musicList.add(new RemoteMusicInfo(
                            songItem.getString("song_name"),
                            songItem.getString("album_name"),
                            songItem.getString("artist_name"),
                            convertedUrl,
                            albumArtUrl
                    ));
                }
            }

            MusicAlbum musicAlbum = new MusicAlbum(albumName, albumArtUrl,createTime, albumCreator, musicList.size(), musicList);
            mutableMusicAlbum.postValue(musicAlbum);
        }catch (IOException e) {
            onFailedCallback.accept(e.getMessage());
        }catch (JSONException e) {
            onFailedCallback.accept("服务器返回数据有误");
        }
    }

}