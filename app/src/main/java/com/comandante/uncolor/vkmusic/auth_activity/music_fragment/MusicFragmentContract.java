package com.comandante.uncolor.vkmusic.auth_activity.music_fragment;

import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetMusicRequestBody;
import com.comandante.uncolor.vkmusic.models.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Uncolor on 04.09.2018.
 */

public interface MusicFragmentContract {

    interface View{
        void showProgress();
        void hideProgress();
        ArrayList<Music> getMusic();
        void setAlbumImageForMusic(String url, int position);
        void setMusicItems(List<Music> items, boolean isRefreshing);
        void showErrorToast(String message);
    }

    interface Presenter extends BaseMusicFragmentPresenter {
        void onLoadMusic(GetMusicRequestBody requestBody, boolean isRefreshing);
    }
}
