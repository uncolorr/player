package com.comandante.uncolor.vkmusic.main_activity.my_music_fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.comandante.uncolor.vkmusic.Apis.Api;
import com.comandante.uncolor.vkmusic.Apis.ApiResponse;
import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.comandante.uncolor.vkmusic.Apis.request_bodies.SearchVkMusicBody;
import com.comandante.uncolor.vkmusic.Apis.response_models.album_image_model.AlbumImageResponseModel;
import com.comandante.uncolor.vkmusic.Apis.response_models.album_image_model.ImageInfo;
import com.comandante.uncolor.vkmusic.Apis.response_models.VKMusicResponseModel;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.application.AppPermissionManager;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.models.VkMusic;
import com.comandante.uncolor.vkmusic.music_adapter.MusicAdapter;
import com.comandante.uncolor.vkmusic.services.download.DownloadService;

import com.comandante.uncolor.vkmusic.services.download.NewDownloadService;
import com.comandante.uncolor.vkmusic.services.music.NewMusicService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Uncolor on 26.08.2018.
 */

public class VkMusicFragmentPresenter implements VkMusicFragmentContract.Presenter,
        ApiResponse.ApiFailureListener {

    private Context context;
    private VkMusicFragmentContract.View view;
    private Realm realm;


    public VkMusicFragmentPresenter(Context context, VkMusicFragmentContract.View view) {
        this.context = context;
        this.view = view;
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onFailure(int code, String message) {
        view.hideProgress();
        view.removeLoadMoreProgress();
    }

    @Override
    public void onLoadMusic(GetVkMusicBody getVkMusicBody, boolean isRefreshing) {
        if (App.isAuth()) {
            view.showProgress();
            view.addLoadMoreProgress();
            Api.getSource().getVkMusic(App.getToken(),
                    getVkMusicBody.getV(),
                    getVkMusicBody.getOffset(),
                    getVkMusicBody.getCount())
                    .enqueue(ApiResponse.getCallback(getMusicCallback(isRefreshing),
                            this));
        }
    }

    @Override
    public void onSearchMusic(SearchVkMusicBody searchVkMusicBody, int mode, boolean withCaptcha, boolean isRefreshing) {
        App.Log("Search offset: " + searchVkMusicBody.getOffset());
        if (App.isAuth()) {
            view.showProgress();
            switch (mode){
                case MusicAdapter.MODE_CACHE:
                    RealmResults<VkMusic> results = realm.where(VkMusic.class)
                            .beginGroup()
                            .contains("artist", searchVkMusicBody.getQ().toString(), Case.INSENSITIVE)
                            .or()
                            .contains("title", searchVkMusicBody.getQ().toString(), Case.INSENSITIVE)
                            .endGroup()
                            .findAll();
                    view.setMusicItems(results, true);
                    view.hideProgress();
                    break;
                case MusicAdapter.MODE_ALL_MUSIC:
                    view.addLoadMoreProgress();
                    if (withCaptcha) {
                        Api.getSource().searchVkMusicWithCaptcha(
                                App.getToken(),
                                searchVkMusicBody.getQ(),
                                searchVkMusicBody.getV(),
                                searchVkMusicBody.getOffset(),
                                searchVkMusicBody.getCount(),
                                searchVkMusicBody.getCaptchaSid(),
                                searchVkMusicBody.getCaptchaKey())
                                .enqueue(ApiResponse.getCallback(getSearchMusicCallback(isRefreshing),
                                        this));
                    } else {
                        Api.getSource().searchVkMusic(
                                App.getToken(),
                                searchVkMusicBody.getQ(),
                                searchVkMusicBody.getV(),
                                searchVkMusicBody.getOffset(),
                                searchVkMusicBody.getCount())
                                .enqueue(ApiResponse.getCallback(getSearchMusicCallback(isRefreshing),
                                        this));
                    }
                    break;
            }
        }
    }

    @Override
    public void onFindAlbumImageUrl(BaseMusic music, int position) {
        Api.getSource().getAlbumImage(music.getArtist(), music.getTitle())
                .enqueue(ApiResponse.getCallback(getFindAlbumImageCallback(position), this));
    }

    private ApiResponse.ApiResponseListener<AlbumImageResponseModel> getFindAlbumImageCallback(final int position) {
        return new ApiResponse.ApiResponseListener<AlbumImageResponseModel>() {
            @Override
            public void onResponse(AlbumImageResponseModel result) throws IOException {
                if(result.getTrack() == null){
                    return;
                }
                if(result.getTrack().getAlbum() != null) {
                    List<ImageInfo> images = result.getTrack().getAlbum().getImages();
                    for (int i = 0; i < images.size(); i++) {
                        if (Objects.equals(images.get(i).getSize(), "extralarge")) {
                            view.setAlbumImageForMusic(images.get(i).getUrl(), position);
                        }
                    }
                }

            }
        };
    }

    private ApiResponse.ApiResponseListener<VKMusicResponseModel> getSearchMusicCallback(final boolean isRefreshing) {
        return new ApiResponse.ApiResponseListener<VKMusicResponseModel>() {
            @Override
            public void onResponse(VKMusicResponseModel result) throws IOException {
                if (result.getResponse() != null) {
                    App.Log("search not null");
                    view.setMusicItems(result.getResponse().getItems(), isRefreshing);
                } else if (result.getError() != null) {
                    view.showCaptchaDialog(result.getError(), isRefreshing);
                }
                view.hideProgress();
                view.removeLoadMoreProgress();
            }
        };
    }

    private ApiResponse.ApiResponseListener<VKMusicResponseModel> getMusicCallback(final boolean isRefreshing) {
        return new ApiResponse.ApiResponseListener<VKMusicResponseModel>() {
            @Override
            public void onResponse(VKMusicResponseModel result) throws IOException {
                view.hideProgress();
                view.removeLoadMoreProgress();
                view.setMusicItems(result.getResponse().getItems(), isRefreshing);
            }
        };
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
        Intent intent = new Intent(context, NewMusicService.class);
        intent.setAction(NewMusicService.ACTION_PLAY);
        intent.putExtra(NewMusicService.ARG_MUSIC, music);
        intent.putParcelableArrayListExtra(NewMusicService.ARG_PLAYLIST, view.getMusic());
        intent.putExtra(NewMusicService.ARG_POSITION, position);
        context.startService(intent);
    }

    @Override
    public void onDeleteTrack(BaseMusic music, int position) {
        view.deleteMusic((VkMusic) music, position);
    }

    private void startDownload(BaseMusic music) {
        App.Log("start Download");
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.ARG_MUSIC, music);
        context.startService(intent);
    }

}
