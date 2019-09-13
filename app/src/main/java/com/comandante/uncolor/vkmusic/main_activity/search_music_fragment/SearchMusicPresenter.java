package com.comandante.uncolor.vkmusic.main_activity.search_music_fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.comandante.uncolor.vkmusic.Apis.Api;
import com.comandante.uncolor.vkmusic.Apis.ApiResponse;
import com.comandante.uncolor.vkmusic.Apis.request_bodies.SearchVkMusicBody;
import com.comandante.uncolor.vkmusic.Apis.response_models.VKMusicResponseModel;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.application.AppSettings;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicPresenter;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.services.download.DownloadService;
import com.comandante.uncolor.vkmusic.services.music.MusicService;

public class SearchMusicPresenter extends BaseMusicPresenter
        implements SearchMusicContract.Presenter, ApiResponse.ApiFailureListener {

    private Context context;
    private SearchMusicContract.View view;

    public SearchMusicPresenter(Context context, SearchMusicContract.View view) {
        super(context, view);
        this.context = context;
        this.view = view;
    }

    @Override
    public BroadcastReceiver getBroadcastReceiver() {
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

                switch (action){
                    case MusicService.ACTION_PLAY:
                        BaseMusic music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        if(music == null){
                            return;
                        }
                        view.selectCurrentTrack(music);
                        break;
                    case MusicService.ACTION_BEGIN_PLAYING:
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
                }
            }
        };
    }

    @Override
    public void onSearchMusic(SearchVkMusicBody searchVkMusicBody, boolean isRefreshing) {
        if(isRefreshing){
            view.showProgressBar();
        }
        else {
            view.addLoadMoreItem();
        }

        Api.getSource().searchVkMusic(
                AppSettings.getToken(),
                searchVkMusicBody.getQ(),
                searchVkMusicBody.getV(),
                searchVkMusicBody.getOffset(),
                searchVkMusicBody.getCount())
                .enqueue(ApiResponse.getCallback(getSearchMusicCallback(isRefreshing), this));
    }

    private ApiResponse.ApiResponseListener<VKMusicResponseModel> getSearchMusicCallback(final boolean isRefreshing) {
        return result -> {
            if(isRefreshing){
                view.hideProgressBar();
            }
            else {
                view.removeLoadMoreItem();
            }
            if (result.getResponse() != null) {
                App.Log("search not null");
                view.addMusicItems(result.getResponse().getItems(), isRefreshing);
            } else if (result.getError() != null) {
                switch (result.getError().getErrorCode()) {
                    case 5:
                        //view.showFailureMessage();
                        break;
                    case 14:
                       // view.showCaptchaDialog(result.getError(), isRefreshing);
                }
            }
        };
    }

    @Override
    public void onFailure(int code, String message) {

    }
}
