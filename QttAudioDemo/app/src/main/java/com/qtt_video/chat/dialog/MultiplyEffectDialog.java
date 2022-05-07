package com.qtt_video.chat.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.hjq.toast.ToastUtils;
import com.qtt_video.chat.R;
import com.qtt_video.chat.dialog.BaseDialog;
import com.qtt_video.chat.ilistener.MyOnSeekBarChangeListener;
import com.qttaudio.sdk.channel.BeautyOptions;
import com.qttaudio.sdk.channel.ChannelEngine;

import java.nio.ByteBuffer;

public class MultiplyEffectDialog extends BaseDialog implements View.OnClickListener {

    private ChannelEngine mEngine;
    private AppCompatTextView tvBeauty;
    private AppCompatTextView tvFilter;
    private AppCompatTextView tvChangeVoice;
    private CheckBox cbStatus;
    private AppCompatImageView ivExit;
    private LinearLayout llBeauty;
    private SeekBar sbMicrodermabrasion;
    private SeekBar sbWhitening;
    private AppCompatTextView tvMicrodermabrasion;
    private AppCompatTextView tvWhitening;
    private LinearLayout llFilter;
    private FrameLayout flSelectBg1;
    private FrameLayout flSelectBg2;
    private FrameLayout flSelectBg3;
    private LinearLayout llChangeVoice;
    private AppCompatImageView ivRaw;
    private FrameLayout flUncle;
    private FrameLayout flLoli;

    private SparseBooleanArray selectStatue;
    private View vBeauty;
    private View vFilter;
    private View vChangeVoice;

    private boolean isOpenHandle;
    private AppCompatTextView tvTip;
    private AppCompatTextView tvRawIcon;
    private AppCompatTextView tvOld;
    private AppCompatTextView tvBlue;
    private AppCompatTextView tvRaw;
    private AppCompatTextView tvUncle;
    private AppCompatTextView tvLoli;

    private BeautyOptions beautyOptions;

    public MultiplyEffectDialog(Context context, ChannelEngine engine) {
        super(context, R.layout.dialog_multiply_effect);
        setGravity(Gravity.BOTTOM);
        setCancelable(false);

        this.mEngine = engine;
        beautyOptions = new BeautyOptions();
        beautyOptions.lightening = 0.0f;
        beautyOptions.smoothness = 0.0F;
    }

    @Override
    protected void init() {
        super.init();
        tvBeauty = mView.findViewById(R.id.tv_beauty);
        tvFilter = mView.findViewById(R.id.tv_filter);
        tvChangeVoice = mView.findViewById(R.id.tv_changeVoice);
        ivExit = mView.findViewById(R.id.iv_exit);
        cbStatus = mView.findViewById(R.id.cb_status);
        cbStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOpenHandle = isChecked;
                enableProgress();
            }
        });

        llBeauty = mView.findViewById(R.id.ll_beauty);
        sbMicrodermabrasion = mView.findViewById(R.id.sb_microdermabrasion);
        tvMicrodermabrasion = mView.findViewById(R.id.tv_microdermabrasion);
        sbWhitening = mView.findViewById(R.id.sb_whitening);
        tvWhitening = mView.findViewById(R.id.tv_whitening);
        sbMicrodermabrasion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !isOpenHandle;
            }
        });
        sbMicrodermabrasion.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                super.onProgressChanged(seekBar, progress, fromUser);
                float result = progress / 1.0f / 10;
                beautyOptions.smoothness = result;
                mEngine.setBeautyEffectOptions(isOpenHandle, beautyOptions);
                tvMicrodermabrasion.setText("" + result);
            }
        });
        sbWhitening.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !isOpenHandle;
            }
        });
        sbWhitening.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                super.onProgressChanged(seekBar, progress, fromUser);
                float result = progress / 1.0f / 10;
                beautyOptions.lightening = result;
                mEngine.setBeautyEffectOptions(isOpenHandle, beautyOptions);
                tvWhitening.setText("" + result);
            }
        });

        llFilter = mView.findViewById(R.id.ll_filter);
        flSelectBg1 = mView.findViewById(R.id.fl_selectBg1);
        flSelectBg2 = mView.findViewById(R.id.fl_selectBg2);
        flSelectBg3 = mView.findViewById(R.id.fl_selectBg3);
        vBeauty = mView.findViewById(R.id.v_beauty);
        vFilter = mView.findViewById(R.id.v_filter);
        vChangeVoice = mView.findViewById(R.id.v_changeVoice);

        llChangeVoice = mView.findViewById(R.id.ll_changeVoice);
        ivRaw = mView.findViewById(R.id.iv_raw);
        flUncle = mView.findViewById(R.id.fl_uncle);
        flLoli = mView.findViewById(R.id.fl_loli);

        tvTip = mView.findViewById(R.id.tv_tip);

        tvRawIcon = mView.findViewById(R.id.tv_rawIcon);
        tvOld = mView.findViewById(R.id.tv_old);
        tvBlue = mView.findViewById(R.id.tv_blue);
        tvRaw = mView.findViewById(R.id.tv_raw);
        tvUncle = mView.findViewById(R.id.tv_uncle);
        tvLoli = mView.findViewById(R.id.tv_loli);

        tvBeauty.setOnClickListener(this);
        tvFilter.setOnClickListener(this);
        tvChangeVoice.setOnClickListener(this);
        flSelectBg1.setOnClickListener(this);
        flSelectBg2.setOnClickListener(this);
        flSelectBg3.setOnClickListener(this);
        ivRaw.setOnClickListener(this);
        flUncle.setOnClickListener(this);
        flLoli.setOnClickListener(this);
        ivExit.setOnClickListener(this);

        enableProgress();
    }

    private void switchPage(int id) {
        boolean b1 = id == tvBeauty.getId();
        boolean b2 = id == tvFilter.getId();
        boolean b3 = id == tvChangeVoice.getId();
        llBeauty.setVisibility(b1 ? View.VISIBLE : View.GONE);
        llFilter.setVisibility(b2 ? View.VISIBLE : View.GONE);
        llChangeVoice.setVisibility(b3 ? View.VISIBLE : View.GONE);

        tvBeauty.setTextColor(Color.parseColor(b1 ? "#ff000000" : "#ff777777"));
        tvFilter.setTextColor(Color.parseColor(b2 ? "#ff000000" : "#ff777777"));
        tvChangeVoice.setTextColor(Color.parseColor(b3 ? "#ff000000" : "#ff777777"));

        vBeauty.setVisibility(b1 ? View.VISIBLE : View.INVISIBLE);
        vFilter.setVisibility(b2 ? View.VISIBLE : View.INVISIBLE);
        vChangeVoice.setVisibility(b3 ? View.VISIBLE : View.INVISIBLE);
    }

    private void enableProgress() {
        if (beautyOptions != null) {
            mEngine.setBeautyEffectOptions(isOpenHandle, beautyOptions);
        }

        tvTip.setVisibility(isOpenHandle ? View.INVISIBLE : View.VISIBLE);
        tvTip.setText("提示：当前美化已关闭，该功能不能使用");

        sbMicrodermabrasion.setThumb(mContext.getDrawable(isOpenHandle ? R.drawable.shape_circle_point : R.drawable.shape_circle_point_gray));
        sbMicrodermabrasion.setProgressDrawable(mContext.getDrawable(isOpenHandle ? R.drawable.layer_progress_horizontal : R.drawable.layer_gray_progress_horizontal));
        sbWhitening.setThumb(mContext.getDrawable(isOpenHandle ? R.drawable.shape_circle_point : R.drawable.shape_circle_point_gray));
        sbWhitening.setProgressDrawable(mContext.getDrawable(isOpenHandle ? R.drawable.layer_progress_horizontal : R.drawable.layer_gray_progress_horizontal));

        flSelectBg2.setBackground(isOpenHandle ? null : mContext.getResources().getDrawable(R.drawable.shape_gray_bround1));
        flSelectBg1.setBackground(isOpenHandle ? null : mContext.getResources().getDrawable(R.drawable.shape_gray_bround1));
        flSelectBg3.setBackground(isOpenHandle ? null : mContext.getResources().getDrawable(R.drawable.shape_gray_bround1));

        ivRaw.setImageResource(isOpenHandle ? R.drawable.raw_close : R.drawable.raw_ban);
        flUncle.setBackground(isOpenHandle ? null : mContext.getResources().getDrawable(R.drawable.shape_gray_bround1));
        flLoli.setBackground(isOpenHandle ? null : mContext.getResources().getDrawable(R.drawable.shape_gray_bround1));

        tvRawIcon.setTextColor(Color.parseColor(isOpenHandle ? "#333333" : "#BCBABA"));
        tvOld.setTextColor(Color.parseColor(isOpenHandle ? "#333333" : "#BCBABA"));
        tvBlue.setTextColor(Color.parseColor(isOpenHandle ? "#333333" : "#BCBABA"));

        tvRaw.setTextColor(Color.parseColor(isOpenHandle ? "#333333" : "#BCBABA"));
        tvUncle.setTextColor(Color.parseColor(isOpenHandle ? "#333333" : "#BCBABA"));
        tvLoli.setTextColor(Color.parseColor(isOpenHandle ? "#333333" : "#BCBABA"));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_raw:
            case R.id.fl_uncle:
            case R.id.fl_loli:
                changeVoice(v.getId());
                break;
            case R.id.fl_selectBg1:
            case R.id.fl_selectBg2:
            case R.id.fl_selectBg3:
                changeFilter(v.getId());
                break;
            case R.id.tv_beauty:
            case R.id.tv_filter:
            case R.id.tv_changeVoice:
                switchPage(v.getId());
                break;
            case R.id.iv_exit:
                dismiss();
                break;

        }
    }

    private void changeFilter(int id) {
        if (!isOpenHandle) {
            return;
        }
        boolean b1 = id == R.id.fl_selectBg1;
        boolean b2 = id == R.id.fl_selectBg2;
        boolean b3 = id == R.id.fl_selectBg3;

        flSelectBg1.setBackground(mContext.getResources().getDrawable(b1 ? R.drawable.shape_blue_bround : R.drawable.shape_gray_bround1));
        flSelectBg2.setBackground(mContext.getResources().getDrawable(b2 ? R.drawable.shape_blue_bround : R.drawable.shape_gray_bround1));
        flSelectBg3.setBackground(mContext.getResources().getDrawable(b3 ? R.drawable.shape_blue_bround : R.drawable.shape_gray_bround1));

        tvRawIcon.setTextColor(Color.parseColor(b1 ? "#2B6AF9" : "#333333"));
        tvOld.setTextColor(Color.parseColor(b2 ? "#2B6AF9" : "#333333"));
        tvBlue.setTextColor(Color.parseColor(b3 ? "#2B6AF9" : "#333333"));


        if (b1) {
            ToastUtils.show("已选择滤镜：原图");
        }
        if (b2) {
            ToastUtils.show("已选择滤镜：复古");
        }
        if (b3) {
            ToastUtils.show("已选择滤镜：蓝调");
        }

    }

    private void changeVoice(int id) {
        if (!isOpenHandle) {
            return;
        }
        boolean b1 = id == R.id.iv_raw;
        boolean b2 = id == R.id.fl_uncle;
        boolean b3 = id == R.id.fl_loli;
        ivRaw.setImageResource(b1 ? R.drawable.raw_audio : R.drawable.raw_close);
        flUncle.setBackground(mContext.getResources().getDrawable(b2 ? R.drawable.shape_blue_bround : R.drawable.shape_gray_bround1));
        flLoli.setBackground(mContext.getResources().getDrawable(b3 ? R.drawable.shape_blue_bround : R.drawable.shape_gray_bround1));

        tvRaw.setTextColor(Color.parseColor(b1 ? "#2B6AF9" : "#333333"));
        tvUncle.setTextColor(Color.parseColor(b2 ? "#2B6AF9" : "#333333"));
        tvLoli.setTextColor(Color.parseColor(b3 ? "#2B6AF9" : "#333333"));

        if (b1) {
            ToastUtils.show("已选择变声：原声");
        }
        if (b2) {
            ToastUtils.show("已选择变声：大叔音");
        }
        if (b3) {
            ToastUtils.show("已选择变声：萝莉音");
        }
    }

    public void setEngine(ChannelEngine mEngine) {
        this.mEngine = mEngine;
    }
}
