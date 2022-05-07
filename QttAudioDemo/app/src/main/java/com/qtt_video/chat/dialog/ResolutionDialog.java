package com.qtt_video.chat.dialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.PopupWindowCompat;

import com.qtt_video.chat.R;
import com.qtt_video.chat.ilistener.ViewCheckListener;
import com.qtt_video.chat.utils.Constants;

public class ResolutionDialog extends BaseDialog {

    private RelativeLayout rlResolution;
    private AppCompatImageView ivExit;
    private AppCompatTextView tvResolution;
    private AppCompatImageView ivArrow;
    private AppCompatTextView tvTip;
    private LinearLayout llEnableVideo;
    private LinearLayout llResolution;
    private AppCompatCheckBox cbEnableVideo;
    private ViewCheckListener<View> viewCheckListener;

    private boolean isAllowChangePixel = true;
    private ChangePixelListener changePixelListener;
    private ResolutionPopup resolutionPopup;
    private Context mContext;


    public ResolutionDialog(Context context) {
        super(context, R.layout.dialog_more);
        setGravity(Gravity.BOTTOM);
        mContext = context;
    }


    @Override
    protected void init() {
        super.init();
        llEnableVideo = mView.findViewById(R.id.ll_enableVideo);
        llResolution = mView.findViewById(R.id.ll_resolution);
        cbEnableVideo = mView.findViewById(R.id.cb_enableVideo);
        cbEnableVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (viewCheckListener != null) {
                    viewCheckListener.onCheckedChanged(buttonView, isChecked);
                }
                setAllowChangePixel(isChecked);
//                isAllowChangePixel = isChecked;
//                rlResolution.setEnabled(isAllowChangePixel);
//                tvResolution.setTextColor(isAllowChangePixel ? Color.parseColor("#ff333333") : Color.parseColor("#ffbcbaba"));
                ivArrow.setImageResource(isAllowChangePixel ? R.mipmap.arrow_down : R.mipmap.gray_arrow_down);
            }
        });
        rlResolution = mView.findViewById(R.id.rl_resolution);
        rlResolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!isAllowChangePixel) {
//                    return;
//                }
                if (resolutionPopup == null) {
                    resolutionPopup = new ResolutionPopup(mContext);
                }
                if (!resolutionPopup.popupWindow.isShowing()) {
                    resolutionPopup.showAsDropDown(rlResolution);
                }
            }
        });
        tvResolution = mView.findViewById(R.id.tv_resolution);
        ivArrow = mView.findViewById(R.id.iv_arrow);
        tvTip = mView.findViewById(R.id.tv_tip);

        ivExit = mView.findViewById(R.id.iv_exit);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void openVideoEnable() {
        llEnableVideo.setVisibility(View.VISIBLE);
    }

    public void openResolutionEnable(boolean b) {
        llResolution.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setAllowChangePixel(boolean allowChangePixel) {
//        isAllowChangePixel = allowChangePixel;
//        rlResolution.setEnabled(isAllowChangePixel);
//        tvResolution.setTextColor(isAllowChangePixel ? Color.parseColor("#ff333333") : Color.parseColor("#ffbcbaba"));
//        ivArrow.setImageResource(isAllowChangePixel ? R.mipmap.arrow_down : R.mipmap.gray_arrow_down);
//        tvTip.setVisibility(isAllowChangePixel ? View.INVISIBLE : View.VISIBLE);
    }

    public void setViewCheckListener(ViewCheckListener<View> viewCheckListener) {
        this.viewCheckListener = viewCheckListener;
    }

    public void setChangePixelListener(ChangePixelListener changePixelListener) {
        this.changePixelListener = changePixelListener;
    }

    public class ResolutionPopup {

        private View mView;
        private PopupWindow popupWindow;

        public ResolutionPopup(Context context) {
            mView = LayoutInflater.from(context).inflate(R.layout.layout_select_resolution, null);
            popupWindow = new PopupWindow(mView, -2, -2);
            popupWindow.setWidth(rlResolution.getWidth());
            popupWindow.setOutsideTouchable(true);
            popupWindow.setClippingEnabled(false);

            mView.findViewById(R.id.tv_360p).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (changePixelListener != null) {
                        changePixelListener.pixel(Constants.PIXEL_TYPE_1);
                    }
                    tvResolution.setText("360P");
                    popupWindow.dismiss();
                }
            });

            mView.findViewById(R.id.tv_720p).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (changePixelListener != null) {
                        changePixelListener.pixel(Constants.PIXEL_TYPE_2);
                    }
                    tvResolution.setText("480P");
                    popupWindow.dismiss();
                }
            });

            mView.findViewById(R.id.tv_1080p).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (changePixelListener != null) {
                        changePixelListener.pixel(Constants.PIXEL_TYPE_3);
                    }
                    tvResolution.setText("720P");
                    popupWindow.dismiss();
                }
            });
        }

        public void showAsDropDown(View anchor) {
            int[] location = new int[2];
            anchor.getLocationOnScreen(location);
            DisplayMetrics outMetrics = new DisplayMetrics();
            Context context = anchor.getContext();
            ((Activity) context).getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
//            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, (location[0] + anchor.getWidth() / 2) - popupWindow.getWidth() / 2
//                    , location[1] - outMetrics.heightPixels);
//            offsetX = Math.abs(mWindow.getContentView().getMeasuredWidth()-anchor.getWidth()) / 2;

            PopupWindowCompat.showAsDropDown(popupWindow, anchor, 0, (int) (-rlResolution.getHeight()*5-context.getResources().getDimension(R.dimen.qb_px_10)), Gravity.NO_GRAVITY);

        }

    }

    public interface ChangePixelListener {
        void pixel(int type);
    }
}
