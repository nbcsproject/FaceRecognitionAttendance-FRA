package com.android.fra.db;

import org.litepal.crud.LitePalSupport;
import org.opencv.core.Mat;

public class Face extends LitePalSupport {
    private String name;
    private int uid;
    private int pages;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    private String gender;
    private String feature;
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getGender(){
        return gender;
    }
    public void setGender(String gender){
        this.gender=gender;
    }
    public String getFeature(){
        return feature;
    }
    public void setFeature(String feature){
        this.feature=feature;
    }
}
