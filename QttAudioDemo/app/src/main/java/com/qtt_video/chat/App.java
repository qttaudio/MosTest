package com.qtt_video.chat;

import android.app.Application;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;

import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.BlackToastStyle;
import com.tencent.bugly.crashreport.CrashReport;

public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        CrashReport.initCrashReport(getApplicationContext(), "a0f995156d", true);
        CrashReport.setAppVersion(this, "test_video");
        ToastUtils.init(this);
        ToastUtils.setStyle(new BlackToastStyle(){
            @Override
            protected int getHorizontalPadding(Context context) {
                return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, context.getResources().getDisplayMetrics());
            }

            @Override
            protected int getVerticalPadding(Context context) {
                return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
            }
        });
        ToastUtils.setGravity(Gravity.CENTER);
    }

    public static Context getContext() {
        return context;
    }
}
