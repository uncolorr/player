package com.comandante.uncolor.vkmusic.main_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.services.music.MusicService;

public class MainActivityPresenter implements MainActivityContract.Presenter{

    private Context context;
    private MainActivityContract.View view;


    public MainActivityPresenter(Context context, MainActivityContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void onBindPlayerState(BaseMusic music, boolean isLooping, boolean isShuffling) {
        view.showPlayerBar();
        view.setPauseButtons();
        view.setSongDescriptions(music);
        view.setLoopingState(isLooping);
        view.setShufflingState(isShuffling);
    }

    @Override
    public BroadcastReceiver getTempMusicReceiver(){
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

                switch (action){
                    case MusicService.ACTION_PLAY:

                        BaseMusic music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                        if(music == null){
                            return;
                        }
                        view.setSongDescriptions(music);
                        view.setPauseButtons();
                        view.updateMusicPosition();
                        view.showPlayerBar();
                        view.pauseProgressUpdate();
                        break;

                    case MusicService.ACTION_BEGIN_PLAYING:
                        view.checkServiceConnection();
                        view.pauseProgressUpdate();
                        view.setPauseButtons();
                        view.updateMusicPosition();
                        view.resumeProgressUpdate();
                        view.startProgressUpdate();
                        break;

                    case MusicService.ACTION_PAUSE:
                        view.setPlayButtons();
                        view.pauseProgressUpdate();
                        break;

                    case MusicService.ACTION_RESUME:
                        view.setPauseButtons();
                        view.resumeProgressUpdate();
                        break;

                    case MusicService.ACTION_CLOSE:
                        view.hidePlayerBar();
                        view.hidePlayerPanel();
                        view.setPlayButtons();
                        break;
                }
            }
        };
    }

    @Override
    public void onSwitchTrack(String action, BaseMusic music) {
        view.setSongDescriptions(music);
        view.setPauseButtons();
        view.updateMusicPosition();
        view.pauseProgressUpdate();
        //view.broadcastSwitchedTrack(action, music);
    }

    @Override
    public void onPauseTrack() {
        view.pauseProgressUpdate();
        view.setPlayButtons();
    }

    @Override
    public void onResumeTrack() {
        view.resumeProgressUpdate();
        view.setPauseButtons();
    }
}
