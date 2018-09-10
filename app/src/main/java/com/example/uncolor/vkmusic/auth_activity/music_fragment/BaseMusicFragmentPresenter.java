package com.example.uncolor.vkmusic.auth_activity.music_fragment;

import com.example.uncolor.vkmusic.Apis.request_bodies.GetMusicRequestBody;
import com.example.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.models.Music;

/**
 * Created by Uncolor on 04.09.2018.
 */

public interface BaseMusicFragmentPresenter {

    void onUploadTrack(BaseMusic music);
    void onPlayTrack(BaseMusic music, int position);
    void onDeleteTrack(BaseMusic music, int position);

}
