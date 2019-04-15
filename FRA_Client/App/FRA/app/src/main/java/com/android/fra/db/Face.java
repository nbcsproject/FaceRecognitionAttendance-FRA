package com.android.fra.db;

import org.litepal.crud.LitePalSupport;
import org.opencv.calib3d.StereoBM;
import org.opencv.core.Mat;

public class Face extends LitePalSupport {
    private String name;
    private String uid;
    private String gender;
    private String phone;
    private String email;
    private String department;
    private String post;
    private String feature;

    public String  getUid() {
        return uid;
    }

    public void setUid(String  uid) {
        this.uid = uid;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getGender(){
        return gender;
    }

    public void setGender(String gender){
        this.gender = gender;
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getDepartment(){
        return department;
    }

    public void setDepartment(String department){
        this.department = department;
    }

    public String getPost(){
        return post;
    }

    public void setPost(String post){
        this.post = post;
    }

    public String getFeature(){
        return feature;
    }

    public void setFeature(String feature){
        this.feature = feature;
    }
}
