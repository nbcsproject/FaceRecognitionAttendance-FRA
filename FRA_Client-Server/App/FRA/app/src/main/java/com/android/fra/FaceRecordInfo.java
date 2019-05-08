package com.android.fra;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.fra.db.Face;
import com.bumptech.glide.Glide;

import org.litepal.LitePal;

public class FaceRecordInfo extends BaseActivity {
    private String currentUid;
    private String currentPid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_record_info);
        Intent intent = getIntent();
        currentUid = intent.getStringExtra("current_uid");
        currentPid = intent.getStringExtra("current_pid");
        ImageView imageView = (ImageView) findViewById(R.id.recordInfo_image_view);
        int resource = R.drawable.record_info_image;
        Glide.with(this).load(resource).into(imageView);
        Button button_registerFace = (Button) findViewById(R.id.record_continue_button);
        button_registerFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FaceRecordInfo.this, CameraActivity.class);
                intent.putExtra("current_uid", currentUid);
                intent.putExtra("capture_mode", 1);
                startActivity(intent);
            }
        });

        Button button_cancelRegister = (Button) findViewById(R.id.record_cancel_button);
        button_cancelRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LitePal.deleteAll(Face.class, "uid = ? and pid = ?", currentUid, currentPid);
                Intent intent = new Intent(FaceRecordInfo.this, CameraActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            LitePal.deleteAll(Face.class, "uid = ? and pid = ?", currentUid, currentPid);
            Intent intent = new Intent(FaceRecordInfo.this, CameraActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }

}
