package com.ssports.video.utils;

import android.content.Context;
import android.media.AudioManager;

public class VolumeUtils {

    public static int MAX_VOLUME;
    public int currentVolume;
    public AudioManager mAudioManager;

    public VolumeUtils(Context mContext) {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        this.MAX_VOLUME = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    //设置当前音量大小
    public void setCurrentVolume(int currentVolume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
    }
    //获取最大音量
    public int getMaxVolume() {
        return MAX_VOLUME;
    }
    //获取当前的音量
    public int getCurrentVolume() {
        this.currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentVolume;
    }
}
