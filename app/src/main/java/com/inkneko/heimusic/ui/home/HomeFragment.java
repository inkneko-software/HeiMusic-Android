package com.inkneko.heimusic.ui.home;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.inkneko.heimusic.MusicDetailActivity;
import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.service.MusicCoreService;
import com.inkneko.heimusic.ui.adapter.MusicBriefAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ArrayList<MusicInfo> localMusicInfoList;
    MusicBriefAdapter adapter;
    MusicCoreService musicCoreService;
    boolean firstTimePlay = true;
    boolean needScan = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        //自安卓6.0起敏感权限需要进一步动态申请
        //https://developer.android.com/training/permissions/requesting
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }else{
            if (needScan = true){
                loadLocalMusicResources();
                needScan = false;
            }
        }

        //MusicPlayService服务创建
        Intent intent = new Intent(getActivity(), MusicCoreService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        return root;
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadLocalMusicResources();
                } else {
                    homeViewModel = null;
                }
            });

    private void loadLocalMusicResources(){
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getLocalMusicInfoList().getValue();
        //TODO: fix concurrent issue on update datalist without notify the adapter
        homeViewModel.getLocalMusicInfoList().observe(getViewLifecycleOwner(), new Observer<ArrayList<MusicInfo>>() {
            @Override
            public void onChanged(ArrayList<MusicInfo> localMusicInfos) {
                if (localMusicInfoList == null){
                    localMusicInfoList = localMusicInfos;
                    ListView listView = getView().findViewById(R.id.home_local_music_list);
                    listView.setOnItemClickListener(onItemClickListener);
                    adapter = new MusicBriefAdapter(getContext(), localMusicInfos);
                    listView.setAdapter(adapter);
                }else{
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private ListView.OnItemClickListener onItemClickListener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LocalMusicInfo localMusicInfo = (LocalMusicInfo)localMusicInfoList.get(position);
            if (firstTimePlay){
                musicCoreService.playMusic(localMusicInfo, localMusicInfoList);
                firstTimePlay = false;
            }else{
                musicCoreService.playMusic(localMusicInfo);
            }
        }
    };

    /**
     * MusicPlayService服务建立成功的事件回调
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicCoreService.MusicCoreServiceBinder binder = (MusicCoreService.MusicCoreServiceBinder)service;
            musicCoreService = binder.getService();
            musicCoreService.addOnStateChangeListener(onStateChangeListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private MusicCoreService.OnStateChangeListener onStateChangeListener = new MusicCoreService.OnStateChangeListener() {
        private int defaultTextColor = 0;
        @Override
        public void onMusicListChanged(ArrayList<MusicInfo> musicList) {
            //TODO dehighlight item
        }

        @Override
        public void onMusicChanged(MusicInfo newMusicInfo, int index) {
            if (adapter != null){
                adapter.setHighLightItem(index);
            }
        }

        @Override
        public void onPaused() {
            //ignored
        }

        @Override
        public void onStoped() {
            //ignored
        }

        @Override
        public void onResumed(MusicInfo resumeMusicInfo) {
            //ignored
        }

        @Override
        public void onPositionChanged(int posotion, int duration) {
            //ignored
        }

        @Override
        public void onPlayMethodChanged(int method) {
            //ignored
        }
    };
}