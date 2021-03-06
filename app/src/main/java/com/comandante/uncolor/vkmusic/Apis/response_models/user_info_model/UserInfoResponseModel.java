package com.comandante.uncolor.vkmusic.Apis.response_models.user_info_model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Uncolor on 16.09.2018.
 */

public class UserInfoResponseModel {

    @Nullable
    @SerializedName("response")
    private List<UserInfo> response;

    @Nullable
    public List<UserInfo> getResponse() {
        return response;
    }
}
