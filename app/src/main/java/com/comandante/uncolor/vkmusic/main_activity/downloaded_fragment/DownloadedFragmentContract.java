package com.comandante.uncolor.vkmusic.main_activity.downloaded_fragment;

import android.content.BroadcastReceiver;

import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicPresenterInterface;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicViewInterface;
import com.comandante.uncolor.vkmusic.models.BaseMusic;

import java.util.List;

public interface DownloadedFragmentContract {

    interface View extends BaseMusicViewInterface {
        void showProgressBar();
        void hideProgressBar();
        void deleteTrack(BaseMusic music);
        void addMusicItems(List<? extends BaseMusic> tracks, boolean isRefreshing);
        void selectCurrentTrack(BaseMusic music);
    }

    interface Presenter extends BaseMusicPresenterInterface {
        void onLoadMusicFromDatabase();
        BroadcastReceiver getMusicBroadcastReceiver();
    }
}
