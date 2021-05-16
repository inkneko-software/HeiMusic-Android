package com.inkneko.heimusic.ui.explor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExplorViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ExplorViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is explor fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}