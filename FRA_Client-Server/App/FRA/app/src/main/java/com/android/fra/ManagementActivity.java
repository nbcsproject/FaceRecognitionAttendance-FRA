package com.android.fra;

import android.content.ContentValues;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fra.db.Face;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManagementActivity extends AppCompatActivity implements ManagementAdapter.onItemClickListener {

    private List<Face> faceList = new ArrayList<>();
    private ManagementAdapter adapter;
    private TextView toolBarHeadText;
    private Map<Integer, Boolean> restoreMap;
    public SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        LitePal.deleteAll(Face.class, "valid = ?", "0");
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
                    }).show();
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
        if (adapter.getInDeletionMode() == true) {
            hideText();
            adapter.setInDeletionMode(false);
            adapter.notifyDataSetChanged();
            adapter.setMap(new LinkedHashMap<Integer, Boolean>());
            return true;
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
        setText("全选");
    }

    public void hideText() {
        toolBarHeadText.setVisibility(View.GONE);
    }

    private void refreshRecyclerView() {
        initFace();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.management_recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ManagementAdapter(faceList);
        recyclerView.setAdapter(adapter);
        adapter.setListener(this);
        LitePal.deleteAll(Face.class, "valid = ?", "0");
        swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    refreshRecyclerView();
                }
                break;
            default:
        }
    }

}