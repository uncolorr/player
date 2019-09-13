package com.comandante.uncolor.vkmusic.main_activity.base_music_fragment;

import com.comandante.uncolor.vkmusic.models.BaseMusic;

/**
 * Created by Uncolor on 04.09.2018.
 */

public interface BaseMusicPresenterInterface {

    void onUploadTrack(BaseMusic music);
    void onPlayTrack(BaseMusic music, int position);
    void onDeleteTrack(BaseMusic music, int position);
    void onFindAlbumImageUrl(BaseMusic music, int position);

}
