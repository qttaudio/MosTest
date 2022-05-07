package com.qtt_video.chat.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.qtt_video.chat.R;
import com.qtt_video.chat.dialog.MusicControlDialog;
import com.qtt_video.chat.dialog.MultiplyEffectDialog;
import com.qtt_video.chat.dialog.ResolutionDialog;
import com.qttaudio.sdk.channel.ChannelEngine;

import java.util.Random;

public abstract class BaseActivity extends AppCompatActivity {

    protected AppCompatTextView tvRoomName;
    protected AppCompatTextView tvMySelfUid;
    protected AppCompatImageView ivSwitchCamera;
    protected AppCompatImageView ivExit;
    protected AppCompatImageView ivMic;
    protected AppCompatImageView ivVideo;
    protected AppCompatImageView ivMusic;
    protected AppCompatImageView ivBeauty;
    protected AppCompatImageView ivMore;
    protected AppCompatImageView ivSound;

    protected Handler handler;
    protected MusicControlDialog musicControlDialog;
    protected MultiplyEffectDialog multiplyEffectDialog;
    protected ResolutionDialog resolutionDialog;

    protected ChannelEngine channelEngine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());

        handler = new Handler(Looper.getMainLooper());

        setUpView();
    }

    public abstract int getLayoutRes();

    protected void setUpView() {
        tvRoomName = findViewById(R.id.tv_roomName);
        tvMySelfUid = findViewById(R.id.tv_mySelfUid);
        ivSwitchCamera = findViewById(R.id.iv_switchCamera);
        ivExit = findViewById(R.id.iv_exit);

        ivMic = findViewById(R.id.iv_mic);
        ivVideo = findViewById(R.id.iv_video);
        ivMusic = findViewById(R.id.iv_music);
        ivBeauty = findViewById(R.id.iv_beauty);
        ivMore = findViewById(R.id.iv_more);
        ivSound = findViewById(R.id.iv_sound);

    }

    protected void initDialogs() {
        musicControlDialog = new MusicControlDialog(this, channelEngine);
        multiplyEffectDialog = new MultiplyEffectDialog(this, channelEngine);
        resolutionDialog = new ResolutionDialog(this);
    }

    public int getRandomUid() {
        String uid = "";
        for (int i = 0; i < 5; i++) {
            int value = new Random().nextInt(9) + 1;
            uid += String.valueOf(value);
        }
        return Integer.parseInt(uid);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (channelEngine != null) {
                channelEngine.leave();
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
