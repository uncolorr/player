package com.example.uncolor.vkmusic;

import android.content.IntentFilter;

import com.example.uncolor.vkmusic.models.Music;
import com.example.uncolor.vkmusic.services.MusicService;
import com.example.uncolor.vkmusic.services.download.DownloadService;

/**
 * Created by Uncolor on 10.09.2018.
 */

public class IntentFilterManager {
    public static IntentFilter getMusicIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicService.ACTION_PLAY);
        intentFilter.addAction(MusicService.ACTION_NEXT);
        intentFilter.addAction(MusicService.ACTION_PREVIOUS);
        intentFilter.addAction(MusicService.ACTION_PAUSE_OR_RESUME);
        intentFilter.addAction(MusicService.ACTION_PLAYER_RESUME);
        intentFilter.addAction(MusicService.ACTION_BEGIN_PLAYING);
        intentFilter.addAction(MusicService.ACTION_CLOSE);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_FAILURE);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_COMPLETED);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_STARTED);
        return intentFilter;
    }
}
