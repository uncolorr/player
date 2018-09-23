package com.comandante.uncolor.vkmusic.auth_activity.auth_fragment;

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
