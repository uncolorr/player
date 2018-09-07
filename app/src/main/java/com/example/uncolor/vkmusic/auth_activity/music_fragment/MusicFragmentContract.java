package com.example.uncolor.vkmusic.auth_activity.music_fragment;

import com.example.uncolor.vkmusic.Apis.request_bodies.GetMusicRequestBody;
import com.example.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.example.uncolor.vkmusic.models.Music;

import java.util.List;

/**
 * Created by Uncolor on 04.09.2018.
 */

public interface MusicFragmentContract {

    interface View{
        void showProgress();
        void hideProgress();
        void setMusicItems(List<Music> items, boolean isRefreshing);
    }

    interface Presenter extends BaseMusicFragmentPresenter {
        void onLoadMusic(GetMusicRequestBody requestBody, boolean isRefreshing);
    }
}
