package com.example.uncolor.vkmusic.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Uncolor on 04.09.2018.
 */

public class VkMusic extends RealmObject implements BaseMusic{

    @SerializedName("artist")
    private String artist;

    @SerializedName("title")
    private String title;

    @SerializedName("duration")
    private int duration;

    @SerializedName("url")
    private String url;


    public VkMusic(){

    }


    protected VkMusic(Parcel in) {
        artist = in.readString();
        title = in.readString();
        duration = in.readInt();
        url = in.readString();
    }

    public static final Creator<VkMusic> CREATOR = new Creator<VkMusic>() {
        @Override
        public VkMusic createFromParcel(Parcel in) {
            return new VkMusic(in);
        }

        @Override
        public VkMusic[] newArray(int size) {
            return new VkMusic[size];
        }
    };

    @Override
    public String getArtist() {
        return artist;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public String getDownload() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeInt(duration);
        dest.writeString(url);
    }
}
