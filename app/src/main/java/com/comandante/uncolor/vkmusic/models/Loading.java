package com.comandante.uncolor.vkmusic.models;

import com.comandante.uncolor.vkmusic.base_adapter.ItemModel;

public class Loading implements ItemModel {

    public static final int TYPE = 2;

    @Override
    public int getType() {
        return TYPE;
    }
}
