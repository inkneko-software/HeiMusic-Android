package com.inkneko.heimusic.ui.explor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.inkneko.heimusic.R;

public class ExplorFragment extends Fragment {

    private ExplorViewModel explorViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        explorViewModel =
                ViewModelProviders.of(this).get(ExplorViewModel.class);
        View root = inflater.inflate(R.layout.fragment_explor, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        explorViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}