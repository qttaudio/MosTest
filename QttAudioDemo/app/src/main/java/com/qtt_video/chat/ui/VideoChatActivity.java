package com.qtt_video.chat.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.hjq.toast.ToastUtils;
import com.qtt_video.chat.R;
import com.qtt_video.chat.dialog.ResolutionDialog;
import com.qtt_video.chat.utils.Constants;
import com.qtt_video.chat.utils.TimeUtil;
import com.qttaudio.sdk.channel.AudioMode;
import com.qttaudio.sdk.channel.AudioQuality;
import com.qttaudio.sdk.channel.ChannelEngine;
import com.qttaudio.sdk.channel.ChannelObserver;
import com.qttaudio.sdk.channel.ChannelRole;
import com.qttaudio.sdk.channel.DataObserver;
import com.qttaudio.sdk.channel.RtcStat;
import com.qttaudio.sdk.channel.VideoEncoderConfiguration;
import com.qttaudio.sdk.channel.VolumeInfo;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoChatActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = VideoChatActivity.class.getName();

    private AppCompatTextView tvOnlineTime;
    private RelativeLayout rlVideo;
    private FrameLayout flVideo;
    private AppCompatTextView tvUid;
    private AppCompatImageView ivCloseMic;
    private AppCompatTextView tvOtherUid;

    private Runnable timeTask;


    private long time;
    private long uid;
    private long remoteUid;
    private boolean remoteMuteVideo = true;
    private boolean remoteMuteAudio;

    private boolean isCloseMic;
    private boolean isDisableVideo;
    private boolean isCloseSound;

    private SurfaceView localView;
    private SurfaceView remoteView;
    private String roomName;

    private boolean isOtherJoin = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        tvOnlineTime = findViewById(R.id.tv_onlineTime);
        tvOtherUid = findViewById(R.id.tv_otherUid);
        ivCloseMic = findViewById(R.id.iv_closeMic);

        rlVideo = findViewById(R.id.rl_video);
        rlVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchView(localView);
                switchView(remoteView);
            }
        });

        tvUid = findViewById(R.id.tv_uid);
        flVideo = findViewById(R.id.fl_video);

        ivSwitchCamera.setOnClickListener(this);
        ivExit.setOnClickListener(this);
        ivMic.setOnClickListener(this);
        ivVideo.setOnClickListener(this);
        ivMusic.setOnClickListener(this);
        ivBeauty.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        ivSound.setOnClickListener(this);

        setUp();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_1to1;
    }

    private void setUp() {
        timeTask = new Runnable() {
            @Override
            public void run() {
                time++;
                tvOnlineTime.setText(TimeUtil.secToTime(time));
                handler.postDelayed(timeTask, 1000);
            }
        };

        Intent intent = getIntent();
        roomName = intent.getStringExtra(Constants.ROOM_NAME);
        tvRoomName.setText(roomName);
        initEngineAndJoin(VideoEncoderConfiguration.VD_1280x720);

        initDialogs();
        resolutionDialog.setChangePixelListener(new ResolutionDialog.ChangePixelListener() {
            @Override
            public void pixel(int type) {
                VideoEncoderConfiguration.VideoDimensions videoDimensions = null;
                if (Constants.PIXEL_TYPE_1 == type) {
                    videoDimensions = VideoEncoderConfiguration.VD_640x360;
                } else if (Constants.PIXEL_TYPE_2 == type) {
                    videoDimensions = VideoEncoderConfiguration.VD_640x480;
                } else if (Constants.PIXEL_TYPE_3 == type) {
                    videoDimensions = VideoEncoderConfiguration.VD_1280x720;
                }
                resetVideo(videoDimensions);
            }
        });
    }

    private void initEngineAndJoin(VideoEncoderConfiguration.VideoDimensions videoDimensions) {
        if (uid == 0) {
            uid = getRandomUid();
        }
        channelEngine = ChannelEngine.GetChannelInstance(this, Constants.APP_KEY, channelObserver);
        channelEngine.changeRole(ChannelRole.TALKER);
        channelEngine.enableVideo();
        channelEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                videoDimensions, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                0, VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        ));
        channelEngine.setAudioConfig(AudioQuality.AUDIO_QUALITY_MUSIC_STEREO, AudioMode.AUDIO_MODE_MIX);

        DataObserver recordObServer = new DataObserver() {
            private String pcmFileName="";
            OutputStream out;

            @Override
            public boolean onData(ByteBuffer byteBuffer, int i) {
                if(isOtherJoin){
                    saveToFile(byteBuffer,i);
                }
                return true;
            }

            private void saveToFile(ByteBuffer buffer , int len){
                try{
                    if(pcmFileName.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
                        String now = sdf.format(new Date());
                        sb.append("TIME:").append(now);
                        pcmFileName = getCrashFilePath(VideoChatActivity.this)+now+"_record.raw";
                        out = new FileOutputStream(pcmFileName);
                        Log.d("Demo","will store data to file: "+pcmFileName);
                    }
                    buffer.rewind();
                    byte[] data = new byte[len];
                    buffer.get(data,0,len);

                    InputStream is = new ByteArrayInputStream(data);
                    byte[] outData = new byte[1920];
                    int outLen = 0;
                    while ((outLen = is.read(outData)) != -1) {
                        out.write(outData, 0, outLen);
                        Log.d("Demo","wtire data. len: "+outLen);
                    }
                    is.close();

                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            private  String getCrashFilePath(Context context) {
                String path = null;
                try {
                    path = Environment.getExternalStorageDirectory().getCanonicalPath() + "/"
                            + context.getResources().getString(R.string.app_name) + "/Crash/";
                    File file = new File(path);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e("TAG", "getCrashFilePath: " + path);
                return path;
            }

            protected void finalize() {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        channelEngine.setRecordDataObserver(recordObServer,48000,1,0);

        DataObserver receiveObServer = new DataObserver() {
            private String pcmFileName="";
            OutputStream out;

            @Override
            public boolean onData(ByteBuffer byteBuffer, int i) {
                if(isOtherJoin){
                    saveToFile(byteBuffer,i);
                }
                return true;
            }

            private void saveToFile(ByteBuffer buffer , int len){
                try{
                    if(pcmFileName.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
                        String now = sdf.format(new Date());
                        sb.append("TIME:").append(now);
                        pcmFileName = getCrashFilePath(VideoChatActivity.this)+now+"_receive.raw";
                        out = new FileOutputStream(pcmFileName);
                        Log.d("Demo","will store data to file: "+pcmFileName);
                    }
                    buffer.rewind();
                    byte[] data = new byte[len];
                    buffer.get(data,0,len);

                    InputStream is = new ByteArrayInputStream(data);
                    byte[] outData = new byte[1920];
                    int outLen = 0;
                    while ((outLen = is.read(outData)) != -1) {
                        out.write(outData, 0, outLen);
                        Log.d("Demo","wtire data. len: "+outLen);
                    }
                    is.close();

                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            private  String getCrashFilePath(Context context) {
                String path = null;
                try {
                    path = Environment.getExternalStorageDirectory().getCanonicalPath() + "/"
                            + context.getResources().getString(R.string.app_name) + "/Crash/";
                    File file = new File(path);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e("TAG", "getCrashFilePath: " + path);
                return path;
            }

            protected void finalize() {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        channelEngine.setPlaybackDataObserver(receiveObServer,48000,1,0);
        channelEngine.setSpeakerOn(true);
        channelEngine.join("", roomName, uid);
    }

    private void resetVideo(VideoEncoderConfiguration.VideoDimensions videoDimensions) {
        channelEngine.disableVideo();
        channelEngine.enableVideo();
        channelEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                videoDimensions, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                0, VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        ));
    }


    private void loadLocalVideo() {
        localView = ChannelEngine.CreateRendererView(this);
        channelEngine.setupLocalVideo(localView, 1, 0);
        flVideo.addView(localView);
    }

    private void setRemoteVideo(long rUId) {
        remoteView = ChannelEngine.CreateRendererView(this);
        remoteView.setZOrderMediaOverlay(false);
        channelEngine.setupRemoteVideo(remoteView, rUId, 1, 0);
        if (localView.getParent() != null && localView.getParent() != rlVideo) {
            removeFromParent(localView);
            localView.setZOrderMediaOverlay(true);
            rlVideo.addView(localView, 0);
            tvUid.setText("ID：" + uid + "(我)");
            rlVideo.setVisibility(View.VISIBLE);
            ivCloseMic.setVisibility(isCloseMic ? View.VISIBLE : View.INVISIBLE);
        }
        tvOtherUid.setText("ID: " + rUId);

        flVideo.addView(remoteView);
        flVideo.setVisibility(View.VISIBLE);
    }


    private ViewGroup removeFromParent(SurfaceView surfaceView) {
        if (surfaceView != null) {
            ViewParent parent = surfaceView.getParent();
            if (parent != null) {
                ViewGroup viewGroup = (ViewGroup) parent;
                viewGroup.removeView(surfaceView);
                return viewGroup;
            }
        }
        return null;
    }

    private void switchView(SurfaceView surfaceView) {
        ViewGroup parent = removeFromParent(surfaceView);
        if (parent == null) {
            return;
        }
        if (parent == rlVideo) {
            surfaceView.setZOrderMediaOverlay(false);
            flVideo.addView(surfaceView);
            if (surfaceView == localView) {
                tvOtherUid.setText("ID：" + uid + "(我)");
            } else if (surfaceView == remoteView) {
                tvOtherUid.setText("ID：" + remoteUid);
            }
        } else if (parent == flVideo) {
            surfaceView.setZOrderMediaOverlay(true);
            rlVideo.addView(surfaceView, 0);
            if (surfaceView == localView) {
                tvUid.setText("ID：" + uid + "(我)");
                ivCloseMic.setVisibility(isCloseMic ? View.VISIBLE : View.INVISIBLE);
            } else if (surfaceView == remoteView) {
                tvUid.setText("ID：" + remoteUid);
                ivCloseMic.setVisibility(remoteMuteAudio ? View.VISIBLE : View.INVISIBLE);
                rlVideo.setVisibility(remoteMuteVideo ? View.INVISIBLE : View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_switchCamera:
                channelEngine.switchCamera();
                break;
            case R.id.iv_exit:
                if (channelEngine != null) {
                    channelEngine.leave();
                }
                finish();
                break;
            case R.id.iv_mic:
                isCloseMic = !isCloseMic;
                channelEngine.muteLocalAudio(isCloseMic);
                ivMic.setImageResource(isCloseMic ? R.mipmap.close_mic_gray : R.mipmap.open_mic);
                ToastUtils.show(isCloseMic ? "已关闭麦克风" : "已开启麦克风");
                if (localView.getParent() == rlVideo) {
                    ivCloseMic.setVisibility(isCloseMic ? View.VISIBLE : View.INVISIBLE);
                }
                break;
            case R.id.iv_sound:
                isCloseSound = !isCloseSound;
                channelEngine.muteAllRemoteAudio(isCloseSound);
                ivSound.setImageResource(isCloseSound ? R.mipmap.close_sound : R.mipmap.open_sound);
                ToastUtils.show(isCloseSound ? "已关闭声音" : "已开启声音");
                break;
            case R.id.iv_video:
                isDisableVideo = !isDisableVideo;
                channelEngine.muteLocalVideoStream(isDisableVideo);
                resolutionDialog.setAllowChangePixel(!isDisableVideo);
                ivSwitchCamera.setVisibility(isDisableVideo ? View.INVISIBLE : View.VISIBLE);
                ivVideo.setImageResource(isDisableVideo ? R.mipmap.my_close_video : R.mipmap.open_video);
                ToastUtils.show(isDisableVideo ? "已关闭摄像头" : "已开启摄像头");
                break;
            case R.id.iv_music:
                musicControlDialog.show();
                break;
            case R.id.iv_beauty:
                multiplyEffectDialog.show();
                break;
            case R.id.iv_more:
                resolutionDialog.show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        musicControlDialog.release();
        if (channelEngine != null) {
            channelEngine.leave();
            ChannelEngine.Destroy();
        }
    }

    private int count;
    private long joinTime = 0L;

    private ChannelObserver channelObserver = new ChannelObserver() {
        @Override
        public void onJoinSuccess(String s, long l, ChannelRole channelRole, boolean b) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    joinTime = System.currentTimeMillis();
                    uid = l;
                    tvOtherUid.setText("ID:" + l + "(我)");
                    tvMySelfUid.setText("ID:" + l);
                    loadLocalVideo();
                    handler.postDelayed(timeTask, 1000);

                }
            });
        }

        @Override
        public void onReJoinSuccess(String s, long l, ChannelRole channelRole, boolean b) {

        }

        @Override
        public void onOtherJoin(long l, ChannelRole channelRole, boolean b) {
            isOtherJoin = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() - joinTime <= 200) {
                        count++;
                        if (count == 2) {
                            finish();
                            ToastUtils.show("进入房间失败，当前房间人数已达上限");
                            return;
                        }
                    }
                    if (remoteUid != 0) {
                        return;
                    }
                    remoteUid = l;
                    remoteMuteVideo = false;
                    remoteMuteAudio = b;
                    tvOtherUid.setText("ID：" + remoteUid);
                    setRemoteVideo(l);
                }
            });
        }

        @Override
        public void onJoinFail(int i, String s) {

        }

        @Override
        public void onConnectionBreak() {

        }

        @Override
        public void onConnectionLost() {

        }

        @Override
        public void onError(int i, String s) {

        }

        @Override
        public void onWarning(int i, String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (i == 1002 || i == 1001) {
                        ToastUtils.show("当前网络不可用，请检查网络设置");
                    }
                }
            });
        }

        @Override
        public void onLeave() {

        }

        @Override
        public void onOtherLeave(long l, ChannelRole channelRole) {
            isOtherJoin = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (remoteUid == l) {
                        remoteUid = 0;
                        remoteMuteVideo = true;
                        if (remoteView != null) {
                            ViewGroup parent = (ViewGroup) remoteView.getParent();
                            if (parent != null && parent == flVideo) {
                                switchView(localView);
                                switchView(remoteView);
                            }
                        }
                        removeFromParent(remoteView);
                        ToastUtils.show("用户" + l + "已离开");
                    }
                }
            });
        }

        @Override
        public void onTalkingVolumeIndication(VolumeInfo[] volumeInfos, int i) {

        }

        @Override
        public void onMuteStatusChanged(long l, boolean b) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (l == remoteUid && remoteUid != 0) {
                        remoteMuteAudio = b;
                        if (remoteView.getParent() == rlVideo) {
                            ivCloseMic.setVisibility(remoteMuteAudio ? View.VISIBLE : View.INVISIBLE);
                        }
                    }
                }
            });
        }

        @Override
        public void onRoleStatusChanged(long l, ChannelRole channelRole) {

        }

        @Override
        public void onNetworkStats(long l, int txQuality, int rxQuality, RtcStat rtcStat) {

        }

        @Override
        public void onAudioRouteChanged(int i) {

        }

        @Override
        public void onSoundStateChanged(int i) {
            switch (i) {
                case 0://开始
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicControlDialog.resetMusicProgress(channelEngine.getSoundMixingDuration());
                            musicControlDialog.updateMusicProgress();
                        }
                    });
                    break;
                case 2://停止
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicControlDialog.stopUpdateMusicProgress();
                            musicControlDialog.updateMusicProgress(0);
                        }
                    });
                    break;
                case 1://暂停
                case 3://发生错误
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicControlDialog.stopUpdateMusicProgress();
                        }
                    });
                    break;
            }
        }

        @Override
        public void onEffectFinished(int i) {

        }

        @Override
        public void onUserEnableVideo(long l, boolean b) {

        }

        @Override
        public void onUserEnableLocalVideo(long l, boolean b) {

        }

        @Override
        public void onUserMuteVideo(long l, boolean b) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (l == remoteUid) {
                        remoteMuteVideo = b;
                        setRemoteVideo(l);
                    }
                }
            });
        }

    };
}
