package com.comandante.uncolor.vkmusic.main_activity;

import android.content.BroadcastReceiver;

import com.comandante.uncolor.vkmusic.models.BaseMusic;

public interface MainActivityContract {
    interface View {
        void showPlayerPanel();
        void hidePlayerPanel();
        void showPlayerBar();
        void hidePlayerBar();
        void setPauseButtons();
        void setPlayButtons();
        void setSongDescriptions(BaseMusic music);
        void setLoopingState(boolean isLooping);
        void setShufflingState(boolean isShuffling);
        void updateMusicPosition();
        void startProgressUpdate();
        void pauseProgressUpdate();
        void resumeProgressUpdate();
        void interruptProgressUpdate();
        void broadcastSwitchedTrack(String action, BaseMusic music);
        void checkServiceConnection();
    }

    interface Presenter {
        void onBindPlayerState(BaseMusic music, boolean isLooping, boolean isShuffling);
        BroadcastReceiver getTempMusicReceiver();
        void onSwitchTrack(String action, BaseMusic music);
        void onPauseTrack();
        void onResumeTrack();
    }
}














