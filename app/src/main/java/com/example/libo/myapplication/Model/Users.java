package com.example.libo.myapplication.Model;

public class Users {

    private String email;
    private String username;
    private String uid;

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    private String photo;

    public Users(){

    }
    public Users(String email, String username, String uid) {
        this.email = email;
        this.username = username;
        this.uid = uid;

    }

    public Users(String email, String uid) {
        this.email = email;
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

