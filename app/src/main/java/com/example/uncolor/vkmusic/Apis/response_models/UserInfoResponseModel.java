package com.example.uncolor.vkmusic.Apis.response_models;

import android.support.annotation.Nullable;

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
