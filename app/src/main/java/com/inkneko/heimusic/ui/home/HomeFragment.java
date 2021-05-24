package com.inkneko.heimusic.ui.home;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inkneko.heimusic.MusicDetailActivity;
import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.service.MusicCoreService;
import com.inkneko.heimusic.storage.localmusic.LocalMusic;
import com.inkneko.heimusic.ui.adapter.MusicBriefAdapter;
import com.inkneko.heimusic.ui.adapter.MusicBriefViewHolder;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ArrayList<MusicInfo> localMusicInfoList;
    MusicBriefAdapter adapter;
    MusicCoreService musicCoreService;
    TextView emptyNoticeTextView;

    //设定当前播放列表是否为第一次播放，即MusicService中是否没有当前的音乐列表
    boolean firstTimePlay = true;
    boolean needScan = true;
    RecyclerView recyclerView;
    MusicBriefViewHolder lastHighLightedViewHolder;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        emptyNoticeTextView = root.findViewById(R.id.home_fragment_notice_textview);
        loadLocalMusicResources();


        //MusicPlayService服务创建
        Intent intent = new Intent(getActivity(), MusicCoreService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void loadLocalMusicResources(){
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getCount().observe(getViewLifecycleOwner(), (count)->{
            if (count != 0){
                emptyNoticeTextView.setVisibility(View.GONE);
                homeViewModel.getLocalMusicInfoList().observe(getViewLifecycleOwner(), new Observer<ArrayList<MusicInfo>>() {
                    int lastSize = 0;
                    @Override
                    public void onChanged(ArrayList<MusicInfo> localMusicInfos) {
                        int updateNum = localMusicInfos.size() - lastSize;
                        if (localMusicInfoList == null){
                            localMusicInfoList =  localMusicInfos;
                            recyclerView = getView().findViewById(R.id.home_local_music_list);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),layoutManager.getOrientation()));
                            adapter = new MusicBriefAdapter(new MusicBriefAdapter.MusicBriefDiff(), onItemClickListener);
                            adapter.submitList(localMusicInfoList);
                            recyclerView.setAdapter(adapter);
                        }else{
                            if (lastSize != 0){
                                Log.i("adapter-seq", String.format("%d, %d", lastSize - 1, updateNum));
                                adapter.notifyItemRangeChanged(lastSize - 1, updateNum);
                            }else{
                                Log.i("adapter-seq", String.format("%d, %d", 0, updateNum));
                                adapter.notifyItemRangeChanged(0, updateNum);
                            }
                        }
                        lastSize = localMusicInfos.size();
                    }
                });
            }
        });
    }

    private MusicBriefViewHolder.OnItemClickedListener onItemClickListener = new MusicBriefViewHolder.OnItemClickedListener() {
        @Override
        public void onClicked(View view, int position) {
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
            if (recyclerView != null){
                int selectedPos = MusicBriefViewHolder.getSelectedPosition();
                if (selectedPos != -1){
                    MusicBriefViewHolder selectedViewHolder = (MusicBriefViewHolder) recyclerView.findViewHolderForAdapterPosition(selectedPos);
                    if (selectedViewHolder != null){
                        selectedViewHolder.setItemViewHighLighted(false);
                    }
                }

                MusicBriefViewHolder viewHolder = (MusicBriefViewHolder) recyclerView.findViewHolderForAdapterPosition(index);
                if (viewHolder != null){
                    viewHolder.setItemViewHighLighted(true);
                }

                MusicBriefViewHolder.setSelectedPosition(index);
                /*if (lastHighLightedViewHolder != null && lastHighLightedViewHolder != viewHolder){
                    lastHighLightedViewHolder.setItemViewHighLighted(false);
                }
                lastHighLightedViewHolder = viewHolder;*/
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

    @Override
    public void onDestroyView() {
        musicCoreService.removeOnStateChangeListener(onStateChangeListener);
        super.onDestroyView();
        for(MusicInfo info : localMusicInfoList){
            if (info instanceof LocalMusicInfo){
                ((LocalMusicInfo) info).onDestory();
            }
        }
    }
}