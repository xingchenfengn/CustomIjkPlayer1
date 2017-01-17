package com.xingzhiqiao.xzqplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.xingzhiqiao.xzqplayer.util.AndroidTools;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * 视频播放器基类，主要实现播放功能
 * Created by xingzhiqiao on 2016/11/30.
 */

public class BaseVideoView extends FrameLayout implements MediaController.MediaPlayerControl, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener {

    private String TAG = "CnrVideoView";
    // settable by the client
    private Uri mUri;
    private Map<String, String> mHeaders;

    // all possible internal states
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_START_BUFFER = 6;
    private static final int STATE_END_BUFFER = 7;

    public static final int PLAYER_BUFFER_START = 2001;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;

    // All the stuff we need for playing and showing a video
    private IRenderView.ISurfaceHolder mSurfaceHolder = null;
    private IjkMediaPlayer mMediaPlayer = null;
    // private int         mAudioSession;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private int mVideoRotationDegree;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private int mCurrentBufferPercentage;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private int mSeekWhenPrepared;  // recording the seek position while preparing
    private boolean mCanPause = true;
    private boolean mCanSeekBack = true;
    private boolean mCanSeekForward = true;

    public Context mAppContext;
    //    private Settings mSettings;
    private IRenderView mRenderView;
    private int mVideoSarNum;
    private int mVideoSarDen;


    private int videoviewWidth;
    private int videoviewHeigh;

    public Activity mActivity;

    //    private InfoHudViewHolder 3;
    public void toggleAspectRatio(int aspecRation) {
        mRenderView.setAspectRatio(aspecRation);
    }


    public BaseVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    public BaseVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public BaseVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    public void initVideoView(Context context) {

        mAppContext = context;
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
        setRender();
        mVideoWidth = 0;
        mVideoHeight = 0;
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }


    private Handler mVideoHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };


    private static final int[] s_allAspectRatio = {
            IRenderView.AR_ASPECT_FIT_PARENT,
            IRenderView.AR_ASPECT_FILL_PARENT,
            IRenderView.AR_ASPECT_WRAP_CONTENT,
            IRenderView.AR_MATCH_PARENT,
            IRenderView.AR_16_9_FIT_PARENT,
            IRenderView.AR_4_3_FIT_PARENT};
    private int mCurrentAspectRatioIndex = 0;
    private int mCurrentAspectRatio = s_allAspectRatio[3];


    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
    }

    public void setRender() {
        TextureRenderView renderView = new TextureRenderView(getContext());
        if (mMediaPlayer != null) {
            renderView.getSurfaceHolder().bindToMediaPlayer(mMediaPlayer);
            renderView.setVideoSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
            renderView.setVideoSampleAspectRatio(mMediaPlayer.getVideoSarNum(), mMediaPlayer.getVideoSarDen());
            renderView.setAspectRatio(mCurrentAspectRatio);
        }
        setRenderView(renderView);
    }

    public void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            if (mMediaPlayer != null)
                mMediaPlayer.setDisplay(null);

            View renderUIView = mRenderView.getView();
            mRenderView.removeRenderCallback(mSHCallback);
            mRenderView = null;
            removeView(renderUIView);
        }

        if (renderView == null)
            return;

        mRenderView = renderView;
        renderView.setAspectRatio(mCurrentAspectRatio);
        if (mVideoWidth > 0 && mVideoHeight > 0)
            renderView.setVideoSize(mVideoWidth, mVideoHeight);
        if (mVideoSarNum > 0 && mVideoSarDen > 0)
            renderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);

        View renderUIView = mRenderView.getView();
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        renderUIView.setLayoutParams(lp);
        addView(renderUIView);
        mRenderView.addRenderCallback(mSHCallback);
        mRenderView.setVideoRotation(mVideoRotationDegree);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;
        int displayWidht = AndroidTools.getScreenWidth(mAppContext);
        //设定宽度
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = getPaddingLeft() + getPaddingRight() + widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = getPaddingLeft() + getPaddingRight();
        }

        //设定高度
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = getPaddingBottom() + getPaddingTop() + heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {//如果用户没有设置高度，设置成宽度的9/16
            height = getPaddingBottom() + getPaddingTop() + displayWidht * 9 / 16;
        }

        videoviewWidth = width;
        videoviewHeigh = height;
        setMeasuredDimension(width, height);
    }

    IRenderView.IRenderCallback mSHCallback = new IRenderView.IRenderCallback() {
        @Override
        public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int w, int h) {
            if (holder.getRenderView() != mRenderView) {
                Log.d(TAG, "onSurfaceChanged: unmatched render callback\n");
                return;
            }

            mSurfaceWidth = w;
            mSurfaceHeight = h;
            Log.d("TAG", "onSurfaceChanged");
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = !mRenderView.shouldWaitForResize() || (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        @Override
        public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {
            if (holder.getRenderView() != mRenderView) {
                Log.d(TAG, "onSurfaceCreated: unmatched render callback\n");
                return;
            }
            Log.d("TAG", "onSurfaceCreated");
            mSurfaceHolder = holder;
            if (mMediaPlayer != null) {
                bindSurfaceHolder(mMediaPlayer, holder);
                if (mCurrentState == STATE_PAUSED) {
                    mMediaPlayer.start();
                }
            } else {
                openVideo();
            }
        }

        @Override
        public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {
            if (holder.getRenderView() != mRenderView) {
                Log.d(TAG, "onSurfaceDestroyed: unmatched render callback\n");
                return;
            }
            Log.d("TAG", "onSurfaceDestroyed");
            mSurfaceHolder = null;
            releaseWithStop();
        }
    };


    public void releaseWithStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(null);
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
        }
    }

    /*
     * release the media player in any state
     */
    public void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            // REMOVED: mPendingSubtitleTracks.clear();
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            // not ready for playback just yet, will try again later
            return;
        }
        // we shouldn't haveclear the target state, because somebody might
        // called start() previously
        release(false);

        AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        try {
            mMediaPlayer = createPlayer();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
//            mMediaPlayer.setLooping(true);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            //设置屏幕常亮
            if (mActivity != null) {
                mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            mCurrentBufferPercentage = 0;
            String scheme = mUri.getScheme();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
                IMediaDataSource dataSource = new FileMediaDataSource(new File(mUri.toString()));
                mMediaPlayer.setDataSource(dataSource);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mMediaPlayer.setDataSource(mAppContext, mUri, mHeaders);
            } else {
                mMediaPlayer.setDataSource(mUri.toString());
            }
            bindSurfaceHolder(mMediaPlayer, mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();

            mCurrentState = STATE_PREPARING;
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        } finally {
            // REMOVED: mPendingSubtitleTracks.clear();
        }
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

//    private void attachMediaController() {
//        if (mMediaPlayer != null && mMediaController != null) {
//            mMediaController.setMediaPlayer(this);
//            View anchorView = this.getParent() instanceof View ?
//                    (View) this.getParent() : this;
//            mMediaController.setAnchorView(anchorView);
//            mMediaController.setEnabled(isInPlaybackState());
//        }
//    }

    public IjkMediaPlayer createPlayer() {

        IjkMediaPlayer ijkMediaPlayer = null;
        if (mUri != null) {
            ijkMediaPlayer = new IjkMediaPlayer();
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 300000);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        }
        return ijkMediaPlayer;
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     */
    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    // REMOVED: mSHCallback
    private void bindSurfaceHolder(IMediaPlayer mp, IRenderView.ISurfaceHolder holder) {
        if (mp == null)
            return;

        if (holder == null) {
            mp.setDisplay(null);
            return;
        }

        holder.bindToMediaPlayer(mp);
    }


    IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new IMediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    mVideoSarNum = mp.getVideoSarNum();
                    mVideoSarDen = mp.getVideoSarDen();
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        if (mRenderView != null) {
                            mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                            mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                        }
                        // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                        requestLayout();
                    }
                }
            };


    private IMediaPlayer.OnErrorListener mErrorListener =
            new IMediaPlayer.OnErrorListener() {
                public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
                    Log.d(TAG, "Error: " + framework_err + "," + impl_err);
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;

                    /* If an error handler has been supplied, use it and finish. */
                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    /* Otherwise, pop up an error dialog so the user knows that
                     * something bad has happened. Only try and pop up the dialog
                     * if we're attached to a window. When we're going away and no
                     * longer have a window, don't bother showing the user an error.
                     */
                    if (getWindowToken() != null) {
                        Resources r = mAppContext.getResources();
                        int messageId;

//                        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
//                            messageId = R.string.VideoView_error_text_invalid_progressive_playback;
//                        } else {
//                            messageId = R.string.VideoView_error_text_unknown;
//                        }

//                        new AlertDialog.Builder(getContext())
//                                .setMessage(messageId)
//                                .setPositiveButton(R.string.VideoView_error_button,
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int whichButton) {
//                                            /* If we get here, there is no onError listener, so
//                                             * at least inform them that the video is over.
//                                             */
//                                                if (mOnCompletionListener != null) {
//                                                    mOnCompletionListener.onCompletion(mMediaPlayer);
//                                                }
//                                            }
//                                        })
//                                .setCancelable(false)
//                                .show();
                    }
                    return true;
                }
            };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new IMediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                    Log.d("TAG", "mCurrentBufferPercentage" + percent);
                    if (mOnBufferingUpdateListener != null) {
                        mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
                    }
                }
            };


    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getDuration();
        }

        return -1;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return (int) mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    /**
     * 设置横屏
     */
    public void switchLandscape() {
        if (mActivity != null) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mActivity.getWindow().setAttributes(attrs);
            this.getLayoutParams().width = AndroidTools.getScreenWidth(mAppContext);
            this.getLayoutParams().height = AndroidTools.getScreenHeight(mAppContext);
        }
    }

    /**
     * 反向横屏
     */
    public void switchReverseLandscape() {
        if (mActivity != null) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mActivity.getWindow().setAttributes(attrs);
            this.getLayoutParams().width = AndroidTools.getScreenWidth(mAppContext);
            this.getLayoutParams().height = AndroidTools.getScreenHeight(mAppContext);
        }
    }


    /**
     * 设置半屏幕
     */
    public void switchPortrait() {
        if (mActivity != null) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mActivity.getWindow().setAttributes(attrs);
            this.getLayoutParams().width = AndroidTools.getScreenWidth(mAppContext);
            this.getLayoutParams().height = AndroidTools.getScreenWidth(mAppContext) * 9 / 16;
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }


    @Override
    public boolean canPause() {
        return mCanPause;
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBack;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
        if (mOnBufferingUpdateListener != null) {
            mOnBufferingUpdateListener.onBufferingUpdate(iMediaPlayer, i);
        }
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int arg1, int arg2) {
        if (mOnInfoListener != null) {
            mOnInfoListener.onInfo(iMediaPlayer, arg1, arg2);
            Log.d("TAG", "OnInfo:+arg1:" + arg1 + ":arg2" + arg2);
        }
        switch (arg1) {
            case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                mVideoHandler.sendEmptyMessage(IMediaPlayer.MEDIA_INFO_BUFFERING_START);
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                mVideoHandler.sendEmptyMessage(IMediaPlayer.MEDIA_INFO_BUFFERING_END);
                break;
            case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + arg2);
                break;
            case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                break;
            case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                break;
            case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                break;
            case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                break;
            case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                mVideoRotationDegree = arg2;
                Log.d(TAG, "MEDIA_INFO_VIDEO_ROTATION_CHANGED: " + arg2);
                if (mRenderView != null)
                    mRenderView.setVideoRotation(arg2);
                break;
            case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        mCurrentState = STATE_PREPARED;

        // Get the capabilities of the player for this stream
        // REMOVED: Metadata

        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(mMediaPlayer);
        }
        mVideoWidth = iMediaPlayer.getVideoWidth();
        mVideoHeight = iMediaPlayer.getVideoHeight();

        int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
        if (seekToPosition != 0) {
            seekTo(seekToPosition);
        }
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
            // REMOVED: getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            if (mRenderView != null) {
                mRenderView.setVideoSize(mVideoWidth, mVideoHeight);
                mRenderView.setVideoSampleAspectRatio(mVideoSarNum, mVideoSarDen);
                if (!mRenderView.shouldWaitForResize() || mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    // We didn't actually change the size (it was already at the size
                    // we need), so we won't get a "surface changed" callback, so
                    // start the video here instead of in the callback.
                    if (mTargetState == STATE_PLAYING) {
                        start();
                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {
                    }
                }
            }
        } else {
            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            if (mTargetState == STATE_PLAYING) {
                start();
            }
        }
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        mCurrentState = STATE_PLAYBACK_COMPLETED;
        mTargetState = STATE_PLAYBACK_COMPLETED;
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mMediaPlayer);
        }
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int framework_err, int impl_err) {
        Log.d(TAG, "Error: " + framework_err + "," + impl_err);
        mCurrentState = STATE_ERROR;
        mTargetState = STATE_ERROR;

        /* If an error handler has been supplied, use it and finish. */
        if (mOnErrorListener != null) {
            if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                return true;
            }
        }

        /* Otherwise, pop up an error dialog so the user knows that
         * something bad has happened. Only try and pop up the dialog
         * if we're attached to a window. When we're going away and no
         * longer have a window, don't bother showing the user an error.
         */
        if (getWindowToken() != null) {
            Resources r = mAppContext.getResources();
            int messageId;

//                        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
//                            messageId = R.string.VideoView_error_text_invalid_progressive_playback;
//                        } else {
//                            messageId = R.string.VideoView_error_text_unknown;
//                        }

//                        new AlertDialog.Builder(getContext())
//                                .setMessage(messageId)
//                                .setPositiveButton(R.string.VideoView_error_button,
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int whichButton) {
//                                            /* If we get here, there is no onError listener, so
//                                             * at least inform them that the video is over.
//                                             */
//                                                if (mOnCompletionListener != null) {
//                                                    mOnCompletionListener.onCompletion(mMediaPlayer);
//                                                }
//                                            }
//                                        })
//                                .setCancelable(false)
//                                .show();
        }
        return true;
    }
}
