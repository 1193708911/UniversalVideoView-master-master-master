package com.ssports.video.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

public class BritenessUtils {
    public Window mWindow;


    public float currentBrite;
    public static final float MAX_BRITEN = 1f;

    public BritenessUtils(Activity mContext) {
        mWindow = mContext.getWindow();
    }
//获取屏幕当前亮度
    public float getCurrentBrite() {
        this.currentBrite = mWindow.getAttributes().screenBrightness;
        return currentBrite;
    }

    //设置屏幕亮度
    public void setCurrentBrite(float currentBrite) {
        this.currentBrite = currentBrite;
        WindowManager.LayoutParams params = mWindow.getAttributes();
        params.screenBrightness = currentBrite;
        mWindow.setAttributes(params);
    }
}
