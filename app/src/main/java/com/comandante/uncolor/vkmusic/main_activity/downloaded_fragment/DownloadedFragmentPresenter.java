package com.comandante.uncolor.vkmusic.main_activity.downloaded_fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.comandante.uncolor.vkmusic.database.DatabaseManager;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicPresenter;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.services.download.DownloadService;
import com.comandante.uncolor.vkmusic.services.music.MusicService;

public class DownloadedFragmentPresenter extends BaseMusicPresenter implements DownloadedFragmentContract.Presenter {

    private Context context;
    private DownloadedFragmentContract.View view;

    public DownloadedFragmentPresenter(Context context, DownloadedFragmentContract.View view) {
        super(context, view);
        this.context = context;
        this.view = view;
    }

    @Override
    public void onLoadMusicFromDatabase() {
        view.showProgressBar();
        view.addMusicItems(DatabaseManager.get().findAll(), true);
        view.hideProgressBar();
    }

    @Override
    public BroadcastReceiver getMusicBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent == null){
                    return;
                }

                String action = intent.getAction();
                if(action == null || action.isEmpty()){
                    return;
                }

                BaseMusic music;

                switch (action){
                    case MusicService.ACTION_PLAY:
                        music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        if(music == null){
                            return;
                        }
                        view.selectCurrentTrack(music);
                        break;

                    case DownloadService.ACTION_DOWNLOAD_STARTED:
                        music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        view.startDownloading(music);
                        break;
                    case DownloadService.ACTION_DOWNLOAD_COMPLETED:
                        music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        view.completeDownloading(music);
                        break;
                    case DownloadService.ACTION_DOWNLOAD_FAILURE:
                        music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        view.cancelDownloading(music);
                        break;
                }
            }
        };
    }
}
