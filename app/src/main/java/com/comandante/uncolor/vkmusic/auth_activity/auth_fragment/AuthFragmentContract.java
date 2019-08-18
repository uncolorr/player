package com.comandante.uncolor.vkmusic.auth_activity.auth_fragment;

/**
 * Created by Uncolor on 04.09.2018.
 */

public interface AuthFragmentContract {

    interface View{
      /*  void showProcess();
        void hideProcess();
        void showErrorMessage();
        void login();*/

        void showToast(String message);
        void showLoadingDialog();
        void hideLoadingDialog();
        void showErrorMessage(String message);
        void showCaptchaDialog(String c_sid, String c_img);
        void signIn();
    }

    interface Presenter {
        void onSignInButtonClick(String login, String password);
        void onSignInWithCaptchaButtonClick(String login, String password, String cSid, String cKey);
        boolean isAuth();
    }
}
