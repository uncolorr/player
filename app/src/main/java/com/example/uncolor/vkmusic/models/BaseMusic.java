package com.example.uncolor.vkmusic.models;

import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Uncolor on 05.09.2018.
 */

public interface BaseMusic extends Parcelable{
     String getArtist();
     String getTitle();
     int getDuration();
     String getDownload();
}
