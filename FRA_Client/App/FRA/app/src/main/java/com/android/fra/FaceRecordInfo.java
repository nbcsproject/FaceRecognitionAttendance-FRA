package com.android.fra;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class FaceRecordInfo extends AppCompatActivity {
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_record_info);
        ImageView imageView = (ImageView) findViewById(R.id.recordInfo_image_view);
        int resource = R.drawable.record_info_image;
        Glide.with(this).load(resource).into(imageView);
        Button button_registerFace = (Button) findViewById(R.id.record_continue_button);
        button_registerFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FaceRecordInfo.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        Button button_cancelRegister = (Button) findViewById(R.id.record_cancel_button);
        button_cancelRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FaceRecordInfo.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
