package com.comandante.uncolor.vkmusic.main_activity.main_music_fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.comandante.uncolor.vkmusic.Apis.Api;
import com.comandante.uncolor.vkmusic.Apis.ApiResponse;
import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.comandante.uncolor.vkmusic.Apis.response_models.VKMusicResponseModel;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.application.AppPermissionManager;
import com.comandante.uncolor.vkmusic.application.AppSettings;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicPresenter;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.models.VkMusic;
import com.comandante.uncolor.vkmusic.services.download.DownloadService;
import com.comandante.uncolor.vkmusic.services.music.MusicService;

import java.util.List;

public class MainMusicFragmentPresenter extends BaseMusicPresenter implements MainMusicFragmentContract.Presenter,
        ApiResponse.ApiFailureListener {

    private Context context;
    private MainMusicFragmentContract.View view;

    public MainMusicFragmentPresenter(Context context, MainMusicFragmentContract.View view) {
        super(context, view);
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

    private void startDownload(BaseMusic music) {
        App.Log("start Download");
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.ARG_MUSIC, music);
        context.startService(intent);
    }

    @Override
    public void onPlayTrack(BaseMusic music, int position) {
        view.startPlayingTrack(music);
    }

    @Override
    public void onDeleteTrack(BaseMusic music, int position) {
        view.deleteTrack(music);
    }

    @Override
    public void onFindAlbumImageUrl(BaseMusic music, int position) {

    }

    @Override
    public void onLoadMusicByRequest(GetVkMusicBody getVkMusicBody, boolean isRefreshing) {
        if(!isRefreshing){
            view.addLoadMoreItem();
        }
        else {
            view.showProgressBar();
        }
        Api.getSource().getVkMusic(AppSettings.getToken(),
                getVkMusicBody.getV(),
                getVkMusicBody.getOffset(),
                getVkMusicBody.getCount()).enqueue(ApiResponse
                .getCallback(getVkMusicResponseListener(isRefreshing), this));
    }

    @Override
    public BroadcastReceiver getMusicBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent == null){
                    return;
                }

                String action = intent.getAction();
                if(action == null || action.isEmpty()){
                    return;
                }

                BaseMusic music;

                switch (action){
                    case MusicService.ACTION_PLAY:
                        music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        if(music == null){
                            return;
                        }
                        view.selectCurrentTrack(music);
                        break;

                    case DownloadService.ACTION_DOWNLOAD_STARTED:
                        music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        view.startDownloading(music);
                        break;
                    case DownloadService.ACTION_DOWNLOAD_COMPLETED:
                        music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        view.completeDownloading(music);
                        break;
                    case DownloadService.ACTION_DOWNLOAD_FAILURE:
                        music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        view.cancelDownloading(music);
                        break;
                }
            }
        };
    }

    private ApiResponse.ApiResponseListener<VKMusicResponseModel> getVkMusicResponseListener(boolean isRefreshing) {
        return result -> {
            view.hideProgressBar();
            view.removeLoadMoreItem();
            if(result == null){
                onFailure(500, context.getString(R.string.err_unknown_err));
                return;
            }

            if(result.getError() != null){
                onFailure(500, context.getString(R.string.err_unknown_err));
                return;
            }

            if(result.getResponse() == null){
                onFailure(500, context.getString(R.string.err_unknown_err));
                return;
            }

            List<VkMusic> tracks = result.getResponse().getItems();
            view.addMusicItems(tracks, isRefreshing);
        };
    }


    @Override
    public void onFailure(int code, String message) {
        view.hideProgressBar();
        view.removeLoadMoreItem();
    }
}
