package com.pharaohtech.kasralmakarxx.models;

import java.util.Objects;

public class Post {
    public Post(String displayName, String profilePhoto, String thumbnail, String caption, String uid, int commentsCount, int likeCount, Long timestamp) {
        this.displayName = displayName;
        this.profilePhoto = profilePhoto;
        this.thumbnail = thumbnail;
        this.caption = caption;
        this.uid = uid;
        this.commentsCount = commentsCount;
        this.likeCount = likeCount;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return commentsCount == post.commentsCount &&
                likeCount == post.likeCount &&
                Objects.equals(displayName, post.displayName) &&
                Objects.equals(profilePhoto, post.profilePhoto) &&
                Objects.equals(thumbnail, post.thumbnail) &&
                Objects.equals(caption, post.caption) &&
                Objects.equals(uid, post.uid) &&
                Objects.equals(timestamp, post.timestamp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(displayName, profilePhoto, thumbnail, caption, uid, commentsCount, likeCount, timestamp);
    }

    @Override
    public String toString() {
        return "Post{" +
                "displayName='" + displayName + '\'' +
                ", profilePhoto='" + profilePhoto + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", caption='" + caption + '\'' +
                ", uid='" + uid + '\'' +
                ", commentsCount=" + commentsCount +
                ", likeCount=" + likeCount +
                ", timestamp=" + timestamp +
                '}';
    }

    public Post() {

    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    private String displayName;
    private String profilePhoto;
    private String thumbnail;
    private String caption;
    private String uid;
    private int commentsCount;
    private int likeCount;
    private Long timestamp;
}
