package com.comandante.uncolor.vkmusic.main_activity.search_music_fragment;

import android.content.BroadcastReceiver;

import com.comandante.uncolor.vkmusic.Apis.request_bodies.SearchVkMusicBody;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicPresenterInterface;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicViewInterface;
import com.comandante.uncolor.vkmusic.models.BaseMusic;

import java.util.List;

public interface SearchMusicContract {

    interface View extends BaseMusicViewInterface {
        void showProgressBar();
        void hideProgressBar();
        void addMusicItems(List<? extends BaseMusic> tracks, boolean isRefreshing);
        void startPlayingTrack(BaseMusic music);
        void selectCurrentTrack(BaseMusic music);
        void removeLoadMoreItem();
        void addLoadMoreItem();
    }

    interface Presenter extends BaseMusicPresenterInterface {
        BroadcastReceiver getBroadcastReceiver();
        void onSearchMusic(SearchVkMusicBody searchVkMusicBody, boolean isRefreshing);
    }
}
