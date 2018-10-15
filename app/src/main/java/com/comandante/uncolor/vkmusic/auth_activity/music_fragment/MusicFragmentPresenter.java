package com.comandante.uncolor.vkmusic.auth_activity.music_fragment;

import android.content.Context;
import android.content.Intent;

import com.comandante.uncolor.vkmusic.Apis.Api;
import com.comandante.uncolor.vkmusic.Apis.ApiResponse;
import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetMusicRequestBody;
import com.comandante.uncolor.vkmusic.Apis.response_models.album_image_model.AlbumImageResponseModel;
import com.comandante.uncolor.vkmusic.Apis.response_models.album_image_model.ImageInfo;
import com.comandante.uncolor.vkmusic.Apis.response_models.MusicListResponseModel;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.services.music.MusicService;
import com.comandante.uncolor.vkmusic.utils.MessageReporter;
import com.flurry.android.FlurryAgent;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
        Intent intent = new Intent(context, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putExtra(MusicService.ARG_MUSIC, music);
        intent.putParcelableArrayListExtra(MusicService.ARG_PLAYLIST, view.getMusic());
        intent.putExtra(MusicService.ARG_POSITION, position);
        context.startService(intent);
    }

    @Override
    public void onDeleteTrack(BaseMusic music, int position) {

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
    private void showMessageAboutAuth() {
        FlurryAgent.logEvent(context.getString(R.string.log_download_before_auth));
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
