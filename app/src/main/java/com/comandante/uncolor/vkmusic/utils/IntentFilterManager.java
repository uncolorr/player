package com.comandante.uncolor.vkmusic.utils;

import android.content.IntentFilter;

import com.comandante.uncolor.vkmusic.main_activity.settings_fragment.SettingsFragment;
import com.comandante.uncolor.vkmusic.services.download.DownloadService;
import com.comandante.uncolor.vkmusic.services.music.NewMusicService;

/**
 * Created by Uncolor on 10.09.2018.
 */

public class IntentFilterManager {
    public static IntentFilter getMusicIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NewMusicService.ACTION_PLAY);
        intentFilter.addAction(NewMusicService.ACTION_NEXT);
        intentFilter.addAction(NewMusicService.ACTION_PREVIOUS);
        intentFilter.addAction(NewMusicService.ACTION_PAUSE_OR_RESUME);
        //intentFilter.addAction(MusicService.ACTION_PLAYER_RESUME);
        intentFilter.addAction(NewMusicService.ACTION_BEGIN_PLAYING);
        intentFilter.addAction(NewMusicService.ACTION_CLOSE);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_FAILURE);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_COMPLETED);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_STARTED);
        intentFilter.addAction(SettingsFragment.ACTION_CLEAR_CACHE);
        return intentFilter;
    }
}
