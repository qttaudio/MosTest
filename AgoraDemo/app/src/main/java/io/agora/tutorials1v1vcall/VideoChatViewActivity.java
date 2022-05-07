package io.agora.tutorials1v1vcall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.agora.rtc.AudioFrame;
import io.agora.rtc.Constants;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.audio.AudioParams;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.uikit.logger.LoggerRecyclerView;

public class VideoChatViewActivity extends AppCompatActivity {
    private static final String TAG = VideoChatViewActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;

    // Permission WRITE_EXTERNAL_STORAGE is not mandatory
    // for Agora RTC SDK, just in case if you wanna save
    // logs to external sdcard.
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Thread   mRecordThread;
    private RtcEngine mRtcEngine;
    private boolean mCallEnd;
    private boolean mMuted;

    private FrameLayout mLocalContainer;
    private RelativeLayout mRemoteContainer;
    private VideoCanvas mLocalVideo;
    private VideoCanvas mRemoteVideo;

    private ImageView mCallBtn;
    private ImageView mMuteBtn;
    private ImageView mSwitchCameraBtn;

    public final  int  DEFAULT_SAMPLE_RATE=48000;
    public final  int  DEFAULT_CHANNEL_COUNT =2;

    private  boolean isWriteBackAudio = false;

    // Customized logger view
    private LoggerRecyclerView mLogView;



    private String[] musicPaths = {"/assets/夜的钢琴曲.raw"};

    private void copyFile() {
        for (int i = 0; i < musicPaths.length; i++) {
            String name = musicPaths[i];
            File file = new File(getFilesDir().getAbsolutePath(), name.substring(8));
            if (!file.exists()) {
                copyAssetsFile(this, name.substring(8));
            }
        }
    }

    public static String copyAssetsFile(Context context, String fileName) {
        String result = context.getFilesDir() + "/" + fileName;
        try {
            File file = new File(result);
            if (file.exists()) {
                return result;
            }
            InputStream is = context.getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(result);
            byte[] buffer = new byte[4096];
            int len = -1;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            fos.close();
            is.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Event handler registered into RTC engine for RTC callbacks.
     * Note that UI operations needs to be in UI thread because RTC
     * engine deals with the events in a separate thread.
     */
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        /**
         * Occurs when the local user joins a specified channel.
         * The channel name assignment is based on channelName specified in the joinChannel method.
         * If the uid is not specified when joinChannel is called, the server automatically assigns a uid.
         *
         * @param channel Channel name.
         * @param uid User ID.
         * @param elapsed Time elapsed (ms) from the user calling joinChannel until this callback is triggered.
         */
        @Override
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogView.logI("Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        public void onUserJoined(final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogView.logI("First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                 //   String fileName = getFilesDir().getAbsolutePath() + "/夜的钢琴曲.raw";
                    //TODO
                   //RtcEngine.startAudioMixing(fileName,false,false,1,0);
                }
            });
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * There are two reasons for users to become offline:
         *
         *     Leave the channel: When the user/host leaves the channel, the user/host sends a
         *     goodbye message. When this message is received, the SDK determines that the
         *     user/host leaves the channel.
         *
         *     Drop offline: When no data packet of the user or host is received for a certain
         *     period of time (20 seconds for the communication profile, and more for the live
         *     broadcast profile), the SDK assumes that the user/host drops offline. A poor
         *     network connection may lead to false detections, so we recommend using the
         *     Agora RTM SDK for reliable offline detection.
         *
         * @param uid ID of the user or host who leaves the channel or goes offline.
         * @param reason Reason why the user goes offline:
         *
         *     USER_OFFLINE_QUIT(0): The user left the current channel.
         *     USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data packet was received within a certain period of time. If a user quits the call and the message is not passed to the SDK (due to an unreliable channel), the SDK assumes the user dropped offline.
         *     USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from the host to the audience.
         */
        @Override
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLogView.logI("User offline, uid: " + (uid & 0xFFFFFFFFL));
                    onRemoteUserLeft(uid);
                }
            });
        }
    };

    private void setupRemoteVideo(int uid) {
        ViewGroup parent = mRemoteContainer;
        if (parent.indexOfChild(mLocalVideo.view) > -1) {
            parent = mLocalContainer;
        }

        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        if (mRemoteVideo != null) {
            return;
        }

        /*
          Creates the video renderer view.
          CreateRendererView returns the SurfaceView type. The operation and layout of the view
          are managed by the app, and the Agora SDK renders the view provided by the app.
          The video display view must be created using this method instead of directly
          calling SurfaceView.
         */
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(parent == mLocalContainer);
        parent.addView(view);
        mRemoteVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        // Initializes the video view of a remote user.
        mRtcEngine.setupRemoteVideo(mRemoteVideo);

        //TODO
       // startRecordThread();
        isWriteBackAudio = true;
    }

    private void onRemoteUserLeft(int uid) {
        if (mRemoteVideo != null && mRemoteVideo.uid == uid) {
            removeFromParent(mRemoteVideo);
            // Destroys remote view
            mRemoteVideo = null;
            isWriteBackAudio = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat_view);
        initUI();

        // Ask for permissions at runtime.
        // This is just an example set of permissions. Other permissions
        // may be needed, and please refer to our online documents.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2],PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[3],PERMISSION_REQ_ID)){
            copyFile();
            initEngineAndJoinChannel();
        }
    }

    private void initUI() {
        mLocalContainer = findViewById(R.id.local_video_view_container);
        mRemoteContainer = findViewById(R.id.remote_video_view_container);

        mCallBtn = findViewById(R.id.btn_call);
        mMuteBtn = findViewById(R.id.btn_mute);
        mSwitchCameraBtn = findViewById(R.id.btn_switch_camera);

        mLogView = findViewById(R.id.log_recycler_view);

        // Sample logs are optional.
        showSampleLogs();
    }

    private void showSampleLogs() {
        mLogView.logI("Welcome to Agora 1v1 video call");
        mLogView.logW("You will see custom logs here");
        mLogView.logE("You can also use this to show errors");
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length < 2 || grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                        "/" + Manifest.permission.CAMERA);
                finish();
                return;
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initEngineAndJoinChannel();
        }
    }

    private void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initEngineAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        initializeEngine();
        setupVideoConfig();
        setupLocalVideo();
        joinChannel();
    }

    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            Log.d("Demo","Agora SDK Version:"+RtcEngine.getSdkVersion()) ;
            IAudioFrameObserver audioFrameObserver = new IAudioFrameObserver() {
                String fileName = getFilesDir().getAbsolutePath() + "/夜的钢琴曲.raw";
                private String pcmReceiveFileName="";
                private String pcmRecordFileName="";
                OutputStream receiveOut;
                OutputStream recordOut;
                byte[] pcmBuffer = new byte[1920];  //10ms 数据
                FileInputStream input = null;

                // 实现 getObservedAudioFramePosition 回调，在该回调的返回值中设置音频观测位置为 POSITION_RECORD，对应 onRecordFrame 回调。
                @Override
                public int getObservedAudioFramePosition() {
                    return IAudioFrameObserver.POSITION_PLAYBACK | IAudioFrameObserver.POSITION_RECORD;
                }

                // 实现 getRecordAudioParams 回调，在该回调的返回值中设置 onRecordFrame 回调音频的格式。
                @Override
                public AudioParams getRecordAudioParams() {
                    return new AudioParams(48000, 2, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_WRITE, 960);
                }

                @Override
                public AudioParams getPlaybackAudioParams() {
                    return new AudioParams(48000, 2, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_ONLY, 960);
                }

                @Override
                public AudioParams getMixedAudioParams() {
                    return new AudioParams(48000, 2, Constants.RAW_AUDIO_FRAME_OP_MODE_READ_ONLY, 960);
                }

                // 实现 onRecordFrame 回调，从回调中获取音频数据，与本地音频文件混音后发送给 SDK 播放。
                @Override
                public boolean onRecordFrame(AudioFrame audioFrame) {

                    if(isWriteBackAudio){

                        //使用麦克风采样
                        Log.d("Demo","onRecordFrame numOfSamples:"+audioFrame.numOfSamples+" samplesPerSec:"+audioFrame.samplesPerSec);
                        saveTorRecordFile(audioFrame.samples,audioFrame.numOfSamples*audioFrame.bytesPerSample*audioFrame.channels);

                        //使用音频文件作为输入
                      /*  try{
                            Log.d("Demo",audioFrame.toString());
                            if(input == null) {
                                input = new FileInputStream(fileName);
                            }

                            int len = input.read(pcmBuffer);
                            if(len==-1)
                                return true;

                            audioFrame.samples.clear();
                            audioFrame.samples.rewind();

                            int bufferLen = audioFrame.numOfSamples * audioFrame.channels * audioFrame.bytesPerSample;
                            if(bufferLen != 1920){
                                Log.e("Demo","Len is error");
                            }
                            audioFrame.samples.put(pcmBuffer,0,1920);

                            saveTorRecordFile(audioFrame.samples,1920);
                          //  mRtcEngine.pushExternalAudioFrame(pcmBuffer,System.currentTimeMillis(),48000,2,2,2);

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }*/
                    }

                    return true;
                }

                @Override
                public boolean onPlaybackFrame(AudioFrame audioFrame) {
                    if(isWriteBackAudio){
                        Log.d("Demo","OnPlayBack numOfSamples:"+audioFrame.numOfSamples+" samplesPerSec:"+audioFrame.samplesPerSec);
                        saveTorReceiveFile(audioFrame.samples,audioFrame.numOfSamples*audioFrame.bytesPerSample*audioFrame.channels);
                    }
                    return true;
                }

                @Override
                public boolean onPlaybackFrameBeforeMixing(AudioFrame audioFrame, int uid) {
                    return true;
                }

                @Override
                public boolean onMixedFrame(AudioFrame audioFrame) {
                    return false;
                }

                @Override
                public boolean isMultipleChannelFrameWanted() {
                    return false;
                }

                @Override
                public boolean onPlaybackFrameBeforeMixingEx(AudioFrame audioFrame, int uid, String channelId) {
                    return true;
                }

                private void saveTorReceiveFile(ByteBuffer buffer , int len){
                    try{
                        if(pcmReceiveFileName.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
                            String now = sdf.format(new Date());
                            sb.append("TIME:").append(now);
                            pcmReceiveFileName = getCrashFilePath(VideoChatViewActivity.this)+now+"_receive.raw";
                            receiveOut = new FileOutputStream(pcmReceiveFileName);
                            Log.d("Demo","will store data to file: "+pcmReceiveFileName);
                        }
                        buffer.rewind();
                        byte[] data = new byte[len];
                        buffer.get(data,0,len);

                        InputStream is = new ByteArrayInputStream(data);
                        byte[] outData = new byte[len];
                        int outLen = 0;
                        while ((outLen = is.read(outData)) != -1) {
                            receiveOut.write(outData, 0, outLen);
                            Log.d("Demo","wtire data. len: "+outLen);
                        }
                        is.close();

                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }

                private void saveTorRecordFile(ByteBuffer buffer , int len){
                    try{
                        if(pcmRecordFileName.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
                            String now = sdf.format(new Date());
                            sb.append("TIME:").append(now);
                            pcmRecordFileName = getCrashFilePath(VideoChatViewActivity.this)+now+"_record.raw";
                            recordOut = new FileOutputStream(pcmRecordFileName);
                            Log.d("Demo","will store data to file: "+pcmRecordFileName);
                        }
                        buffer.rewind();
                        byte[] data = new byte[len];
                        buffer.get(data,0,len);

                        InputStream is = new ByteArrayInputStream(data);
                        byte[] outData = new byte[len];
                        int outLen = 0;
                        while ((outLen = is.read(outData)) != -1) {
                            recordOut.write(outData, 0, outLen);
                            Log.d("Demo","wtire data. len: "+outLen);
                        }
                        is.close();

                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }

                protected void finalize() {
                    try {
                        recordOut.close();
                        receiveOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            mRtcEngine.registerAudioFrameObserver(audioFrameObserver);
            //TODO
            //mRtcEngine.setExternalAudioSource(true, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_COUNT);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
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

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.
        mRtcEngine.enableVideo();

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(true);
        mLocalContainer.addView(view);
        // Initializes the local video view.
        // RENDER_MODE_HIDDEN: Uniformly scale the video until it fills the visible boundaries. One dimension of the video may have clipped contents.
        mLocalVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(mLocalVideo);
    }

    private void joinChannel() {
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name that
        // you use to generate this token.
        String token = getString(R.string.agora_access_token);
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
            token = null; // default, no token
        }
        mRtcEngine.joinChannel(token, "testRoom", "Extra Optional Data", 0);
    }

    private void startRecordThread(){
        mRecordThread = new Thread(() -> {
            //TODO 循环读取pcm文件,并且计时睡眠
            //循环调用 push 函数
            String fileName = getFilesDir().getAbsolutePath() + "/夜的钢琴曲.raw";
            String pcmRecordFileName = "";
            OutputStream recordOut = null;

            byte[] buffer = new byte[1920];  //10ms 数据
            FileInputStream input = null;
            long currentTime =  System.currentTimeMillis();
            try {
                input = new FileInputStream(fileName);
                while(true){
                    long beginTime = System.currentTimeMillis();

                    if(isWriteBackAudio){
                        int len = input.read(buffer);
                        if(len==-1)
                            break;

                        Log.d("Demo","Push ExternalAudioFrame");
                        mRtcEngine.pushExternalAudioFrame(buffer,currentTime,48000,2,2,1);

                        //写到文件里面去
                        if(pcmRecordFileName.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
                            String now = sdf.format(new Date());
                            sb.append("TIME:").append(now);
                            pcmRecordFileName = getCrashFilePath(VideoChatViewActivity.this)+now+"_record.raw";
                            recordOut = new FileOutputStream(pcmRecordFileName);
                            Log.d("Demo","will store data to file: "+pcmRecordFileName);
                        }
                        recordOut.write(buffer, 0, 1920);
                    }

                    long endTime = System.currentTimeMillis();
                    long sleepTime = 10-(endTime-beginTime);
                    if(sleepTime<0)
                        sleepTime = 0;

                    Thread.sleep(sleepTime);
                    currentTime+=10;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        mRecordThread.start();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mCallEnd) {
            leaveChannel();
        }
        /*
          Destroys the RtcEngine instance and releases all resources used by the Agora SDK.

          This method is useful for apps that occasionally make voice or video calls,
          to free up resources for other operations when not making calls.
         */
        RtcEngine.destroy();
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

    public void onLocalAudioMuteClicked(View view) {
        mMuted = !mMuted;
        // Stops/Resumes sending the local audio stream.
        mRtcEngine.muteLocalAudioStream(mMuted);
        int res = mMuted ? R.drawable.btn_mute : R.drawable.btn_unmute;
        mMuteBtn.setImageResource(res);
    }

    public void onSwitchCameraClicked(View view) {
        // Switches between front and rear cameras.
        mRtcEngine.switchCamera();
    }

    public void onCallClicked(View view) {
        if (mCallEnd) {
            startCall();
            mCallEnd = false;
            mCallBtn.setImageResource(R.drawable.btn_endcall);
        } else {
            endCall();
            mCallEnd = true;
            mCallBtn.setImageResource(R.drawable.btn_startcall);
        }

        showButtons(!mCallEnd);
    }

    private void startCall() {
        setupLocalVideo();
        joinChannel();
    }

    private void endCall() {
        removeFromParent(mLocalVideo);
        mLocalVideo = null;
        removeFromParent(mRemoteVideo);
        mRemoteVideo = null;
        leaveChannel();
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mMuteBtn.setVisibility(visibility);
        mSwitchCameraBtn.setVisibility(visibility);
    }

    private ViewGroup removeFromParent(VideoCanvas canvas) {
        if (canvas != null) {
            ViewParent parent = canvas.view.getParent();
            if (parent != null) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(canvas.view);
                return group;
            }
        }
        return null;
    }

    private void switchView(VideoCanvas canvas) {
        ViewGroup parent = removeFromParent(canvas);
        if (parent == mLocalContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }
            mRemoteContainer.addView(canvas.view);
        } else if (parent == mRemoteContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }
            mLocalContainer.addView(canvas.view);
        }
    }

    public void onLocalContainerClick(View view) {
        switchView(mLocalVideo);
        switchView(mRemoteVideo);
    }
}
