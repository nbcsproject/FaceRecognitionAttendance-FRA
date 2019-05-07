package com.android.fra;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;

import java.util.ArrayList;
import java.util.List;

public class OSAboutActivity extends BaseActivity implements OSAboutAdapter.onItemClickListener {
    private OSAboutAdapter adapter;
    private List<About> aboutList = new ArrayList<>();
    private List<String> urlList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_os_about);
        SlidrConfig.Builder mBuilder = new SlidrConfig.Builder().edge(true);
        SlidrConfig mSlidrConfig = mBuilder.build();
        Slidr.attach(this, mSlidrConfig);
        Toolbar toolbar = (Toolbar) findViewById(R.id.os_about_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("开源相关");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initAboutList();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.os_about_recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new OSAboutAdapter(aboutList);
        recyclerView.setAdapter(adapter);
        adapter.setListener(this);
    }

    private void initAboutList() {
        aboutList.clear();
        About[] abouts = {new About("Glide-transformations", "wasabeef", "An Android transformation library providing a variety of image transformations for Glide."),
                new About("LitePal", "LitePalFramework", "LitePal is an open source Android library that allows developers to use SQLite database extremely easy."),
                new About("OptionFrame", "q805699513", "一款Android弹出框、对话框、Dialog、PopupWindow."),
                new About("CircleImageView", "hdodenhof", "A fast circular ImageView perfect for profile images."),
                new About("Okhttp", "square", "An HTTP & HTTP/2 client for Android and Java applications."),
                new About("Gson", "Google", "Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used to convert a JSON string to an equivalent Java object. Gson can work with arbitrary Java objects including pre-existing objects that you do not have source-code of."),
                new About("Slidr", "r0adkll", "Easily add slide-to-dismiss functionality to your Activity.")};
        urlList.add("https://github.com/wasabeef/glide-transformations");
        urlList.add("https://github.com/LitePalFramework/LitePal");
        urlList.add("https://github.com/q805699513/OptionFrame");
        urlList.add("https://github.com/hdodenhof/CircleImageView");
        urlList.add("https://github.com/square/okhttp");
        urlList.add("https://github.com/google/gson");
        urlList.add("https://github.com/r0adkll/Slidr");
        for (int i = 0; i < abouts.length; i++) {
            aboutList.add(abouts[i]);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public List<String> getUrlList() {
        return urlList;
    }

}
