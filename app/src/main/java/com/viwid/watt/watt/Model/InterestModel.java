package com.viwid.watt.watt.Model;

/**
 * Created by YOGI on 13-09-2018.
 */

/*
Model Class for getting the Data for Interest for ChooserActivity
*/

public class InterestModel {
    private String created_userid;
    private String description;
    private String photoURL;
    private String title;
    private String id;


    public InterestModel()
    {

    }

    public InterestModel(String created_userid, String description, String photoURL, String title,String id) {
        this.created_userid = created_userid;
        this.description = description;
        this.photoURL = photoURL;
        this.title = title;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated_userid() {
        return created_userid;
    }

    public void setCreated_userid(String created_userid) {
        this.created_userid = created_userid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
