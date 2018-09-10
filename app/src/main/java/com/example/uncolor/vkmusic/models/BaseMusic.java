package com.example.uncolor.vkmusic.models;

import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Uncolor on 05.09.2018.
 */

public interface BaseMusic extends Parcelable {

     static final int STATE_DEFAULT = 0;
     static final int STATE_DOWNLOADING = 1;
     static final int STATE_COMPLETED = 2;
     
     String getArtist();
     String getTitle();
     int getDuration();
     String getDownload();
     String getLocalPath();
     void setLocalPath(String localPath);
     int getState();
     void setState(int state);
}
