package com.comandante.uncolor.vkmusic.auth_activity.auth_fragment;

import android.content.Context;

import com.comandante.uncolor.vkmusic.Apis.Api;
import com.comandante.uncolor.vkmusic.Apis.ApiResponse;
import com.comandante.uncolor.vkmusic.Apis.response_models.ResponseStatus;
import com.comandante.uncolor.vkmusic.Apis.response_models.AuthResponseModel;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.AppSettings;

public class AuthFragmentPresenter implements AuthFragmentContract.Presenter, ApiResponse.ApiFailureListener {

    private Context context;
    private AuthFragmentContract.View view;

    public AuthFragmentPresenter(Context context, AuthFragmentContract.View view) {
        this.context = context;
        this.view = view;
    }


    @Override
    public void onSignInButtonClick(String login, String password) {
        view.showLoadingDialog();
        Api.getSource().login(login, password)
                .enqueue(ApiResponse.getCallback(getLoginResponseListener(), this));
    }

    @Override
    public boolean isAuth() {
        return AppSettings.isAuth();
    }

    private ApiResponse.ApiResponseListener<AuthResponseModel> getLoginResponseListener() {
        return result -> {
            view.hideLoadingDialog();
            if(result == null){
                onFailure(500, context.getString(R.string.err_unknown_err));
                return;
            }

            processErrorIfNeeded(result);

            if(result.getToken() == null) {
                onFailure(500, context.getString(R.string.err_unknown_err));
                return;
            }

            String token = result.getToken();
            AppSettings.signIn(token);
            view.signIn();
        };
    }

    private void processErrorIfNeeded(AuthResponseModel result){
        if(result.getError() == null){
            return;
        }
        String error = result.getError();
        switch (error){
            case ResponseStatus.ERROR_INVALID_CLIENT:
                view.showErrorMessage(context.getString(R.string.err_incorrect_login_or_pass));
                break;
            case ResponseStatus.ERROR_NEED_CAPTCHA:
                view.showCaptchaDialog(result.getCaptchaSid(), result.getCaptchaImg());
                break;
        }
    }

    @Override
    public void onSignInWithCaptchaButtonClick(String login, String password, String cSid, String cKey) {
        view.showLoadingDialog();
        Api.getSource().loginWithCaptcha(login, password, cSid, cKey)
                .enqueue(ApiResponse.getCallback(getLoginResponseListener(), this));
    }

    @Override
    public void onFailure(int code, String message) {
        view.hideLoadingDialog();
        view.showErrorMessage(message);
    }
}
