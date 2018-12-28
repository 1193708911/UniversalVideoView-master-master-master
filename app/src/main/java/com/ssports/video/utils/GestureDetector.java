package com.ssports.video.utils;

import android.os.Handler;
import android.view.MotionEvent;

import com.ssports.video.controller.VideoController;

public class GestureDetector extends android.view.GestureDetector.SimpleOnGestureListener {

    private Handler UIHandler;

    public GestureDetector(Handler UIHandler) {
        super();
        this.UIHandler = UIHandler;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        UIHandler.sendEmptyMessage(VideoController.SHOW_HIDE_CONTROLLER);
        return super.onSingleTapUp(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {




        return super.onScroll(e1, e2, distanceX, distanceY);
    }
}
