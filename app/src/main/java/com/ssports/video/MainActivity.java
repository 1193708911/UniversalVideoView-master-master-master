package com.ssports.video;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.example.admin.videocontroller.R;
import com.ssports.video.controller.VideoController;

public class MainActivity extends AppCompatActivity {

    private VideoController mVideView;
    public static final String VIDEO_URL = "http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4";

    public static final String VIDEO_PIC = "http://img4.jiecaojingxuan.com/2016/3/14/2204a578-609b-440e-8af7-a0ee17ff3aee.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mVideView = findViewById(R.id.videView);
        mVideView.setUpVideoUrl(VIDEO_URL, VIDEO_PIC);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mVideView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVideView.onConfigScreenChanged(newConfig);
    }
}
