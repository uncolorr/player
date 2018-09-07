package com.example.uncolor.vkmusic.Apis.response_models;

import com.example.uncolor.vkmusic.models.Music;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Uncolor on 25.08.2018.
 */

public class MusicListResponseModel {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private List<Music> data;

    public String getStatus() {
        return status;
    }

    public List<Music> getData() {
        return data;
    }
}
