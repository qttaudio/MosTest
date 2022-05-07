package com.qtt_video.chat.dialog;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.qtt_video.chat.R;
import com.qtt_video.chat.ilistener.ViewCheckListener;
import com.qttaudio.sdk.channel.ChannelEngine;

public class MuteRemoteDialog extends BaseDialog {

    private AppCompatTextView tvUid;
    private AppCompatImageView ivExit;
    private AppCompatCheckBox cbAudio;
    private AppCompatCheckBox cbVideo;

    private ChannelEngine engine;
    private long uid;
    private ViewCheckListener<Integer> viewCheckListener;

    public MuteRemoteDialog(Context context, ChannelEngine engine) {
        super(context, R.layout.dialog_mute_remote);
        this.engine = engine;
    }

    public void setEngine(ChannelEngine engine) {
        this.engine = engine;
    }

    @Override
    protected void init() {
        super.init();
        tvUid = mView.findViewById(R.id.tv_uid);
        tvUid.setText("ID:" + uid);
        ivExit = mView.findViewById(R.id.iv_exit);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        cbAudio = mView.findViewById(R.id.cb_audio);
        cbAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (engine != null) {
                    engine.muteRemoteAudio(uid, !isChecked);
                    if (viewCheckListener != null) {
                        viewCheckListener.onCheckedChanged(R.id.cb_audio, !isChecked);
                    }
                }
            }
        });
        cbVideo = mView.findViewById(R.id.cb_video);
        cbVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (engine != null) {
                    engine.muteRemoteVideoStream((int) uid, !isChecked);
                    if (viewCheckListener != null) {
                        viewCheckListener.onCheckedChanged(R.id.cb_video, !isChecked);
                    }
                }
            }
        });
    }

    public void setUid(long uid) {
        this.uid = uid;
        tvUid.setText("ID:" + uid);
    }

    public void setCheckBox(boolean audio, boolean video) {
        cbAudio.setChecked(!audio);
        cbVideo.setChecked(!video);
    }

    public long getUid() {
        return uid;
    }

    public void setViewCheckListener(ViewCheckListener<Integer> viewCheckListener) {
        this.viewCheckListener = viewCheckListener;
    }
}
