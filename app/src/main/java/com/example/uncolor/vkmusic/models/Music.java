package com.example.uncolor.vkmusic.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by Uncolor on 24.08.2018.
 */

public class Music extends RealmObject implements BaseMusic{

    @SerializedName("source_id")
    private String sourceId;

    @SerializedName("artist")
    private String artist;

    @SerializedName("title")
    private String title;

    @SerializedName("duration")
    private int duration;

    @SerializedName("date")
    private int date;

    @SerializedName("genre_id")
    private int genreId;

    @SerializedName("download")
    private String download;

    @SerializedName("stream")
    private String stream;

    public Music(){

    }

    protected Music(Parcel in) {
        sourceId = in.readString();
        artist = in.readString();
        title = in.readString();
        duration = in.readInt();
        date = in.readInt();
        genreId = in.readInt();
        download = in.readString();
        stream = in.readString();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
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
        return download;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sourceId);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeInt(duration);
        dest.writeInt(date);
        dest.writeInt(genreId);
        dest.writeString(download);
        dest.writeString(stream);
    }
}
