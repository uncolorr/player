package com.example.uncolor.vkmusic.main_activity.my_music_fragment;

import android.app.Activity;

import com.example.uncolor.vkmusic.Apis.request_bodies.GetMusicRequestBody;
import com.example.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.example.uncolor.vkmusic.auth_activity.music_fragment.BaseMusicFragmentPresenter;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.models.Music;
import com.example.uncolor.vkmusic.models.VkMusic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Uncolor on 26.08.2018.
 */

public interface VkMusicFragmentContract {

    interface View{
        void showProgress();
        void hideProgress();
        void setMusicItems(List<VkMusic> items, boolean isRefreshing);
        void showErrorToast(String message);
        ArrayList<VkMusic> getMusic();
        Activity getViewActivity();
    }

    interface Presenter extends BaseMusicFragmentPresenter{
        void onLoadMusic(GetVkMusicBody requestBody, boolean isRefreshing);
    }
}
