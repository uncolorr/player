package com.example.uncolor.vkmusic.auth_activity.music_fragment;

import android.content.Context;

import com.example.uncolor.vkmusic.Apis.Api;
import com.example.uncolor.vkmusic.Apis.ApiResponse;
import com.example.uncolor.vkmusic.Apis.request_bodies.GetMusicRequestBody;
import com.example.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.example.uncolor.vkmusic.Apis.response_models.MusicListResponseModel;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.utils.MessageReporter;
import com.example.uncolor.vkmusic.models.Music;

/**
 * Created by Uncolor on 04.09.2018.
 */

public class MusicFragmentPresenter implements MusicFragmentContract.Presenter, ApiResponse.ApiFailureListener {
    private Context context;
    private MusicFragmentContract.View view;

    public MusicFragmentPresenter(Context context, MusicFragmentContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void onFailure(int code, String message) {
        view.hideProgress();
    }

    @Override
    public void onLoadMusic(GetMusicRequestBody getMusicRequestBody, boolean isRefreshing) {
        view.showProgress();
        Api.getSource().getMusic(getMusicRequestBody.getQuery(), getMusicRequestBody.getPage())
                .enqueue(ApiResponse.getCallback(getMusicCallback(isRefreshing), this));
    }

    @Override
    public void onUploadTrack(BaseMusic music) {
        showMessageAboutAuth();
    }

    @Override
    public void onPlayTrack(BaseMusic music, int position) {

    }

    private void showMessageAboutAuth() {
        MessageReporter.showMessageAboutAuth(context);
    }

    private ApiResponse.ApiResponseListener<MusicListResponseModel> getMusicCallback(final boolean isRefreshing) {
        return new ApiResponse.ApiResponseListener<MusicListResponseModel>() {
            @Override
            public void onResponse(MusicListResponseModel result) {
                view.hideProgress();
                view.setMusicItems(result.getData(), isRefreshing);
            }
        };
    }
}
