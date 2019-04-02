package com.pharaohtech.kasralmakarxx.models;

public class User {
    private String user_id;
    private String email;
    private String display_name;
    private String profile_photo;
    private String user_type;

    public User(String user_id, String email, String display_name, String profile_photo, String user_type) {
        this.user_id = user_id;
        this.email = email;
        this.display_name = display_name;
        this.profile_photo = profile_photo;
        this.user_type = user_type;
    }
    public User() {

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", email='" + email + '\'' +
                ", display_name='" + display_name + '\'' +
                ", profile_photo='" + profile_photo + '\'' +
                ", user_type='" + user_type + '\'' +
                '}';
    }
}
