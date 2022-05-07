package com.qtt_video.chat.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

public class BaseDialog {

    protected Context mContext;
    protected AlertDialog mDialog;
    protected View mView;
    protected int mWidth = -1;
    private int mHeight;
    private int mGravity;

    public BaseDialog(Context context, int layoutRes) {
        this.mContext = context;
        mView = LayoutInflater.from(mContext).inflate(layoutRes, null);
        mDialog = new AlertDialog.Builder(mContext).create();
        mDialog.setView(mView);
        init();
    }

    public BaseDialog(Context context, View view) {
        this.mContext = context;
        this.mView = view;
        mDialog = new AlertDialog.Builder(mContext).create();
        if (view != null) {
            mDialog.setView(view);
            init();
        }
    }

    public void setView(View view){
        this.mView = view;
        mDialog.setView(view);
    }

    public void setCancelable(boolean b) {
        if (mDialog != null) {
            mDialog.setCancelable(b);
            mDialog.setCanceledOnTouchOutside(b);
        }
    }

    protected void init() {
    }

    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    public View getView() {
        return mView;
    }

    public void setAlpha(float alpha) {
        if (mView != null) {
            mView.setAlpha(0.7f);
        }
    }

    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
            Window window = mDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.gravity = mGravity;
                if (mWidth != 0) {
                    attributes.width = mWidth;
                }
                if (mHeight != 0) {
                    attributes.height = mHeight;
                }
                window.setAttributes(attributes);
            }
        }
    }

    public void dismiss() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }
}
