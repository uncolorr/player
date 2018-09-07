package com.example.uncolor.vkmusic.main_activity.my_music_fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.uncolor.vkmusic.Apis.Api;
import com.example.uncolor.vkmusic.Apis.ApiResponse;
import com.example.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.example.uncolor.vkmusic.Apis.response_models.VKMusicResponseModel;
import com.example.uncolor.vkmusic.application.App;
import com.example.uncolor.vkmusic.application.AppPermissionManager;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.services.MusicService;
import com.example.uncolor.vkmusic.services.download.DownloadService;

import java.io.IOException;

/**
 * Created by Uncolor on 26.08.2018.
 */

public class VkMusicFragmentPresenter implements VkMusicFragmentContract.Presenter,
        ApiResponse.ApiFailureListener {

    private Context context;
    private VkMusicFragmentContract.View view;

    public VkMusicFragmentPresenter(Context context, VkMusicFragmentContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void onFailure(int code, String message) {
        view.hideProgress();
        view.showErrorToast(message);
    }

    @Override
    public void onLoadMusic(GetVkMusicBody getVkMusicBody, boolean isRefreshing) {
        if(App.isAuth()) {
            view.showProgress();
            Api.getSource().getVkMusic(App.getToken(), getVkMusicBody.getV(), getVkMusicBody.getOffset(), getVkMusicBody.getCount())
                    .enqueue(ApiResponse.getCallback(getMusicCallback(isRefreshing), this));
        }
    }

    private ApiResponse.ApiResponseListener<VKMusicResponseModel> getMusicCallback(final boolean isRefreshing) {
        return new ApiResponse.ApiResponseListener<VKMusicResponseModel>() {
            @Override
            public void onResponse(VKMusicResponseModel result) throws IOException {
                view.hideProgress();
                view.setMusicItems(result.getResponse().getItems(), isRefreshing);
            }
        };
    }

    @Override
    public void onUploadTrack(BaseMusic music) {
        if(!AppPermissionManager.checkIfAlreadyhavePermission(view.getViewActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AppPermissionManager.requestAppPermissions(view.getViewActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    AppPermissionManager.PERMISSION_REQUEST_CODE);
        }
        else {
            if(music == null){
                App.Log("null");
            }
            else {
                App.Log("not null");
            }
            startDownload(music);
        }

    }

    @Override
    public void onPlayTrack(BaseMusic music, int position) {
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putExtra(MusicService.ARG_MUSIC, music);
        intent.putParcelableArrayListExtra(MusicService.ARG_PLAYLIST, view.getMusic());
        intent.putExtra(MusicService.ARG_POSITION, position);
        context.startService(intent);
    }

    private void startDownload(BaseMusic music) {
        App.Log("start Download");
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("music", music);
        context.startService(intent);
    }

}
