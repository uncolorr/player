package com.comandante.uncolor.vkmusic.Apis.response_models.album_image_model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Uncolor on 14.09.2018.
 */

public class AlbumImageResponseModel {

    @Nullable
    @SerializedName("track")
    private Track track;


    @Nullable
    public Track getTrack() {
        return track;
    }
}
