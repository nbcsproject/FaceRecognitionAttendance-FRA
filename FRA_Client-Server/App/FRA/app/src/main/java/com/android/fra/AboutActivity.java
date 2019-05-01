package com.android.fra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static com.android.fra.ActivityCollector.finishAll;
import static org.litepal.LitePalApplication.getContext;

public class AboutActivity extends BaseActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private boolean isEggOn;
    private boolean isEggFirstOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ImageView imageView = (ImageView) findViewById(R.id.about_image_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbar.setTitle(" ");
        int resource = R.drawable.about_image;
        Glide.with(this).load(resource).into(imageView);

        FloatingActionButton eggButton = (FloatingActionButton) findViewById(R.id.egg_button);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        isEggOn = pref.getBoolean("is_egg_on", false);
        eggButton.setOnClickListener(new View.OnClickListener() {
            long[] mHints = new long[10];

            @Override
            public void onClick(View v) {
                if (isEggOn == true) {
                    Toast.makeText(getContext(), "^_^", Toast.LENGTH_SHORT).show();
                } else {
                    System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
                    mHints[mHints.length - 1] = SystemClock.uptimeMillis();
                    if (SystemClock.uptimeMillis() - mHints[0] <= 10 * 10000 / 60) {
                        isEggFirstOn = true;
                        editor = pref.edit();
                        editor.putBoolean("is_egg_on", true);
                        editor.apply();
                        Toast.makeText(getContext(), "^_^", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        TextView aboutVersion = (TextView) findViewById(R.id.about_version);
        String code = "";
        PackageManager manager = getContext().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getContext().getPackageName(), 0);
            code = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        aboutVersion.setText(code);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isEggFirstOn == true) {
                    Intent intent = new Intent(AboutActivity.this, CameraActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finishAll();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isEggFirstOn == true) {
                Intent intent = new Intent(AboutActivity.this, CameraActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finishAll();
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
