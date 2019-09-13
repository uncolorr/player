package com.comandante.uncolor.vkmusic.main_activity.base_music_fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.application.AppPermissionManager;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.services.download.DownloadService;

public abstract class BaseMusicPresenter implements BaseMusicPresenterInterface {

    private Context context;
    private BaseMusicViewInterface view;

    public BaseMusicPresenter(Context context, BaseMusicViewInterface view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void onUploadTrack(BaseMusic music) {
        if (!AppPermissionManager.checkIfAlreadyHavePermission(view.getViewActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            AppPermissionManager.requestAppPermissions(view.getViewActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    AppPermissionManager.PERMISSION_REQUEST_CODE);
        } else {
            startDownload(music);
        }
    }

    @Override
    public void onPlayTrack(BaseMusic music, int position) {
        view.startPlayingTrack(music);
    }

    @Override
    public void onDeleteTrack(BaseMusic music, int position) {
        App.Log("BaseMusicPresenter onDeleteTrack");
        view.deleteTrack(music);
    }

    private void startDownload(BaseMusic music) {
        App.Log("start Download");
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.ARG_MUSIC, music);
        context.startService(intent);
    }

    @Override
    public void onFindAlbumImageUrl(BaseMusic music, int position) {

    }
}


