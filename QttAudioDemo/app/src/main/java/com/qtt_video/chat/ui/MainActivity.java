package com.qtt_video.chat.ui;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;


import com.qtt_video.chat.R;
import com.qtt_video.chat.utils.Constants;
import com.qtt_video.chat.utils.FileUtil;
import com.qtt_video.chat.utils.StatusBarUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSION_REQUEST_CODE = 0x0001;
    // 要申请的权限
    private String[] permissions = {Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String[] musicPaths = {"/assets/夜的钢琴曲.mp3"};
    private AppCompatEditText mEdRoomName;
    private AppCompatImageView ivClear;
    private AppCompatButton btnJoinRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarUtil.darkMode(this);
        if (!isNeedRequestPermission()) {
            copyFile();
        }

        ivClear = findViewById(R.id.iv_clear);
        ivClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivClear.setVisibility(View.GONE);
                mEdRoomName.setText("");
                btnJoinRoom.setEnabled(false);
            }
        });

        mEdRoomName = findViewById(R.id.ed_room_name);
        btnJoinRoom = findViewById(R.id.btn_join_room);
        btnJoinRoom.setEnabled(mEdRoomName.getText().toString().trim().length() > 0);
        mEdRoomName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnJoinRoom.setEnabled(s.length() != 0);
                ivClear.setVisibility(s.length() == 0 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnJoinRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void copyFile() {
        for (int i = 0; i < musicPaths.length; i++) {
            String name = musicPaths[i];
            File file = new File(getFilesDir().getAbsolutePath(), name.substring(8));
            if (!file.exists()) {
                FileUtil.copyAssetsFile(this, name.substring(8));
            }
        }
    }

    public void login() {
        final String trim = mEdRoomName.getText().toString().trim();
        if (TextUtils.isEmpty(trim)) {
            Toast.makeText(MainActivity.this, "房间名不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(Constants.ROOM_NAME, trim);
        intent.setClass(MainActivity.this, VideoChatActivity.class);
        startActivity(intent);
    }


    private boolean isNeedRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限授权成功", Toast.LENGTH_SHORT).show();
                copyFile();
            } else {
                Toast.makeText(this, "应用必要拥有：" + Manifest.permission.RECORD_AUDIO + "才能正常使用", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


}