package com.comandante.uncolor.vkmusic.main_activity.main_music_fragment;

import android.content.BroadcastReceiver;

import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicPresenterInterface;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicViewInterface;
import com.comandante.uncolor.vkmusic.models.BaseMusic;

import java.util.List;

public interface MainMusicFragmentContract  {

    interface View extends BaseMusicViewInterface {
        void showProgressBar();
        void hideProgressBar();
        void addMusicItems(List<? extends BaseMusic> tracks, boolean isRefreshing);
        void addLoadMoreItem();
        void removeLoadMoreItem();
        void startPlayingTrack(BaseMusic music);
        void selectCurrentTrack(BaseMusic music);
    }

    interface Presenter extends BaseMusicPresenterInterface {
        void onLoadMusicByRequest(GetVkMusicBody getVkMusicBody, boolean isRefreshing);
        BroadcastReceiver getMusicBroadcastReceiver();
    }
}
