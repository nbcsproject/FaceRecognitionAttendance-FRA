package com.android.fra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

import static com.android.fra.ActivityCollector.finishAll;

public class CameraActivity extends BaseActivity {

    private SharedPreferences pref;
    private DrawerLayout mDrawerLayout;
    private String currentUid;
    private int captureMode;
    private boolean openDrawerLayout = false;
    private ShortcutManager mShortcutManager;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("CameraActivity", "initialization failed");
        } else {
            Log.d("CameraActivity", "Initialization successful");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!pref.getBoolean("isLogin", false)) {
            Intent intent = new Intent(CameraActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAll();
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            setContentView(R.layout.camera_activity_main);
            if (null == savedInstanceState) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.camera_container, CameraFragment.newInstance())
                        .commit();
            }

            Toolbar toolbar = (Toolbar) findViewById(R.id.camera_activity_toolBar);
            setSupportActionBar(toolbar);
            Intent intent = getIntent();
            currentUid = intent.getStringExtra("current_uid");
            captureMode = intent.getIntExtra("capture_mode", 0);
            if (captureMode == 0) {
                getSupportActionBar().setTitle("签到");
                NavigationView navView = (NavigationView) findViewById(R.id.camera_activity_nav_view);
                mDrawerLayout = (DrawerLayout) findViewById(R.id.camera_activity_drawer_layout);
                View headerLayout = navView.inflateHeaderView(R.layout.nav_header);
                ImageView drawerImageView = (ImageView) headerLayout.findViewById(R.id.nav_header_image);
                navView.setCheckedItem(R.id.nav_capture);
                TextView navTextView = (TextView) headerLayout.findViewById(R.id.nav_account);
                navTextView.setText(pref.getString("account", ""));
                Glide.with(this)
                        .load(R.drawable.nav_icon)
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(10, 5)))
                        .into(drawerImageView);
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
                }

                MenuItem menuItem = navView.getMenu().findItem(R.id.nav_management);
                boolean isEggOn = pref.getBoolean("is_egg_on", false);
                if (isEggOn) {
                    menuItem.setVisible(true);
                    setupShortcuts();
                } else {
                    menuItem.setVisible(false);
                }
                mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        openDrawerLayout = false;
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        openDrawerLayout = true;
                    }
                });
                navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                            mDrawerLayout.closeDrawers();
                        }
                        switch (menuItem.getItemId()) {
                            case R.id.nav_capture:
                                break;
                            case R.id.nav_register:
                                Intent registerIntent = new Intent(CameraActivity.this, RegisterActivity.class);
                                startActivity(registerIntent);
                                break;
                            case R.id.nav_management:
                                Intent managementIntent = new Intent(CameraActivity.this, ManagementActivity.class);
                                startActivity(managementIntent);
                                break;
                            case R.id.nav_settings:
                                Intent settingsIntent = new Intent(CameraActivity.this, SettingsActivity.class);
                                startActivity(settingsIntent);
                                break;
                            default:
                        }
                        return true;
                    }
                });
            } else if (captureMode == 1) {
                getSupportActionBar().setTitle("添加面孔");
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    private void setupShortcuts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return;
        }
        mShortcutManager = getSystemService(ShortcutManager.class);
        List<ShortcutInfo> shortcutInfos = new ArrayList<>();
        Intent intent = new Intent(this, ManagementActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        ShortcutInfo info = new ShortcutInfo.Builder(this, "dynamicShortcut0")
                .setShortLabel("管理")
                .setLongLabel("管理")
                .setIcon(Icon.createWithResource(this, R.mipmap.ic_management_shortcut))
                .setIntent(intent)
                .setRank(0)
                .build();
        shortcutInfos.add(info);
        mShortcutManager.setDynamicShortcuts(shortcutInfos);
    }

    public String getCurrentUid() {
        return currentUid;
    }

    public int getCaptureMode() {
        return captureMode;
    }

    public boolean getOpenDrawerLayout() {
        return openDrawerLayout;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && captureMode == 1) {
            Intent intent = new Intent(CameraActivity.this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onKeyDown(keyCode, event);
    }

}
