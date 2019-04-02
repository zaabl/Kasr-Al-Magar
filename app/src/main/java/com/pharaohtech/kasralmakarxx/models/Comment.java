package com.pharaohtech.kasralmakarxx.models;

public class Comment {

    private String profilePicture;
    private String name;
    private String commentText;
    private Long commentDate;
    private String Uid;

    public Comment(String profilePicture, String name, String commentText, Long commentDate, String Uid) {
        this.profilePicture = profilePicture;
        this.name = name;
        this.commentText = commentText;
        this.commentDate = commentDate;
        this.Uid = Uid;
    }

    public Comment() {

    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Long getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Long commentDate) {
        this.commentDate = commentDate;
    }

    public String getUid() { return Uid; }

    public void setUid(String uid) { Uid = uid; }

    @Override
    public String toString() {
        return "Comment{" +
                "profilePicture='" + profilePicture + '\'' +
                ", name='" + name + '\'' +
                ", commentText='" + commentText + '\'' +
                ", commentDate=" + commentDate +
                ", Uid='" + Uid + '\'' +
                '}';
    }
}
