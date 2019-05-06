package com.android.fra;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fra.db.Face;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.litepal.LitePal;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.android.fra.ActivityCollector.finishAll;
import static org.litepal.LitePalApplication.getContext;

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

    Handler updateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(getContext(), "未连接至服务器", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void updateInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Face> faceList = faceList = LitePal.where("valid = ?", "1").find(Face.class);
                if (!faceList.isEmpty()) {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://10.10.19.134:3000/app/query")
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        Message msg = new Message();
                        if (!responseData.equals("error")) {
                            Gson gson = new Gson();
                            List<Face> updateFaceList = gson.fromJson(responseData, new TypeToken<List<Face>>() {
                            }.getType());
                            for (Face localFace : faceList) {
                                int existence = 0;
                                for (Face serverFace : updateFaceList) {
                                    if (localFace.getUid().equals(serverFace.getUid())) {
                                        if (!localFace.getModTime().equals(serverFace.getModTime())) {
                                            serverFace.updateAll("uid = ?", serverFace.getUid());
                                        }
                                        existence = 1;
                                    }
                                }
                                if (existence == 0) {
                                    LitePal.deleteAll(Face.class, "uid = ?", localFace.getUid());
                                }
                            }
                            msg.what = 0;
                        } else {
                            msg.what = 1;
                        }
                        updateHandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e instanceof SocketTimeoutException) {
                            Message msg = new Message();
                            msg.what = 1;
                            updateHandler.sendMessage(msg);
                        }
                        if (e instanceof ConnectException) {
                            Message msg = new Message();
                            msg.what = 1;
                            updateHandler.sendMessage(msg);
                        }
                    }
                } else {
                    Message msg = new Message();
                    msg.what = 0;
                    updateHandler.sendMessage(msg);
                }
            }
        }).start();
    }

}
