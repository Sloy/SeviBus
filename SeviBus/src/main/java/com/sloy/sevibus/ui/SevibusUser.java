package com.sloy.sevibus.ui;

public class SevibusUser {

    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private String oauthToken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    @Override
    public String toString() {
        return "SevibusUser{" +
          "id='" + id + '\'' +
          ", name='" + name + '\'' +
          ", email='" + email + '\'' +
          ", photoUrl='" + photoUrl + '\'' +
          ", oauthToken='" + oauthToken + '\'' +
          '}';
    }
}
