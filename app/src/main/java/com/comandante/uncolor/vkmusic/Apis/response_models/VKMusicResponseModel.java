package com.comandante.uncolor.vkmusic.Apis.response_models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Uncolor on 04.09.2018.
 */

public class VKMusicResponseModel {

    @Nullable
    @SerializedName("response")
    private VkResponse response;

    @Nullable
    @SerializedName("error")
    private CaptchaErrorResponse error;

    public VkResponse getResponse() {
        return response;
    }

    @Nullable
    public CaptchaErrorResponse getError() {
        return error;
    }
}
