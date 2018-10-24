package com.comandante.uncolor.vkmusic.Apis.response_models;

import android.support.annotation.Nullable;

import com.comandante.uncolor.vkmusic.models.Music;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Uncolor on 25.08.2018.
 */

public class MusicListResponseModel {

    @Nullable
    @SerializedName("status")
    private String status;

    @Nullable
    @SerializedName("data")
    private List<Music> data;

    public String getStatus() {
        return status;
    }

    public List<Music> getData() {
        return data;
    }
}
