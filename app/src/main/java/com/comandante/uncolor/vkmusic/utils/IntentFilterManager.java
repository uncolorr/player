package com.comandante.uncolor.vkmusic.utils;

import android.content.IntentFilter;

import com.comandante.uncolor.vkmusic.services.download.DownloadService;
import com.comandante.uncolor.vkmusic.services.music.MusicService;

/**
 * Created by Uncolor on 10.09.2018.
 */

public class IntentFilterManager {
    public static IntentFilter getTempMusicIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_PLAY);
        intentFilter.addAction(MusicService.ACTION_PAUSE);
        intentFilter.addAction(MusicService.ACTION_RESUME);
        intentFilter.addAction(MusicService.ACTION_CLOSE);
        intentFilter.addAction(MusicService.ACTION_BEGIN_PLAYING);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_STARTED);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_COMPLETED);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_FAILURE);
        return intentFilter;
    }
}
