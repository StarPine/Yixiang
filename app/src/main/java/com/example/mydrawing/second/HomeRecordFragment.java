package com.example.mydrawing.second;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mydrawing.R;

/**
 * 记录fragment
 * Created by Jerry on 2019/4/6.
 */


public class HomeRecordFragment extends Fragment {


    Activity activity;

    public void  setActivity(Activity mainActivity2) {
        activity = mainActivity2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.record_fragment,null);
        return view;
    }
}
