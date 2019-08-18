package com.comandante.uncolor.vkmusic.Apis.response_models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class AuthResponseModel {

    @Nullable
    @SerializedName("status")
    private String status;

    @Nullable
    @SerializedName("token")
    private String token;

    @Nullable
    @SerializedName("error")
    private String error;

    @Nullable
    @SerializedName("error_type")
    private String errorType;

    @Nullable
    @SerializedName("error_description")
    private String errorDescription;

    @Nullable
    @SerializedName("captcha_sid")
    private String captchaSid;

    @Nullable
    @SerializedName("captcha_img")
    private String captchaImg;

    @Nullable
    public String getError() {
        return error;
    }

    @Nullable
    public String getErrorType() {
        return errorType;
    }

    @Nullable
    public String getErrorDescription() {
        return errorDescription;
    }

    @Nullable
    public String getCaptchaSid() {
        return captchaSid;
    }

    @Nullable
    public String getCaptchaImg() {
        return captchaImg;
    }

    public String getStatus() {
        return status;
    }

    @Nullable
    public String getToken() {
        return token;
    }
}

