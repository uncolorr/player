package com.comandante.uncolor.vkmusic.main_activity.my_music_fragment;

import android.app.Activity;

import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.comandante.uncolor.vkmusic.Apis.request_bodies.SearchVkMusicBody;
import com.comandante.uncolor.vkmusic.Apis.response_models.CaptchaErrorResponse;
import com.comandante.uncolor.vkmusic.auth_activity.music_fragment.BaseMusicFragmentPresenter;
import com.comandante.uncolor.vkmusic.models.VkMusic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Uncolor on 26.08.2018.
 */

public interface VkMusicFragmentContract {

    interface View{
        void showProgress();
        void hideProgress();
        void addLoadMoreProgress();
        void removeLoadMoreProgress();
        void showFailureMessage();
        void hideFailureMessage();
        void showReSignInDialog();
        void hideReSignInDialog();
        void setMusicItems(List<VkMusic> items, boolean isRefreshing);
        void deleteMusic(VkMusic music, int position);
        void showErrorToast(String message);
        void showCaptchaDialog(CaptchaErrorResponse captchaErrorResponse, boolean isRefreshing);
        void setAlbumImageForMusic(String url, int position);
        ArrayList<VkMusic> getMusic();
        Activity getViewActivity();
        void hideProcess();
        void showErrorMessage();
        void showProcess();
    }

    interface Presenter extends BaseMusicFragmentPresenter{
        void onLoadMusic(GetVkMusicBody requestBody, boolean isRefreshing);
        void onSearchMusic(SearchVkMusicBody requestBody, int mode, boolean withCaptcha, boolean isRefreshing);
        void onSignInButtonClick(String login, String password);

    }
}
