package com.example.uncolor.vkmusic.Apis.response_models;

import com.example.uncolor.vkmusic.models.VkMusic;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Uncolor on 05.09.2018.
 */

public class VkResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("items")
    private List<VkMusic> items;

    public int getCount() {
        return count;
    }

    public List<VkMusic> getItems() {
        return items;
    }
}
