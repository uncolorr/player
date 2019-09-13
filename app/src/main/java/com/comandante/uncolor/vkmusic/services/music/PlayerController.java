package com.comandante.uncolor.vkmusic.services.music;

import com.comandante.uncolor.vkmusic.models.BaseMusic;

public interface PlayerController {
    BaseMusic next(boolean fromUser);
    BaseMusic previous();
    void pause();
    void resume();
    void setVolume(float volume);
}
