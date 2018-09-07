package com.example.uncolor.vkmusic.auth_activity.auth_fragment;

import com.example.uncolor.vkmusic.auth_activity.music_fragment.BaseMusicFragmentPresenter;
import com.example.uncolor.vkmusic.models.Music;

import java.util.List;

/**
 * Created by Uncolor on 04.09.2018.
 */

public interface AuthFragmentContract {

    interface View{
        void showProcess();
        void hideProcess();
        void showErrorMessage();
        void login();
    }

    interface Presenter {
        void onSignInButtonClick(String login, String password);
        boolean isAuth();
    }
}
