package com.ssports.video.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.admin.videocontroller.R;
import com.ssports.video.utils.BritenessUtils;
import com.ssports.video.utils.ScreenUtils;
import com.ssports.video.utils.TimeUtils;
import com.ssports.video.utils.VolumeUtils;
import com.ssports.video.view.UniVideoView;


//播放器适配mediaController
public class VideoController extends FrameLayout {

    private View rootView;
    private UniVideoView itemVideoView;
    private Context mContext;
    private FrameLayout mSurfaceContainer;
    private TextView mCurrent;
    private SeekBar mProgress;
    private TextView mTotal;
    private ImageView mFullscreen;
    private LinearLayout mLayoutBottom;
    private LinearLayout mLayoutTop;
    private ProgressBar mLoading;
    private ImageView mStart;
    private ImageView mThumb;
    private String videoUrl;
    private boolean isFullSceen = false;
    private String videoPic;
    private float mDownX;
    private float mDownY;
    private float currentVolume = -1;
    private float currentBriteness = -1;
    private int currentPosition = 0;
    private int totalPosition = 1000;
    public int mSceenWidth_2;
    public int mScreenHeight;
    private boolean isPause = true;
    private BritenessUtils britenessUtils;
    private VolumeUtils volumeUtils;
    protected boolean mChangeVolume;
    protected boolean mChangePosition;
    protected boolean mChangeBrightness;
    public static final int TIME_PROGRESS = 1;
    public static final int SHOW_HIDE_CONTROLLER = 2;
    public static final int SHOW_HIDE_DURATION = 5000;

    public static final int THHOLD = 30;//最小滑动距离
    //初始化Handler
    Handler UIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case TIME_PROGRESS:
                    showProgress();
                    UIHandler.sendEmptyMessage(TIME_PROGRESS);
                    break;
                case SHOW_HIDE_CONTROLLER:
                    showOrHideControler();
                    break;
            }
        }
    };


    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
    }

    private void initView() {
        rootView = LayoutInflater.from(mContext).inflate(R.layout.jc_layout_base, this);
        itemVideoView = (UniVideoView) LayoutInflater.from(mContext).inflate(R.layout.layout_video_view, null);
        this.mSurfaceContainer = rootView.findViewById(R.id.surface_container);
        this.mCurrent = rootView.findViewById(R.id.current);
        this.mProgress = rootView.findViewById(R.id.progress);
        this.mTotal = rootView.findViewById(R.id.total);
        this.mFullscreen = rootView.findViewById(R.id.fullscreen);
        this.mLayoutBottom = rootView.findViewById(R.id.layout_bottom);
        this.mLayoutTop = rootView.findViewById(R.id.layout_top);
        this.mLoading = rootView.findViewById(R.id.loading);
        this.mStart = rootView.findViewById(R.id.start);
        this.mThumb = rootView.findViewById(R.id.thumb);
        if (mSurfaceContainer.getChildCount() > 0) {
            mSurfaceContainer.removeAllViews();
        }
        mSurfaceContainer.addView(itemVideoView);
        mSceenWidth_2 = ScreenUtils.getSceenWidth(mContext) / 2;
        mScreenHeight = ScreenUtils.getScreenHeight(mContext);
        britenessUtils = new BritenessUtils((Activity) mContext);
        volumeUtils = new VolumeUtils(mContext);
        bindListener();
    }

    private void bindListener() {
        mStart.setOnClickListener(onClickListener);
        mFullscreen.setOnClickListener(onClickListener);
        mSurfaceContainer.setOnTouchListener(onTouchListener);
        itemVideoView.setOnInfoListener(onInfoListener);
        itemVideoView.setOnErrorListener(onErrorListener);
        mProgress.setOnSeekBarChangeListener(onSeekBarChangeListener);
        itemVideoView.setOnCompletionListener(onCompletionListener);
        itemVideoView.setOnPreparedListener(onPreparedListener);
    }

    //显示或者隐藏mediaController
    private void showOrHideControler() {
        if (currentPosition != 0) {
            if (mLayoutBottom.getVisibility() == View.VISIBLE) {
                mLayoutBottom.setVisibility(GONE);
                mStart.setVisibility(GONE);
                UIHandler.removeMessages(SHOW_HIDE_CONTROLLER);
            } else if (mLayoutBottom.getVisibility() == View.GONE) {
                mLayoutBottom.setVisibility(VISIBLE);
                mStart.setVisibility(VISIBLE);
                UIHandler.removeMessages(SHOW_HIDE_CONTROLLER);
                UIHandler.sendEmptyMessageDelayed(SHOW_HIDE_CONTROLLER, SHOW_HIDE_DURATION);
            }
        }
    }

    //更新当前进度
    private void showProgress() {
        if (currentPosition != itemVideoView.getCurrentPosition()) {
            if (mLoading.getVisibility() == View.VISIBLE) {
                mLoading.setVisibility(GONE);
            }

        }
        currentPosition = itemVideoView.getCurrentPosition();
        totalPosition = itemVideoView.getDuration();
        Log.e("-----------", currentPosition + "");
        mCurrent.setText(TimeUtils.stringForTime(currentPosition));
        mTotal.setText(TimeUtils.stringForTime(totalPosition));
        mProgress.setMax(totalPosition);
        mProgress.setProgress(currentPosition);
    }


    MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            isPause = true;
            return false;
        }
    };

    MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mLoading.setVisibility(VISIBLE);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    mLoading.setVisibility(GONE);
                    break;
            }
            return true;
        }
    };
    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            isPause = true;
            //移除当前的状态
            UIHandler.removeMessages(TIME_PROGRESS);
            UIHandler.removeMessages(SHOW_HIDE_CONTROLLER);
            mLayoutBottom.setVisibility(VISIBLE);
            mStart.setVisibility(VISIBLE);
            mThumb.setVisibility(VISIBLE);
            mStart.setImageResource(R.drawable.jc_play_normal);
            currentPosition = 0;
            mProgress.setProgress(0);
            itemVideoView.stopPlayback();
            setUpVideoUrl(videoUrl, videoPic);

        }
    };

    //缓冲回掉
    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(final MediaPlayer mp) {
            mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    // 获得当前播放时间和当前视频的长度
                    // 设置进度条的次要进度，表示视频的缓冲进度
                    int duration = mp.getDuration();
                    float currentPercent = percent * duration / 100;
                    mProgress.setSecondaryProgress((int) currentPercent);
                }
            });
        }
    };


    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (itemVideoView != null) {
                    itemVideoView.seekTo(progress);
                }
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.start:
                    if (itemVideoView.isPlaying()) {
                        isPause = true;
                        itemVideoView.pause();
                        changeStartImage(false);
                        UIHandler.removeMessages(TIME_PROGRESS);
                        UIHandler.removeMessages(SHOW_HIDE_CONTROLLER);
                    } else {
                        if (currentPosition > 0) {
                            itemVideoView.start();
                        } else {
                            start();
                        }
                        isPause = false;
                        changeStartImage(true);
                        UIHandler.sendEmptyMessageDelayed(SHOW_HIDE_CONTROLLER, SHOW_HIDE_DURATION);
                        UIHandler.sendEmptyMessage(TIME_PROGRESS);
                    }
                    break;
                case R.id.fullscreen:

                    switchFullSceenOrPortaint();

                    break;
            }
        }
    };


    OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float currentX = event.getX();
            float currentY = event.getY();
            int id = v.getId();
            if (id == R.id.surface_container) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = currentX;
                        mDownY = currentY;
                        mChangeVolume = false;
                        mChangePosition = false;
                        mChangeBrightness = false;
                        if (mThumb.getVisibility() == GONE) {
                            UIHandler.sendEmptyMessage(SHOW_HIDE_CONTROLLER);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = currentX - mDownX;
                        float deltaY = currentY - mDownY;
                        float absDeltaX = Math.abs(deltaX);
                        float absDeltaY = Math.abs(deltaY);

                        if (isFullSceen) {
                            if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                                if (absDeltaX > THHOLD || absDeltaY > THHOLD) {
                                    if (absDeltaX >= THHOLD) {
                                        // 全屏模式下的CURRENT_STATE_ERROR状态下,不响应进度拖动事件.
                                        // 否则会因为mediaplayer的状态非法导致App Crash
                                        mChangePosition = true;
                                    } else {
                                        //如果y轴滑动距离超过设置的处理范围，那么进行滑动事件处理
                                        if (mDownX < mSceenWidth_2) {//左侧改变亮度
                                            mChangeBrightness = true;

                                        } else {//右侧改变声音
                                            mChangeVolume = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (mChangePosition) {
                            changeVideoCurrentPosition(deltaX);
                        }
                        if (mChangeVolume) {
                            changeVideoVolume(deltaY);
                        }
                        if (mChangeBrightness) {
                            changeVideoBriteness(deltaY);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        endGesture();
                        if (mChangePosition) {
                            itemVideoView.seekTo((int) seekPosition);
                            long duration = itemVideoView.getDuration();
                            int progress = (int) (seekPosition * 100 / (duration == 0 ? 1 : duration));
                            mProgress.setProgress(progress);
                        }
                        break;
                }
            }
            return true;
        }
    };


    //更改屏幕的声音
    private void changeVideoVolume(float deltaY) {
        if (currentVolume == -1) {
            currentVolume = volumeUtils.getCurrentVolume();
        }
        float maxVolume = volumeUtils.getMaxVolume();
        float percent = (-deltaY / mScreenHeight) * maxVolume;
        float mCurrentVolume = Math.min(currentVolume + percent, volumeUtils.getMaxVolume());
        int volumePercent = (int) (currentVolume * 100 / maxVolume + -deltaY * 100 * 2 / mScreenHeight);
        if (mCurrentVolume <= 0) {
            mCurrentVolume = 0f;
        }
        showVolumeDialog(-deltaY, volumePercent);
        volumeUtils.setCurrentVolume((int) mCurrentVolume);

    }

    float seekPosition;

    //更改当前视频进度
    private void changeVideoCurrentPosition(float deltaX) {
        float duration = itemVideoView.getDuration();
        float curPostion = itemVideoView.getCurrentPosition();
        seekPosition = (int) (curPostion + deltaX * duration / mSceenWidth_2 * 2);
        if (seekPosition >= duration) {
            seekPosition = duration;
        }
        if (seekPosition <= 0) {
            seekPosition = 0;
        }
        showProgressDialog(deltaX, TimeUtils.stringForTime((int) seekPosition), (int) seekPosition, TimeUtils.stringForTime((int) duration), (int) duration);
//        itemVideoView.seekTo((int) seekPosition);

    }

    //更改屏幕的亮度
    private void changeVideoBriteness(float deltaY) {
        if (currentBriteness == -1) {
            currentBriteness = britenessUtils.getCurrentBrite();
        }
        float mCurrentBriteness = Math.min(currentBriteness + (-deltaY / mScreenHeight), britenessUtils.MAX_BRITEN);
        if (mCurrentBriteness <= 0.1) {
            mCurrentBriteness = 0.1f;
        }
        int britenessPercent = (int) (mCurrentBriteness * 100 / britenessUtils.MAX_BRITEN + (-deltaY) * 100 / mScreenHeight);
        showBritenessDialog(-deltaY, britenessPercent);
        britenessUtils.setCurrentBrite(mCurrentBriteness);
    }

    private void endGesture() {
        dismissProgressDialog();
        dismissVolumeDialog();
        dismissBritenessDialog();
        currentBriteness = -1;
        currentVolume = -1;
        currentPosition = 0;

    }


    //更改是否播放按钮图片
    private void changeStartImage(boolean isPlaying) {
        if (isPlaying) {
            mStart.setImageResource(R.drawable.jc_pause_normal);
        } else {
            mStart.setImageResource(R.drawable.jc_play_normal);
        }
    }

    private void switchFullSceenOrPortaint() {
        Activity activity = (Activity) mContext;
        if (isFullSceen) {
//竖屏
            isFullSceen = false;
            mFullscreen.setImageResource(R.drawable.jc_enlarge);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        } else {
//横屏
            isFullSceen = true;
            mFullscreen.setImageResource(R.drawable.jc_shrink);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }


    }


    public void onConfigScreenChanged(Configuration newConfig) {
        resetLayoutParams(newConfig.orientation);
    }

    //横屏切换到竖屏时候修改布局大小
    private void resetLayoutParams(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            LayoutParams params = (LayoutParams) this.getLayoutParams();
            params.height = ScreenUtils.getScreenHeight(mContext);
            params.width = ScreenUtils.getSceenWidth(mContext);
            this.setLayoutParams(params);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            LayoutParams params = (LayoutParams) this.getLayoutParams();
            params.height = 400;
            params.width = ScreenUtils.getSceenWidth(mContext);
            this.setLayoutParams(params);
        }

    }


    //播放
    public void start() {
        if (TextUtils.isEmpty(videoUrl)) {
            throw new NullPointerException("视频地址不能为空");
        }
        itemVideoView.start();
        mLoading.setVisibility(VISIBLE);
        mThumb.setVisibility(GONE);

    }

    //设置videourl
    public void setUpVideoUrl(String videoUrl, String videoPic) {
        this.videoUrl = videoUrl;
        this.videoPic = videoPic;
        itemVideoView.setVideoPath(videoUrl);
        Glide.with(mContext).load(videoPic).into(mThumb);
    }


    //展示当前的进度
    protected Dialog mProgressDialog;
    protected ProgressBar mDialogProgressBar;
    protected TextView mDialogSeekTime;
    protected TextView mDialogTotalTime;
    protected ImageView mDialogIcon;

    public void showProgressDialog(float deltaX, String seekTime, int seekTimePosition, String totalTime, int totalTimeDuration) {
        if (mProgressDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.jc_progress_dialog, null);
            mDialogProgressBar = localView.findViewById(R.id.duration_progressbar);
            mDialogSeekTime = localView.findViewById(R.id.tv_current);
            mDialogTotalTime = localView.findViewById(R.id.tv_duration);
            mDialogIcon = localView.findViewById(R.id.duration_image_tip);
            mProgressDialog = new Dialog(getContext(), R.style.jc_style_dialog_progress);
            mProgressDialog.setContentView(localView);
            mProgressDialog.getWindow().addFlags(Window.FEATURE_ACTION_BAR);
            mProgressDialog.getWindow().addFlags(32);
            mProgressDialog.getWindow().addFlags(16);
            mProgressDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mProgressDialog.getWindow().getAttributes();
            localLayoutParams.gravity = 49;
            localLayoutParams.y = getResources().getDimensionPixelOffset(R.dimen.jc_progress_dialog_margin_top);
            mProgressDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        mDialogSeekTime.setText(seekTime);
        mDialogTotalTime.setText(" / " + totalTime);
        mDialogProgressBar.setProgress(totalTimeDuration <= 0 ? 0 : (seekTimePosition * 100 / totalTimeDuration));
        if (deltaX > 0) {
            mDialogIcon.setBackgroundResource(R.drawable.jc_forward_icon);
        } else {
            mDialogIcon.setBackgroundResource(R.drawable.jc_backward_icon);
        }

    }

    public void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    protected Dialog mVolumeDialog;
    protected ProgressBar mDialogVolumeProgressBar;

    public void showVolumeDialog(float deltaY, int volumePercent) {
        if (mVolumeDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.jc_volume_dialog, null);
            mDialogVolumeProgressBar = ((ProgressBar) localView.findViewById(R.id.volume_progressbar));
            mVolumeDialog = new Dialog(getContext(), R.style.jc_style_dialog_progress);
            mVolumeDialog.setContentView(localView);
            mVolumeDialog.getWindow().addFlags(8);
            mVolumeDialog.getWindow().addFlags(32);
            mVolumeDialog.getWindow().addFlags(16);
            mVolumeDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mVolumeDialog.getWindow().getAttributes();
            localLayoutParams.x = mSceenWidth_2 * 2 - 48;
            localLayoutParams.y = 40;
            mVolumeDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mVolumeDialog.isShowing()) {
            mVolumeDialog.show();
        }

        mDialogVolumeProgressBar.setProgress(volumePercent);
    }

    public void dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog.dismiss();
        }
    }


    /**
     * 左侧亮度
     */
    protected Dialog mBritenessDialog;
    protected ProgressBar mDialogBritenessProgressBar;

    public void showBritenessDialog(float deltaY, int volumePercent) {
        if (mBritenessDialog == null) {
            View localView = LayoutInflater.from(getContext()).inflate(R.layout.jc_volume_dialog, null);
            mDialogBritenessProgressBar =  localView.findViewById(R.id.volume_progressbar);
            mBritenessDialog = new Dialog(getContext(), R.style.jc_style_dialog_progress);
            mBritenessDialog.setContentView(localView);
            mBritenessDialog.getWindow().addFlags(8);
            mBritenessDialog.getWindow().addFlags(32);
            mBritenessDialog.getWindow().addFlags(16);
            mBritenessDialog.getWindow().setLayout(-2, -2);
            WindowManager.LayoutParams localLayoutParams = mBritenessDialog.getWindow().getAttributes();
            localLayoutParams.gravity = 19;
            localLayoutParams.x = getContext().getResources().getDimensionPixelOffset(R.dimen.jc_volume_dialog_margin_left);
            mBritenessDialog.getWindow().setAttributes(localLayoutParams);
        }
        if (!mBritenessDialog.isShowing()) {
            mBritenessDialog.show();
        }

        mDialogBritenessProgressBar.setProgress(volumePercent);
    }

    public void dismissBritenessDialog() {
        if (mBritenessDialog != null) {
            mBritenessDialog.dismiss();
        }
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        if (itemVideoView != null && itemVideoView.isPlaying()) {
            currentPosition = itemVideoView.getCurrentPosition();
            isPause = false;
        }
        return super.onSaveInstanceState();

    }


    public void onResume() {
        if (itemVideoView != null && currentPosition > 0 && !isPause) {
            itemVideoView.start();
            itemVideoView.seekTo(currentPosition);
            UIHandler.sendEmptyMessage(TIME_PROGRESS);
            UIHandler.sendEmptyMessage(SHOW_HIDE_CONTROLLER);
        }

    }

    public void onPause() {
        if (itemVideoView != null && itemVideoView.isPlaying()) {
            currentPosition = itemVideoView.getCurrentPosition();
        }
    }


    public void onStop() {
        if (itemVideoView != null && itemVideoView.isPlaying()) {
            UIHandler.removeMessages(TIME_PROGRESS);
            UIHandler.removeMessages(SHOW_HIDE_CONTROLLER);
            itemVideoView.pause();
        }
    }


    public void onDestroy() {
        if (itemVideoView != null) {
            itemVideoView.stopPlayback();
            itemVideoView = null;
        }
    }




}
