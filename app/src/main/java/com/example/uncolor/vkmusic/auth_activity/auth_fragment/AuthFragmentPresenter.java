package com.example.uncolor.vkmusic.auth_activity.auth_fragment;

import android.content.Context;

import com.example.uncolor.vkmusic.Apis.Api;
import com.example.uncolor.vkmusic.Apis.ApiResponse;
import com.example.uncolor.vkmusic.Apis.response_models.AuthResponseModel;
import com.example.uncolor.vkmusic.application.App;

/**
 * Created by Uncolor on 04.09.2018.
 */

public class AuthFragmentPresenter implements AuthFragmentContract.Presenter, ApiResponse.ApiFailureListener {

    private Context context;
    private AuthFragmentContract.View view;

    public AuthFragmentPresenter(Context context, AuthFragmentContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void onSignInButtonClick(String login, String password) {
        view.showProcess();
        Api.getSource().login(login, password).enqueue(ApiResponse.getCallback(getAuthCallback(),
                this));
    }

    @Override
    public boolean isAuth() {
        return App.isAuth();
    }

    private void rememberAuth(String token) {
        App.saveToken(token);
    }

    @Override
    public void onFailure(int code, String message) {
        view.hideProcess();
    }


    private ApiResponse.ApiResponseListener<AuthResponseModel> getAuthCallback() {
        return new ApiResponse.ApiResponseListener<AuthResponseModel>() {
            @Override
            public void onResponse(AuthResponseModel result) {
                view.hideProcess();
                if(result.getToken() == null){
                    view.showErrorMessage();
                }
                else {
                    rememberAuth(result.getToken());
                    view.login();
                }
            }
        };
    }
}
