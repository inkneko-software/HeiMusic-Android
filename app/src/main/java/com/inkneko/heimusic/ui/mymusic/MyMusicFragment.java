package com.inkneko.heimusic.ui.mymusic;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.button.MaterialButton;
import com.inkneko.heimusic.R;
import com.inkneko.heimusic.storage.localmusic.LocalMusicScanner;

public class MyMusicFragment extends Fragment {
    private MyMusicViewModel myMusicViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myMusicViewModel =
                ViewModelProviders.of(this).get(MyMusicViewModel.class);
        View root = inflater.inflate(R.layout.fragment_mymusic, container, false);
        MaterialButton scanButton = root.findViewById(R.id.mymusic_framgment_scan_button);
        scanButton.setOnClickListener(onScanMusicClickedListener);
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
        });
    }
}