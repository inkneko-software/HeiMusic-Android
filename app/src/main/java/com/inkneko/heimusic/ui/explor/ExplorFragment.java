package com.inkneko.heimusic.ui.explor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.inkneko.heimusic.MusicDetailActivity;
import com.inkneko.heimusic.R;
import com.inkneko.heimusic.entity.LocalMusicInfo;
import com.inkneko.heimusic.entity.MusicAlbum;
import com.inkneko.heimusic.entity.MusicInfo;
import com.inkneko.heimusic.entity.RemoteMusicInfo;
import com.inkneko.heimusic.service.MusicCoreService;
import com.inkneko.heimusic.ui.adapter.MusicBriefAdapter;
import com.inkneko.heimusic.ui.adapter.MusicBriefViewHolder;
import com.inkneko.heimusic.ui.home.HomeFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import jp.wasabeef.blurry.Blurry;
import jp.wasabeef.blurry.internal.Blur;
import jp.wasabeef.blurry.internal.BlurFactor;

public class ExplorFragment extends Fragment {

    private ExplorViewModel explorViewModel;
    private ImageView albumArtImageView;
    private TextView albumNameTextView;
    private TextView albumCreatorTextView;
    private TextView albumCreateDateTextView;
    private RecyclerView recyclerView;
    private View briefWrapView;
    private Bitmap defaultAlbumArt;
    private MusicBriefAdapter adapter;

    private MusicAlbum musicAlbum;


    private BlurFactor factor;

    private MusicCoreService musicCoreService;
    private boolean firstTimePlay = true;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        explorViewModel = new ViewModelProvider(this).get(ExplorViewModel.class);
        View root = inflater.inflate(R.layout.fragment_explor, container, false);

        briefWrapView = root.findViewById(R.id.fragment_explor_brief_wrap);
        albumArtImageView = root.findViewById(R.id.album_detail_album_art);
        albumNameTextView = root.findViewById(R.id.album_detail_album_name);
        albumCreatorTextView = root.findViewById(R.id.album_detail_album_creator);
        albumCreateDateTextView = root.findViewById(R.id.album_detail_album_create_date);
        recyclerView = root.findViewById(R.id.album_detail_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),layoutManager.getOrientation()));
        recyclerView.setLayoutManager(layoutManager);
        explorViewModel.getMusicAlbum(onFailedLoaddingCallback).observe(this, observer);

        defaultAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_albumart);
        factor = new BlurFactor();
        factor.radius = 20;
        factor.sampling = 5;
        factor.height = defaultAlbumArt.getHeight();
        factor.width = defaultAlbumArt.getWidth();
        factor.color = Color.argb(70, 0, 0, 0);

        //MusicPlayService服务创建
        Intent intent = new Intent(getActivity(), MusicCoreService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        return root;
    }

    Observer<MusicAlbum> observer = new Observer<MusicAlbum>() {
        @Override
        public void onChanged(MusicAlbum musicAlbum) {
            Glide.with(getContext()).asBitmap().load(musicAlbum.getAlbumArtUrl())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            setBlurBackground(resource);
                            return false;
                        }
                    })
                    .placeholder(R.drawable.default_albumart)
                    .into(albumArtImageView);
            albumNameTextView.setText(musicAlbum.getAlbumName());
            albumCreatorTextView.setText(musicAlbum.getAlbumCreator());
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            albumCreateDateTextView.setText(fmt.format(new Date(musicAlbum.getCreateTimestamp())));
            adapter = new MusicBriefAdapter(new MusicBriefAdapter.MusicBriefDiff(), onItemClickListener);
            adapter.submitList(musicAlbum.getSongList());
            recyclerView.setAdapter(adapter);
            ExplorFragment.this.musicAlbum = musicAlbum;
        }
    };

    private void setBlurBackground(Bitmap preparedBitmap){
        factor.height = preparedBitmap.getHeight();
        factor.width = preparedBitmap.getWidth();
        preparedBitmap = Blur.of(getContext(),preparedBitmap, factor);
        Drawable drawable = new BitmapDrawable(getResources(), preparedBitmap);
        briefWrapView.setBackground(drawable);
    }

    private MusicBriefViewHolder.OnItemClickedListener onItemClickListener = new MusicBriefViewHolder.OnItemClickedListener() {
        @Override
        public void onClicked(View view, int position) {
            RemoteMusicInfo remoteMusicInfo = (RemoteMusicInfo)musicAlbum.getSongList().get(position);
            if (firstTimePlay){
                musicCoreService.playMusic(remoteMusicInfo, musicAlbum.getSongList());
                firstTimePlay = false;
            }else{
                musicCoreService.playMusic(remoteMusicInfo);
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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 加载失败时的响应函数
     */
    Consumer<String> onFailedLoaddingCallback = new Consumer<String>() {
        @Override
        public void accept(String s) {
            ExplorFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast msg = Toast.makeText(getContext(), s, Toast.LENGTH_LONG);
                    msg.show();
                    albumNameTextView.setText("加载专辑失败");
                    albumNameTextView.setTextColor(getContext().getResources().getColor(R.color.colorBlack));
                }
            });
        }
    };

}