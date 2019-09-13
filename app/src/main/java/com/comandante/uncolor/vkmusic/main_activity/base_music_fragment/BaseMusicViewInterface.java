package com.comandante.uncolor.vkmusic.main_activity.base_music_fragment;

import android.app.Activity;

import com.comandante.uncolor.vkmusic.models.BaseMusic;

public interface BaseMusicViewInterface {

     Activity getViewActivity();

     void startPlayingTrack(BaseMusic music);

     void deleteTrack(BaseMusic music);

     void startDownloading(BaseMusic music);

     void completeDownloading(BaseMusic music);

     void cancelDownloading(BaseMusic music);
}
