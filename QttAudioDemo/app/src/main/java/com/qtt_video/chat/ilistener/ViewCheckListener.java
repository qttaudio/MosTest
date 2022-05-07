package com.qtt_video.chat.ilistener;

import android.view.View;

public interface ViewCheckListener<T> {

    void onCheckedChanged(T t, boolean isChecked);
}
