package com.android.fra.db;

import org.litepal.crud.LitePalSupport;

public class Face extends LitePalSupport {
    private String pid;
    private String uid;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private String department;
    private String post;
    private String feature;
    private Boolean valid;
    private String modTime;
    private String check_status;
    private String current_checktime;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public String getModTime() {
        return modTime;
    }

    public void setModTime(String modTime) {
        this.modTime = modTime;
    }

    public String getCheckStatus() {
        return check_status;
    }

    public void setCheckStatus(String check_status) {
        this.check_status = check_status;
    }

    public String getCurrentCheckTime() {
        return current_checktime;
    }

    public void setCurrentCheckTime(String current_checktime) {
        this.current_checktime = current_checktime;
    }
}
