package com.ssports.video.utils;

import android.content.Context;

public class ScreenUtils {
    //获取屏幕的高度
    public static int getScreenHeight(Context mContext) {

        return mContext.getResources().getDisplayMetrics().heightPixels;
    }

    //获取屏幕的宽度
    public static int getSceenWidth(Context mContext) {

        return mContext.getResources().getDisplayMetrics().widthPixels;
    }
}
