package com.viwid.watt.watt.Model;

/**
 * Created by YOGI on 14-08-2018.
 */

public class ChoooseActivityModel {
    private String id;
    private String name;
    private String title;
    //private boolean isSelected=false;

    public ChoooseActivityModel(String id,String name,String title)
    {
        this.name = name;
        this.title = title;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /*public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }*/
}

