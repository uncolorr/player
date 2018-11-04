package com.comandante.uncolor.vkmusic.Apis.response_models;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Uncolor on 13.09.2018.
 */

public class CaptchaErrorResponse implements Serializable{

    @SerializedName("error_code")
    private int errorCode;

    @Nullable
    @SerializedName("error_msg")
    private String errorMessage;

    @Nullable
    @SerializedName("captcha_sid")
    private String captchaSID;

    @Nullable
    @SerializedName("captcha_img")
    private String captchaImage;

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getCaptchaSID() {
        return captchaSID;
    }

    public String getCaptchaImage() {
        return captchaImage;
    }
}
