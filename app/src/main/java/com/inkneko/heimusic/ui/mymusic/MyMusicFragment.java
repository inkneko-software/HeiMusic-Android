package com.inkneko.heimusic.ui.mymusic;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.button.MaterialButton;
import com.inkneko.heimusic.R;
import com.inkneko.heimusic.storage.localmusic.LocalMusicDatabase;
import com.inkneko.heimusic.storage.localmusic.LocalMusicScanner;

public class MyMusicFragment extends Fragment {
    private MyMusicViewModel myMusicViewModel;
    private TextView briefTextView;
    private SharedPreferences prefs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myMusicViewModel =
                ViewModelProviders.of(this).get(MyMusicViewModel.class);
        View root = inflater.inflate(R.layout.fragment_mymusic, container, false);
        MaterialButton scanButton = root.findViewById(R.id.mymusic_fragment_scan_button);
        scanButton.setOnClickListener(onScanMusicClickedListener);

        briefTextView = root.findViewById(R.id.mymusic_fragment_brief);
        prefs = getActivity().getSharedPreferences(
                "com.inkneko.heimusic", Context.MODE_PRIVATE);


        int  count = prefs.getInt("cachedCount", 0);
        briefTextView.setText(String.format("当前扫描到的音乐数量：%d", count));
        return root;
    }


    MaterialButton.OnClickListener onScanMusicClickedListener = new MaterialButton.OnClickListener(){
        @Override
        public void onClick(View v) {
            //自安卓6.0起敏感权限需要进一步动态申请
            //https://developer.android.com/training/permissions/requesting
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }else{
                doScan();
            }
        }
    };

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    doScan();
                }
            });

    private void doScan(){
        LocalMusicScanner scanner = new LocalMusicScanner(getContext());
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("扫描中...");
        progressDialog.setIcon(R.drawable.ic_search_black_24dp);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        scanner.scan().observe(getViewLifecycleOwner(), (status)->{
            progressDialog.setProgress(status.first);
            progressDialog.setMax(status.second);
            if (status.first.equals(status.second)){
                progressDialog.setTitle("扫描完成");
                prefs.edit().putInt("cachedCount", status.second).apply();
                briefTextView.setText(String.format("当前扫描到的音乐数量：%d", status.second));
            }

        });
    }
}