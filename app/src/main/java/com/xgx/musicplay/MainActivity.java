package com.xgx.musicplay;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.xiaozhi.firework_core.FireWorkView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements QRCodeView.Delegate, EasyPermissions.PermissionCallbacks {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    @BindView(R.id.container_layout)
    LinearLayout containerLayout;
    @BindView(R.id.zxingview)
    ZXingView mZXingView;
    @BindView(R.id.fileNameTv)
    TextView fileNameTv;
    @BindView(R.id.resultTv)
    TextView resultTv;
    @BindView(R.id.tv_now_time)
    TextView tvNowTime;
    @BindView(R.id.seek_bar)
    SeekBar seekBar;
    @BindView(R.id.tv_all_time)
    TextView tvAllTime;
    @BindView(R.id.changeCameraBtn)
    TextView changeCameraBtn;
    @BindView(R.id.historyBtn)
    TextView historyBtn;
    private boolean isStop = true;    //判断是否音乐播放状态
    private MusicService musicService;
    private boolean mBound = false;
    private String path = "";
    private int index = -1;     //当前所播放的音乐索引
    private List<Music> musicList = new ArrayList<>();
    private int musicNowTime;
    private Timer mTimer;
    private int type = 0;  //播放类型，循环播放0，单曲播放1，随机播放2；
    private Random random = new Random();

    private long thisTime = 0;
    private String lastFloder = "";
    private String resultStr = "";
    private String fileNameStr;
    private String allTimeStr;
    private FireWorkView fireWorkView;
    private static final boolean isOpenFire = true;
    private FireWorkView fireWorkView1;
    private FireWorkView fireWorkView2;
    private FireWorkView fireWorkView4;
    private FireWorkView fireWorkView5;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Music music = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        stopFireWork();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // land do nothing is ok
            setContentView(R.layout.activity_main);
            initView();
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port do nothing is ok
            setContentView(R.layout.activity_main);
            initView();
        }
        startFireWork();
        startCameraInit();
        final int musicAllTime = musicService.getDuration();
        allTimeStr = correctTime(musicAllTime / 60000) + ":" + correctTime(musicAllTime / 1000 % 60);
        tvAllTime.setText(allTimeStr);
        fileNameTv.setText(fileNameStr);
        resultTv.setText(resultStr);
        seekBar.setMax(musicAllTime);
    }

    private void startCameraInit() {
        mZXingView.stopCamera();
        mZXingView.startCamera(cameraId);
        mZXingView.showScanRect();
        mZXingView.startSpot();
    }


    private void initView() {
        ButterKnife.bind(this);
        mZXingView.setDelegate(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                stopFireWork();
                startFireWork();
                musicService.seekTo(seekBar.getProgress());

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        requestCodeQRCodePermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        startCameraInit();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和散光灯的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        } else {
            startCameraInit();
        }
    }


    @Override
    protected void onStop() {
        mZXingView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }


    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    private int lastSize = 0;

    private ArrayList<Integer> historyIndexs = new ArrayList<>();

    @Override
    public void onScanQRCodeSuccess(String result) {
        mZXingView.startSpot(); // 延迟0.1秒后开始识别
        if (TimeUtils.getNowDate().getTime() / 1000 - thisTime > 20) {
            thisTime = TimeUtils.getNowDate().getTime() / 1000;
            vibrate();

            //获取mediaMp3
            String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/" + result;
            List<File> files = FileUtils.listFilesInDirWithFilter(dir, mFilter);

            if (files != null && files.size() > 0) {

                musicList.clear();
                for (int i = 0; i < files.size(); i++) {
                    Music music = new Music();
                    music.setPath(files.get(i).getPath());
                    musicList.add(music);
                }
                musicStop();
                playPerpare(result);

            }
//          } else {
//               Toast.makeText(this, "app已超出使用时间", Toast.LENGTH_SHORT).show();
//           }
        }


    }

    private FileFilter mFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".mp3");
        }
    };

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        // 这里是通过修改提示文案来展示环境是否过暗的状态，接入方也可以根据 isDark 的值来实现其他交互效果
        String tipText = mZXingView.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                mZXingView.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                mZXingView.getScanBoxView().setTipText(tipText);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }


    //纠正显示的时间格式
    private String correctTime(int time) {
        if (time / 10 == 0) {
            return "0" + time;
        } else {
            return time + "";
        }
    }


    //当前活动结束后的响应
    @Override
    protected void onDestroy() {
        mZXingView.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();

        if (mBound) {
            musicService.stop();
            unbindService(mServiceConnection);
            mBound = false;
            isStop = true;
        }
    }


    //初始化服务内的媒体类以及有关媒体类get到的信息设置
    private void playPerpare(String result) {
        //去随机不重复的数
        if (musicList.size() != lastSize || historyIndexs.size() == musicList.size()) {
            //相等 则不需要重置histroyIndex
            historyIndexs = new ArrayList<>();
            lastSize = musicList.size();
        }
        for (int i = 0; i < 100; i++) {
            index = random.nextInt(musicList.size());
            boolean b = false;
            for (int j = 0; j < historyIndexs.size(); j++) {
                if (index == historyIndexs.get(j)) {
                    b = true;
                    break;
                }
            }
            if (!b) {
                historyIndexs.add(index);
                break;
            }
        }
        Log.i(TAG, index + "");
        music = musicList.get(index);
        resultStr = "当前播放目录：music/" + result;
        path = music.getPath();
        fileNameStr = "歌名：《" + FileUtils.getFileNameNoExtension(path) + "》";
        music.setName(FileUtils.getFileNameNoExtension(path));
        music.setScantime(TimeUtils.getNowString());
        music.setMusicDir("/music/" + result);

        if (FileUtils.getFileNameNoExtension(path).startsWith("!") || FileUtils.getFileNameNoExtension(path).startsWith("！")) {
            music.setPrize(true);
        } else {
            music.setPrize(false);
        }

        musicService.prepare(path);
        final int musicAllTime = musicService.getDuration();
        allTimeStr = correctTime(musicAllTime / 60000) + ":" + correctTime(musicAllTime / 1000 % 60);
        music.setAllTimeStr(allTimeStr);
        music.setId(TimeUtils.getNowMills());

        tvAllTime.setText(allTimeStr);
        fileNameTv.setText(fileNameStr);
        resultTv.setText(resultStr);
        seekBar.setMax(musicAllTime);
        mTimer = new Timer();
        TimerTask mTimerTask = new TimerTask() {

            @Override
            public void run() {
                musicNowTime = musicService.getCurrentPosition();
                seekBar.setProgress(musicNowTime);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvNowTime.setText(correctTime(musicNowTime / 60000) + ":" + correctTime(musicNowTime / 1000 % 60));

                    }
                });
            }
        };
        mTimer.schedule(mTimerTask, 0, 10);
        musicStart();
        try {
            stopFireWork();
            startFireWork();
        } catch (
                Exception e)

        {

        }


    }

    //播放音乐
    private void musicStart() {
        // imgStart.setImageDrawable(getResources().getDrawable(R.drawable.player_stop));
        if (music != null) {
            MyApplication.getDaoInstant().getMusicDao().insertOrReplace(music);
        }
        musicService.start();
        // imgCover.startRun();
        isStop = false;
    }

    private void musicStop() {
        //停止直接 保存或更新当前的music对象
        if (music != null) {
            music.setPlaytime(correctTime(musicService.getCurrentPosition() / 60000) + ":" + correctTime(musicService.getCurrentPosition() / 1000 % 60));
            MyApplication.getDaoInstant().getMusicDao().insertOrReplace(music);
        }
        musicService.stop();
        isStop = true;
    }

    //获取当前mp3文件的信息
    private Cursor getArtwork() {
        try {
            String nowpath = null;
            int i = 0;
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor.moveToFirst()) {
                do {
                    nowpath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if (nowpath.equals(path)) {
                        break;
                    }
                    i++;
                    if (i == cursor.getCount()) {
                        return null;
                    }
                } while (cursor.moveToNext());
            }
            return cursor;
        } catch (Exception e) {
            return null;
        }
    }

    private void stopFireWork() {
        if (isOpenFire) {
            if (fireWorkView != null) {
                fireWorkView.stopAnim();
            }
            if (fireWorkView1 != null) {
                fireWorkView1.stopAnim();
            }
            if (fireWorkView2 != null) {
                fireWorkView2.stopAnim();
            }
            if (fireWorkView4 != null) {
                fireWorkView4.stopAnim();
            }
            if (fireWorkView5 != null) {
                fireWorkView5.stopAnim();
            }
        }
    }

    private void startFireWork() {
        if (isOpenFire) {
            if (FileUtils.getFileNameNoExtension(path).startsWith("!") || FileUtils.getFileNameNoExtension(path).startsWith("！")) {
                containerLayout.removeAllViews();
                fireWorkView = new FireWorkView(MainActivity.this, R.drawable.yanhua);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                containerLayout.addView(fireWorkView, layoutParams);
                fireWorkView.initParticleSystem();
                fireWorkView.playAnim();
                fireWorkView1 = new FireWorkView(MainActivity.this, R.drawable.yanhua1);
                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(-1, -1);
                containerLayout.addView(fireWorkView1, layoutParams1);
                fireWorkView1.initParticleSystem();
                fireWorkView1.playAnim();

                fireWorkView2 = new FireWorkView(MainActivity.this, R.drawable.yanhua2);
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -1);
                containerLayout.addView(fireWorkView2, layoutParams2);
                fireWorkView2.initParticleSystem();
                fireWorkView2.playAnim();

                fireWorkView4 = new FireWorkView(MainActivity.this, R.drawable.yanhua4);
                LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -1);
                containerLayout.addView(fireWorkView4, layoutParams4);
                fireWorkView4.initParticleSystem();
                fireWorkView4.playAnim();

                fireWorkView5 = new FireWorkView(MainActivity.this, R.drawable.yanhua4);
                LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-1, -1);
                containerLayout.addView(fireWorkView5, layoutParams5);
                fireWorkView5.initParticleSystem();
                fireWorkView5.playAnim();
            }
        }
    }

    //问服务连接是否开了线程   与服务建立连接
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder musicBinder = (MusicService.MusicBinder) service;
            musicService = musicBinder.getService();
            musicService.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (!mp.isPlaying()) {
                        if (music != null) {
                            music.setPlaytime(correctTime(musicService.getCurrentPosition() / 60000) + ":" + correctTime(musicService.getCurrentPosition() / 1000 % 60));
                            MyApplication.getDaoInstant().getMusicDao().insertOrReplace(music);
                        }
                        stopFireWork();
                    }

                }
            });
            mBound = true;

            //这个响应事件不能放initView()中的原因是musicService要在服务开启后才能调用
            //不放playPerpare()中是不用重复执行，所以才放这边的。

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @OnClick({R.id.changeCameraBtn, R.id.historyBtn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.changeCameraBtn:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                startCameraInit();
                break;
            case R.id.historyBtn:
                startActivity(new Intent(MainActivity.this, HistoryListActivity.class));
                break;
        }
    }

}
