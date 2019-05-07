package com.android.fra;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fra.db.Face;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.litepal.LitePal;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.android.fra.ActivityCollector.finishAll;
import static org.litepal.LitePalApplication.getContext;

public class ManagementActivity extends BaseActivity implements ManagementAdapter.onItemClickListener {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private List<Face> faceList = new ArrayList<>();
    private List<Face> deleteFace = new ArrayList<>();
    private ManagementAdapter adapter;
    private TextView toolBarHeadText;
    private Map<Integer, Boolean> restoreMap;
    public SwipeRefreshLayout swipeRefresh;
    private DrawerLayout mDrawerLayout;
    private static boolean fingerprintReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (!pref.getBoolean("isLogin", false)) {
            Intent intent = new Intent(ManagementActivity.this, LoginActivity.class);
            startActivity(intent);
            finishAll();
        } else {
            if (pref.getBoolean("is_set_fingerprint", false) && pref.getBoolean("is_set_management_fingerprint", false) && !fingerprintReturn) {
                Intent intent = new Intent(ManagementActivity.this, FingerprintActivity.class);
                startActivityForResult(intent, 0);
            }
            setContentView(R.layout.activity_management);
            Toolbar toolbar = (Toolbar) findViewById(R.id.management_activity_toolBar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("管理");
            NavigationView navView = (NavigationView) findViewById(R.id.management_activity_nav_view);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.management_activity_drawer_layout);
            View headerLayout = navView.inflateHeaderView(R.layout.nav_header);
            ImageView drawerImageView = (ImageView) headerLayout.findViewById(R.id.nav_header_image);
            TextView navTextView = (TextView) headerLayout.findViewById(R.id.nav_account);
            navView.setCheckedItem(R.id.nav_management);
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
            if (isEggOn == true) {
                menuItem.setVisible(true);
            } else {
                menuItem.setVisible(false);
            }
            navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                        mDrawerLayout.closeDrawers();
                    }
                    switch (menuItem.getItemId()) {
                        case R.id.nav_capture:
                            updateInBackground();
                            Intent cameraIntent = new Intent(ManagementActivity.this, CameraActivity.class);
                            cameraIntent.putExtra("capture_mode", 0);
                            startActivity(cameraIntent);
                            break;
                        case R.id.nav_register:
                            updateInBackground();
                            Intent registerIntent = new Intent(ManagementActivity.this, RegisterActivity.class);
                            startActivity(registerIntent);
                            break;
                        case R.id.nav_management:
                            updateInBackground();
                            break;
                        case R.id.nav_settings:
                            updateInBackground();
                            Intent settingsIntent = new Intent(ManagementActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            break;
                        default:
                    }
                    return true;
                }
            });
            updateInBackground();
            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.management_recyclerView);
            FloatingActionButton delete_button = (FloatingActionButton) findViewById(R.id.management_delete);
            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adapter.getMap().size() != 0) {
                        deleteItems();
                        Snackbar.make(view, "已删除", Snackbar.LENGTH_SHORT).setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                restoreItems();
                                Toast.makeText(ManagementActivity.this, "已恢复删除的信息", Toast.LENGTH_SHORT).show();
                            }
                        }).setActionTextColor(getResources().getColor(R.color.colorAccent)).show();
                    }
                }
            });
            initFace();
            GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new ManagementAdapter(faceList);
            recyclerView.setAdapter(adapter);
            adapter.setListener(this);
            toolBarHeadText = (TextView) findViewById(R.id.toolbar_headText);
            toolBarHeadText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectAllOrNot();
                }
            });
            swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
            swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshRecyclerView();
                }
            });
        }

    }

    private void initFace() {
        faceList = LitePal.findAll(Face.class);
    }

    private void selectAllOrNot() {
        if (adapter.getMap().size() < faceList.size()) {
            Map<Integer, Boolean> map = adapter.getMap();
            for (int i = 0; i < faceList.size(); i++) {
                map.put(i, true);
            }
            adapter.setMap(map);
            adapter.notifyDataSetChanged();
            setText("取消全选");
        } else {
            adapter.setMap(new LinkedHashMap<Integer, Boolean>());
            adapter.notifyDataSetChanged();
            setText("全选");
        }
    }

    public void deleteItems() {
        Map<Integer, Boolean> map = adapter.getMap();
        restoreMap = map;
        for (Map.Entry<Integer, Boolean> m : map.entrySet()) {
            ContentValues values = new ContentValues();
            values.put("valid", false);
            LitePal.updateAll(Face.class, values, "uid = ?", faceList.get(m.getKey()).getUid());
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.management_recyclerView);
        List<Face> currentFaceList = LitePal.where("valid = ?", "1").find(Face.class);
        adapter = new ManagementAdapter(currentFaceList);
        recyclerView.setAdapter(adapter);
        hideText();
        adapter.setListener(this);
    }

    public void restoreItems() {
        Map<Integer, Boolean> map = restoreMap;
        for (Map.Entry<Integer, Boolean> m : map.entrySet()) {
            ContentValues values = new ContentValues();
            values.put("valid", true);
            LitePal.updateAll(Face.class, values, "uid = ?", faceList.get(m.getKey()).getUid());
        }
        faceList = LitePal.where("valid = ?", "1").find(Face.class);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.management_recyclerView);
        adapter = new ManagementAdapter(faceList);
        recyclerView.setAdapter(adapter);
        adapter.setListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (adapter.getInDeletionMode()) {
            hideText();
            adapter.setInDeletionMode(false);
            adapter.notifyDataSetChanged();
            adapter.setMap(new LinkedHashMap<Integer, Boolean>());
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            updateInBackground();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setText(String text) {
        toolBarHeadText.setText(text);
    }

    @Override
    public void showText() {
        toolBarHeadText.setVisibility(View.VISIBLE);
        if (adapter.getMap().size() < faceList.size()) {
            setText("全选");
        } else {
            setText("取消全选");
        }
    }

    public void hideText() {
        toolBarHeadText.setVisibility(View.GONE);
    }

    private void refreshRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.management_recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ManagementAdapter(faceList);
        recyclerView.setAdapter(adapter);
        adapter.setListener(this);
        updateInBackground();
    }

    Handler deleteHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    LitePal.deleteAll(Face.class, "valid = ?", "0");
                    deleteFace.clear();
                    initFace();
                    adapter.setFaceList(faceList);
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), "信息更新成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    Handler updateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (deleteFace.isEmpty()) {
                        initFace();
                        adapter.setFaceList(faceList);
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), "信息更新成功", Toast.LENGTH_SHORT).show();
                    } else {
                        deleteInBackground();
                    }
                    break;
                case 1:
                    Toast.makeText(getContext(), "未连接至服务器", Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void deleteInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteFace = LitePal.where("valid = ?", "0").find(Face.class);
                if (!deleteFace.isEmpty()) {
                    List<Face> jsonFaceList = new ArrayList<>();
                    for (Face face : deleteFace) {
                        Face faceTemp = new Face();
                        faceTemp.setUid(face.getUid());
                        jsonFaceList.add(faceTemp);
                    }
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String faceJSON = gson.toJson(jsonFaceList);
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), faceJSON);
                    Request request = new Request.Builder()
                            .url("http://10.10.19.134:3000/app/delete")
                            .post(requestBody)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        Message msg = new Message();
                        if (responseData.equals("ok")) {
                            msg.what = 0;
                        } else {
                            msg.what = 1;
                        }
                        deleteHandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (e instanceof SocketTimeoutException) {
                            Message msg = new Message();
                            msg.what = 1;
                            deleteHandler.sendMessage(msg);
                        }
                        if (e instanceof ConnectException) {
                            Message msg = new Message();
                            msg.what = 1;
                            deleteHandler.sendMessage(msg);
                        }
                    }
                }
            }
        }).start();
    }

    private void updateInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    fingerprintReturn = data.getBooleanExtra("fingerprint_return", false);
                } else {
                    finish();
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    refreshRecyclerView();
                }
                break;
            default:
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

}
