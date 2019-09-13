package com.comandante.uncolor.vkmusic.utils.ui_error_handler_states;

import com.comandante.uncolor.vkmusic.utils.UIErrorType;

public interface BaseUIErrorHandlerState {
    UIErrorType getType();

    String getTitle();

    String getSubTitle();

    String getButtonTitle();

    int getImageResourceId();
}
