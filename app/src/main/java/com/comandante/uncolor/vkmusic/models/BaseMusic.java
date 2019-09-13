package com.comandante.uncolor.vkmusic.models;

import android.os.Parcelable;

import com.comandante.uncolor.vkmusic.base_adapter.ItemModel;

/**
 * Created by Uncolor on 05.09.2018.
 */

public interface BaseMusic extends Parcelable, ItemModel{

     static final int STATE_DEFAULT = 0;
     static final int STATE_DOWNLOADING = 1;
     static final int STATE_COMPLETED = 2;


     long getId();
     String getArtist();
     String getTitle();
     int getDuration();
     String getDownload();
     String getLocalPath();
     void setLocalPath(String localPath);
     int getState();
     void setState(int state);
     String getAlbumImageUrl();
     void setAlbumImageUrl(String url);
}
