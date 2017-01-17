package com.xingzhiqiao.customijkplayer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;

import com.xingzhiqiao.xzqplayer.CnrVideoView;
import com.xingzhiqiao.xzqplayer.IRenderView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xingzhiqiao on 2016/12/28.
 */

public class PlayActivity extends AppCompatActivity {

    private String TAG = PlayActivity.class.getSimpleName();

    private SensorManager sensorManager;
    private Sensor sensor;


    @BindView(R.id.cnrvideo)
    CnrVideoView cnrVideoView;

    private MyOrientoinListener myOrientoinListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        myOrientoinListener = new MyOrientoinListener(this);
        boolean autoRotateOn = (android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
        if (autoRotateOn) {
            myOrientoinListener.enable();
        }
        cnrVideoView = (CnrVideoView) findViewById(R.id.cnrvideo);

        cnrVideoView.setVideoPath(getPlayUrl());
        cnrVideoView.setThumbImg("http://img4.imgtn.bdimg.com/it/u=2390338351,126748773&fm=21&gp=0.jpg");
        cnrVideoView.showBottomProgress(true);
        cnrVideoView.start();
        cnrVideoView.toggleAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
    }

    public String getPlayUrl() {
        List<String> playUrls = new ArrayList<>();
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/bjws/z.m3u8");
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/tjws/z.m3u8");
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/szws/z.m3u8");
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/sdws/z.m3u8");
        playUrls.add("http://baobab.wdjcdn.com/14564977406580.mp4");
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/dfws/z.m3u8");
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/zjws/z.m3u8");
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/lyws/z.m3u8");
        playUrls.add("http://baobab.wdjcdn.com/14564977406580.mp4");
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/henws/z.m3u8");
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/ahws/z.m3u8");
        playUrls.add("http://60.220.196.154/livehls1-cnc.wasu.cn/ahws/ .m3u8");
        playUrls.add("http://baobab.wdjcdn.com/14564977406580.mp4");
        playUrls.add(PlayUrl.MV_EVERYTHING);
        playUrls.add(PlayUrl.MV_EVERYTHING);
        playUrls.add(PlayUrl.MV_EVERYTHING);
        playUrls.add(PlayUrl.MV_EVERYTHING);
        playUrls.add(PlayUrl.MV_EVERYTHING);
        Random random = new Random();
        int radomIndex = random.nextInt(playUrls.size() - 1);
        return playUrls.get(radomIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestory");
        cnrVideoView.release(true);
        myOrientoinListener.disable();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    class MyOrientoinListener extends OrientationEventListener {

        public MyOrientoinListener(Context context) {
            super(context);
        }

        public MyOrientoinListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
//            Log.d(TAG, "orention" + orientation);
            int screenOrientation = getResources().getConfiguration().orientation;
            //设置竖屏
            if (((orientation >= 0) && (orientation <= 30)) || (orientation >= 330)) {
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                    cnrVideoView.switchPortrait();
                }
            } else if (((orientation >= 230) && (orientation <= 310))) { //设置横屏
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    cnrVideoView.switchLandscape();
                }
            } else if (orientation > 30 && orientation < 95) {// 设置反向横屏
                if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    cnrVideoView.switchReverseLandscape();
                }
            }
        }
    }


}
