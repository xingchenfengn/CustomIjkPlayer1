package com.xingzhiqiao.xzqplayer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.squareup.picasso.Picasso;
import com.xingzhiqiao.xzqplayer.util.AndroidTools;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import static android.content.Context.AUDIO_SERVICE;

/**
 * 自定义视频播放器，主要实现播放控制
 * Created by xingzhiqiao on 2016/12/28.
 */

public class CnrVideoView extends BaseVideoView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {


    private RelativeLayout mControLayout;

    private RelativeLayout mTopControLayout;
    private LinearLayout mBottomControLayout;

    //横竖屏切换按钮
    private ImageView mSwitchScreenImg;

    private ImageView mBackImg, mThumbImg, mLoadingImg, mPauseImg, mPlayImg;


    private LinearLayout mRePlayLayout;
    /**
     * 进度条
     */
    private SeekBar mSeekBar;

    /**
     * 底部进度条
     */
    private ProgressBar mBottomPb;

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener;

    public static final int SHOW_TOP_CONTROL_LAYOUT = 1001;
    public static final int DISMIESS_TOP_CONTROL_LAYOUT = 1002;
    public static final int CONTROL_LAYOUT_SHOW_TIME = 5000;
    public static final int BRIGHTNESS_FADE_OUT_INFO = 1003;

    public CnrVideoView(Context context) {
        super(context);
    }

    public CnrVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CnrVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initVideoView(Context context) {
        super.initVideoView(context);

        LayoutInflater.from(mAppContext).inflate(R.layout.control_layout, this);
        mControLayout = (RelativeLayout) findViewById(R.id.contorl_layout);
        mTopControLayout = (RelativeLayout) findViewById(R.id.player_contorl_top_layout);
        mBottomControLayout = (LinearLayout) findViewById(R.id.player_contorl_bottom_layout);
        mSwitchScreenImg = (ImageView) findViewById(R.id.change_surfacesize_img);
        mBackImg = (ImageView) findViewById(R.id.player_control_backlayout);
        mSeekBar = (SeekBar) findViewById(R.id.player_seekbar);
        mBottomPb = (ProgressBar) findViewById(R.id.player_bottom_progressbar);
        mThumbImg = (ImageView) findViewById(R.id.player_thumb);
        mLoadingImg = (ImageView) findViewById(R.id.player_load_img);
        mPauseImg = (ImageView) findViewById(R.id.player_pause_img);
        mPlayImg = (ImageView) findViewById(R.id.player_play_img);
        mRePlayLayout = (LinearLayout) findViewById(R.id.player_replay_layout);

        mControLayout.setOnClickListener(this);
        mSwitchScreenImg.setOnClickListener(this);
        mBackImg.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mPlayImg.setOnClickListener(this);
        mPauseImg.setOnClickListener(this);
        mRePlayLayout.setOnClickListener(this);
        mControLayout.setOnTouchListener(this);
        showLoadingView();

        mAudioManager = (AudioManager) mAppContext.getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private Handler mHandler = new Handler(mAppContext.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_TOP_CONTROL_LAYOUT:
                    break;
                case DISMIESS_TOP_CONTROL_LAYOUT:
                    dismissTopLayout();
                    break;
            }
        }
    };


    public void setThumbImg(String thumbUrl) {
        if (thumbUrl != null) {
            mThumbImg.setVisibility(View.VISIBLE);
            Picasso.with(mAppContext).load(thumbUrl).into(mThumbImg);
        }

    }


    @Override
    public void onClick(View v) {
        int key = v.getId();
        if (key == R.id.contorl_layout) {
            clickScreen();
            return;
        }
        if (key == R.id.player_pause_img) {
            mHandler.removeMessages(DISMIESS_TOP_CONTROL_LAYOUT);
            pause();
            mPauseImg.setVisibility(View.GONE);
            mPlayImg.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessageDelayed(DISMIESS_TOP_CONTROL_LAYOUT, CONTROL_LAYOUT_SHOW_TIME);

        }
        if (key == R.id.player_play_img) {
            mHandler.removeMessages(DISMIESS_TOP_CONTROL_LAYOUT);
            start();
            mPlayImg.setVisibility(View.GONE);
            mPauseImg.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessageDelayed(DISMIESS_TOP_CONTROL_LAYOUT, CONTROL_LAYOUT_SHOW_TIME);
        }
        if (key == R.id.player_replay_layout) {
            this.resume();
        }

    }

    private int bufferPercent;

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
        super.onBufferingUpdate(iMediaPlayer, i);
        bufferPercent = i;
        setPlayTime(iMediaPlayer);
    }

    private void setPlayTime(IMediaPlayer iMediaPlayer) {
        if (iMediaPlayer == null) {
            return;
        }
        long duration = iMediaPlayer.getDuration();
        long currentPosition = iMediaPlayer.getCurrentPosition();
        if (duration > 0) {
//            mCurrentTimeTv.setText(DateUtils.getPlayTime(currentPosition));
//            mTotoalTimeTv.setText(DateUtils.getPlayTime(duration));
            mSeekBar.setMax((int) duration);
            mSeekBar.setProgress((int) currentPosition);
            mSeekBar.setSecondaryProgress((int) (duration * bufferPercent / 100));
            mBottomPb.setMax((int) duration);
            mBottomPb.setProgress((int) currentPosition);
            mBottomPb.setSecondaryProgress((int) (duration * bufferPercent / 100));
        }
    }

    @Override
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        super.setOnPreparedListener(l);
    }

    /**
     * 点击屏幕
     */
    private void clickScreen() {
        int topVisibity = mTopControLayout.getVisibility();
        if (topVisibity == View.VISIBLE) {
            dismissTopLayout();
        } else {
            showTopLayout();
        }
    }

    private boolean isPortrait;
    private boolean isLocked;

    private void dismissTopLayout() {
//        topAnimation = AnimationUtils.loadAnimation(this,
//                R.anim.player_translate_top);
//        bottomAnimation = AnimationUtils.loadAnimation(this,
//                R.anim.player_translate_bottom);
        if (!isPortrait) {
//            mTopControLayout.startAnimation(topAnimation);
//            mBottomControlLayout.startAnimation(bottomAnimation);
        }
        if (!isLocked) {
//            mLockImg.setVisibility(View.GONE);
        }
        mTopControLayout.setVisibility(View.GONE);
        mBottomControLayout.setVisibility(View.GONE);
        if (getDuration() > 0 && showBottomPg) {
            mBottomPb.setVisibility(View.VISIBLE);
        }
        if (isPlaying()) {
            mPauseImg.setVisibility(View.GONE);
        } else {
            mPlayImg.setVisibility(View.GONE);
        }
    }

    private void showTopLayout() {
        if (getDuration() > 0 && showBottomPg) {
            mBottomPb.setVisibility(View.GONE);
        }
        if (isPlaying()) {
            mPauseImg.setVisibility(View.VISIBLE);
        } else {
            //TODO 当缓冲时，点击屏幕会重叠
            mPlayImg.setVisibility(View.VISIBLE);
        }
        mHandler.removeMessages(DISMIESS_TOP_CONTROL_LAYOUT);
        mTopControLayout.setVisibility(View.VISIBLE);
        mBottomControLayout.setVisibility(View.VISIBLE);
        mHandler.sendEmptyMessageDelayed(DISMIESS_TOP_CONTROL_LAYOUT, CONTROL_LAYOUT_SHOW_TIME);
    }

    public SeekBar.OnSeekBarChangeListener getSeekBarChangeListener() {
        return mSeekBarChangeListener;
    }

    public void setSeekBarChangeListener(SeekBar.OnSeekBarChangeListener seekBarChangeListener) {
        this.mSeekBarChangeListener = mSeekBarChangeListener;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mSeekBarChangeListener != null) {
            mSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        originalProgress = seekBar.getProgress();
        mHandler.removeMessages(DISMIESS_TOP_CONTROL_LAYOUT);
        if (mSeekBarChangeListener != null) {
            mSeekBarChangeListener.onStartTrackingTouch(seekBar);
        }
        mHandler.removeMessages(DISMIESS_TOP_CONTROL_LAYOUT);
    }

    private int originalProgress;

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int arg1, int arg2) {

        switch (arg1) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                //节目正在加载，显示loadingLayout;
                showLoadingView();
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                //节目加载完成，隐藏loadingLayout;
                dismissLoadingView();
                break;
        }
        return true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.sendEmptyMessageDelayed(DISMIESS_TOP_CONTROL_LAYOUT,
                CONTROL_LAYOUT_SHOW_TIME);
        if (getDuration() > 0 && seekBar.getMax() > 0) {
            int position = seekBar.getProgress();
            if (position == seekBar.getMax()) {//到最后

            } else {
                seekTo(position);
            }
        }
        if (mSeekBarChangeListener != null) {
            mSeekBarChangeListener.onStopTrackingTouch(seekBar);
        }
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        super.onPrepared(iMediaPlayer);
        mThumbImg.setVisibility(View.GONE);
        mPauseImg.setVisibility(View.VISIBLE);
        dismissLoadingView();
        if (iMediaPlayer.getDuration() > 0) {//如果是点播显示进度条
            mSeekBar.setVisibility(View.VISIBLE);
        }
        mHandler.sendEmptyMessageDelayed(DISMIESS_TOP_CONTROL_LAYOUT, CONTROL_LAYOUT_SHOW_TIME);
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        super.onCompletion(iMediaPlayer);
        mThumbImg.setVisibility(View.VISIBLE);
        mRePlayLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 加载loading动画
     */
    private void showLoadingView() {
        mPauseImg.setVisibility(View.GONE);
        mPlayImg.setVisibility(View.GONE);
        mLoadingImg.setVisibility(View.VISIBLE);
        Animation mLoadingAnim = AnimationUtils.loadAnimation(mAppContext, R.anim.player_loading);
        mLoadingAnim.setInterpolator(new LinearInterpolator());
        mLoadingImg.startAnimation(mLoadingAnim);
    }

    /**
     * 隐藏动画
     */
    private void dismissLoadingView() {
        mLoadingImg.clearAnimation();
        mLoadingImg.setVisibility(View.GONE);
        if (mTopControLayout.getVisibility() == View.VISIBLE) {
            if (isPlaying()) {
                mPauseImg.setVisibility(View.VISIBLE);
            } else if (mPauseImg.getVisibility() != View.VISIBLE) {
                mPlayImg.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean showBottomPg = true;

    /**
     * 点播节目展示底部进度条，默认展示
     *
     * @param show
     */
    public void showBottomProgress(boolean show) {
        showBottomPg = show;
    }


    private float mTouchY, mTouchX, mVol;

    private AudioManager mAudioManager;

    private int mAudioMax;

    /**
     * 音量和亮度是否变化
     */
    private boolean mIsAudioOrBrightnessChanged;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        // 获取屏幕上的某个位置
        float y_changed = event.getRawY() - mTouchY;
        float x_changed = event.getRawX() - mTouchX;

        float coef;
        if (Math.abs(x_changed) < 15) {
            coef = Math.abs(y_changed) / 30;
        } else {
            coef = Math.abs(y_changed / x_changed);
        }
        float horCoef = Math.abs(x_changed / y_changed);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isLocked) {

                    mTouchY = event.getRawY();
                    mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mIsAudioOrBrightnessChanged = false;
                    // Seek
                    mTouchX = event.getRawX();
//                    currentPlayPos = mCurrentPosition;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isLocked && this != null) {
                    if (coef > 2) {
                        if (mTouchX > (AndroidTools.getScreenWidth(mAppContext) / 2)) {
                            onVScrollGesture(y_changed, false);
                        }
                        if (mTouchX < (AndroidTools.getScreenWidth(mAppContext) / 2)) {
                            onVScrollGesture(y_changed, true);
                        }
                    } else if (horCoef > 2 && getDuration() > 0) {
                        doTimeTouch(x_changed);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                last_y_changed = 0;
                if (!isLocked) {
//                    if (isSpeedMove) {
//                        isSpeedMove = false;
//                        viewControlHandler.sendEmptyMessageDelayed(SPEED_LAYOUT_FADE_OUT, 500);
//                        if (mBottomControlLayout.getVisibility() == View.VISIBLE) {
//                            viewControlHandler.sendEmptyMessageDelayed(SURFACEVIEW_FADE_OUT_INFO,
//                                    5000);
//                        }
//                        if (cnrVideoView != null) {
//                            cnrVideoView.seekTo(currentPlayPos);
//                        }
//                    }
                }
                break;
        }
        return mIsAudioOrBrightnessChanged;
    }

    /**
     * 快进变量
     */
    private int currentPlayPos;

    /**
     * 播放总长度
     */
    private int mLength = -1;

    private void doTimeTouch(float X_changed) {
        Log.d("OnTouch", "Xchanged" + X_changed);
        //如果顶部控制在显示，移除掉隐藏消息
//        if (mBottomControlLayout.getVisibility() == View.VISIBLE) {
//            viewControlHandler.removeMessages(SURFACEVIEW_FADE_OUT_INFO);
//        }
        if (isPlaying()) {
            if (X_changed > -15 && X_changed < 15) {
                return;
            }
            currentPlayPos = getCurrentPosition();
            mLength = getDuration();
//
//            if (X_changed >= 15) {// 快进
////                mSpeedImage.setImageResource(R.mipmap.player_hand_speed_right);
//                if (currentPlayPos + 15 * 1000 <= mLength) {
//                    currentPlayPos += 15 * 1000;
//                } else {// 如果快进到头倒退一秒
//                    currentPlayPos = mLength - 1 * 1000;
//                }
//            } else if (X_changed <= -15) {// 快退，用步长控制改变速度，可微调
//                mSpeedImage.setImageResource(R.mipmap.player_hand_speed_left);
//
//                if (currentPlayPos - 15 * 1000 > 0) {
//                    currentPlayPos -= 15 * 1000;
//                } else {
//                    currentPlayPos = 0;
//                }
//            }
//            mIsAudioOrBrightnessChanged = true;
//            isSpeedMove = true;
//            mSpeedLayout.setVisibility(View.VISIBLE);
//            mSpeedText.setText(DateUtils.getPlayTime(currentPlayPos) + "/"
//                    + DateUtils.getPlayTime(mLength));
        }
    }


    // y的改变量
    private float last_y_changed = 0;

    // y的改变量差值
    private float y_changed_Delta = 0;

    /**
     * 亮度
     */
    private float mBrightness = -1f;

    public void onVScrollGesture(float y_changed, boolean onLeft) {
        Log.d("OnTouch", "y_changed" + y_changed);
        if (isPlaying()) {
            mIsAudioOrBrightnessChanged = true;
            y_changed_Delta = y_changed - last_y_changed;
            if (onLeft) {
                // 调整亮度
                mBrightness = mActivity.getWindow().getAttributes().screenBrightness;
                if (mBrightness < 0.01f)
                    mBrightness = 0.01f;
                // 显示
//                mBrightImg.setImageResource(R.mipmap.video_brightness_bg);
//                mBrightControlLayout.setVisibility(View.VISIBLE);
//                float brightDelta = -y_changed_Delta
//                        / (mSurfaceYDisplayRange * 0.75f);
//                WindowManager.LayoutParams lpa = getWindow().getAttributes();
//                lpa.screenBrightness = lpa.screenBrightness + brightDelta;
//                if (lpa.screenBrightness > 1.0f) {
//                    lpa.screenBrightness = 1.0f;
//                } else if (lpa.screenBrightness < 0.01f) {
//                    lpa.screenBrightness = 0.01f;
//                }
//                getWindow().setAttributes(lpa);
//                ViewGroup.LayoutParams lp = mBrightPercentImg.getLayoutParams();
//                lp.width = (int) (findViewById(R.id.operation_full)
//                        .getLayoutParams().width * lpa.screenBrightness);
//                mBrightPercentImg.setLayoutParams(lp);
                mHandler.removeMessages(BRIGHTNESS_FADE_OUT_INFO);
                mHandler.sendEmptyMessageDelayed(
                        BRIGHTNESS_FADE_OUT_INFO, 1000);
            } else {
                // 调整音量
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (mVol < 0f)
                    mVol = 0f;
                // 显示
//                mBrightImg.setImageResource(R.mipmap.video_volumn_bg);
////                mBrightControlLayout.setVisibility(View.VISIBLE);
//                float volumeDelta = -y_changed_Delta
//                        / (mSurfaceYDisplayRange * 0.75f) * mAudioMax;
//                total_delta_voice += volumeDelta;
//                int vol = (int) Math.min(
//                        Math.max(mVol + (int) total_delta_voice, 0), mAudioMax);
//                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
//                // 变更进度条
//                ViewGroup.LayoutParams lp = mBrightPercentImg.getLayoutParams();
//                lp.width = findViewById(R.id.operation_full).getLayoutParams().width
//                        * vol / mAudioMax;
//                mBrightPercentImg.setLayoutParams(lp);
                mHandler.removeMessages(BRIGHTNESS_FADE_OUT_INFO);
//                viewControlHandler.sendEmptyMessageDelayed(
//                        BRIGHTNESS_FADE_OUT_INFO, 1000);
//                total_delta_voice = 0;
            }
            last_y_changed = y_changed;
        }
    }
}
