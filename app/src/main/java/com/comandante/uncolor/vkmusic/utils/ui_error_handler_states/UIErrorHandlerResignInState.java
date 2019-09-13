package com.comandante.uncolor.vkmusic.utils.ui_error_handler_states;

import com.comandante.uncolor.vkmusic.utils.UIErrorType;

public class UIErrorHandlerResignInState implements BaseUIErrorHandlerState {
    @Override
    public UIErrorType getType() {
        return UIErrorType.RESIGN_IN;
    }

    @Override
    public String getTitle() {
        return "Текущая сессия завершена";
    }

    @Override
    public String getSubTitle() {
        return "Переавторизуйтесь для использования поиска. Скачанная музыка остается доступной для прослушивания";
    }

    @Override
    public String getButtonTitle() {
        return "Переавторизоваться";
    }

    @Override
    public int getImageResourceId() {
        return 0;
    }
}
