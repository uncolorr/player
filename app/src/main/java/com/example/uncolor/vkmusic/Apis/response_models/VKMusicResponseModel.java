package com.example.uncolor.vkmusic.Apis.response_models;

import com.example.uncolor.vkmusic.models.Music;
import com.example.uncolor.vkmusic.models.VkMusic;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Uncolor on 04.09.2018.
 */

public class VKMusicResponseModel {

    @SerializedName("response")
    private VkResponse response;

    public VkResponse getResponse() {
        return response;
    }
}
