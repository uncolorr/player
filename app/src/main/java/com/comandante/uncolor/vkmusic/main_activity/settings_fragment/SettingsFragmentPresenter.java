package com.comandante.uncolor.vkmusic.main_activity.settings_fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.comandante.uncolor.vkmusic.Apis.Api;
import com.comandante.uncolor.vkmusic.Apis.ApiResponse;
import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetUserInfoRequestBody;
import com.comandante.uncolor.vkmusic.Apis.response_models.user_info_model.UserInfo;
import com.comandante.uncolor.vkmusic.Apis.response_models.user_info_model.UserInfoResponseModel;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.application.AppSettings;
import com.comandante.uncolor.vkmusic.models.VkMusic;
import com.comandante.uncolor.vkmusic.utils.DialogManager;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsFragmentPresenter implements SettingsFragmentContract.Presenter, ApiResponse.ApiFailureListener {

    private Context context;
    private SettingsFragmentContract.View view;
    private Realm realm;

    public SettingsFragmentPresenter(Context context, SettingsFragmentContract.View view) {
        this.context = context;
        this.view = view;
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onLoadUserInfo() {
        GetUserInfoRequestBody requestBody = new GetUserInfoRequestBody();
        Api.getSource().getUserInfo(AppSettings.getToken(), requestBody.getFields(), requestBody.getV())
                .enqueue(ApiResponse.getCallback(getUserInfoCallback(), this));
    }

    private ApiResponse.ApiResponseListener<UserInfoResponseModel> getUserInfoCallback() {
        return new ApiResponse.ApiResponseListener<UserInfoResponseModel>() {
            @Override
            public void onResponse(UserInfoResponseModel result) throws IOException {
                if(result.getResponse() != null){
                    UserInfo userInfo = result.getResponse().get(0);
                    String fullName = userInfo.getFirstName() + " " + userInfo.getLastName();
                    view.showUserInfo(fullName, userInfo.getPhotoUrl());
                }
                else {
                    view.showToast("Ошибка при загрузке информации о пользователе");
                }
            }
        };
    }

    @Override
    public void onClearCache() {
        clearCache(false);
    }

    private void clearCache(boolean isLogOut){
        realm.beginTransaction();
        RealmResults<VkMusic> results = realm.where(VkMusic.class).findAll();
        App.Log("results count: " + results.size());
        for (int i = 0; i < results.size(); i++) {
            File file = new File(results.get(i).getLocalPath());
            if (file.exists()) {
                file.delete();
                App.Log("file " + i + " deleted");
            }
            App.Log("item " + i + " deleted from realm");
        }
        results.deleteAllFromRealm();
        realm.commitTransaction();
        if(!isLogOut) {
            view.showToast(context.getString(R.string.msg_clear_cache_success));
            context.sendBroadcast(new Intent(SettingsFragment.ACTION_CLEAR_CACHE));
        }
    }

    @Override
    public void showClearCacheDialog() {
        DialogManager.showDialog(context, context.getString(R.string.msg_clear_cache_question),
                getClearCacheAgreeListener());
    }

    @Override
    public void showExitDialog() {
        DialogManager.showDialog(context, context.getString(R.string.msg_exit_question),
                getExitAgreeListener());
    }

    private DialogInterface.OnClickListener getExitAgreeListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearCache(true);
                view.logOut();
            }
        };
    }

    private DialogInterface.OnClickListener getClearCacheAgreeListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onClearCache();
            }
        };
    }


    @Override
    public void onFailure(int code, String message) {
        view.showToast("Ошибка при загрузке информации о пользователе");
    }
}
