package com.example.uncolor.vkmusic.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.uncolor.vkmusic.application.App;
import com.example.uncolor.vkmusic.models.BaseMusic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
         MediaPlayer.OnErrorListener {

    public static final String ACTION_PLAY = "com.example.uncolor.action.PLAY";
    public static final String ACTION_PREVIOUS = "com.example.uncolor.action.PREVIOUS";
    public static final String ACTION_NEXT = "com.example.uncolor.action.NEXT";
    public static final String ACTION_PAUSE_OR_RESUME = "com.example.uncolor.action.PAUSE_RESUME";
    public static final String ACTION_PLAYER_STATUS = "com.example.uncolor.action.PLAYER_STATUS";


    public static final String ARG_PLAYLIST = "playlist";
    public static final String ARG_MUSIC = "music";
    public static final String ARG_POSITION = "position";
    public static final String ARG_IS_PAUSE = "isPause";
    public static final String ARG_PLAYBACK_POSITION = "playbackPosition";

    private static final int TIME_LIMIT_FOR_TURN_PREVIOUS = 5000;

    private MediaPlayer mediaPlayer;
    private ArrayList<BaseMusic> playlist = new ArrayList<>();
    private int playlistPosition;
    private BaseMusic music;
    private int playbackPosition;


    @Override
    public void onCreate() {
        super.onCreate();
        App.Log("onCreate");
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        App.Log("onStartCommand");
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if (Objects.equals(action, ACTION_PLAY)) {
           onActionPlay(extras);
        }

        if (Objects.equals(action, ACTION_PAUSE_OR_RESUME)) {
           onActionPauseOrResume();
        }

        if (Objects.equals(action, ACTION_NEXT)) {
            onActionNext();
        }

        if (Objects.equals(action, ACTION_PREVIOUS)) {
            onActionPrevious();
        }

        if (Objects.equals(action, ACTION_PLAYER_STATUS)) {
            onActionPlayerStatus();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void onActionPlayerStatus() {
        App.Log("onActionPlayerStatus");
        boolean isPause = !mediaPlayer.isPlaying();
        Intent musicIntent = new Intent(ACTION_PLAYER_STATUS);
        int currentPlaybackPosition = mediaPlayer.getCurrentPosition() / 1000;
        musicIntent.putExtra(ARG_IS_PAUSE, isPause);
        if(!isPause) {
            musicIntent.putExtra(ARG_PLAYBACK_POSITION, currentPlaybackPosition);
            musicIntent.putExtra(ARG_MUSIC, playlist.get(playlistPosition));
        }
        sendBroadcast(musicIntent);
    }


    private void onActionPlay(Bundle extras){
        if (extras != null) {
            music = extras.getParcelable(ARG_MUSIC);
            playlist = extras.getParcelableArrayList(ARG_PLAYLIST);
            playlistPosition = extras.getInt(ARG_POSITION);
            if (music != null) {
                App.Log("download url: " + music.getDownload());
                try {
                    playAudio(music.getDownload());
                    Intent musicIntent = new Intent(ACTION_PLAY);
                    musicIntent.putExtra(ARG_MUSIC, music);
                    sendBroadcast(musicIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onActionNext(){
        if (playlistPosition != playlist.size() - 1) {
            playlistPosition++;
            if(!playlist.isEmpty()) {
                BaseMusic nextMusic = playlist.get(playlistPosition);
                App.Log(nextMusic.getArtist());
                App.Log(nextMusic.getTitle());
                try {
                    playAudio(nextMusic.getDownload());
                    Intent musicIntent = new Intent(ACTION_NEXT);
                    musicIntent.putExtra(ARG_MUSIC, nextMusic);
                    sendBroadcast(musicIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onActionPrevious() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        if(currentPosition < TIME_LIMIT_FOR_TURN_PREVIOUS){
            if(playlistPosition != 0){
                playlistPosition--;
                BaseMusic previousMusic = playlist.get(playlistPosition);
                try {
                    playAudio(previousMusic.getDownload());
                    Intent musicIntent = new Intent(ACTION_PREVIOUS);
                    musicIntent.putExtra(ARG_MUSIC, previousMusic);
                    sendBroadcast(musicIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else {
            BaseMusic previousMusic = playlist.get(playlistPosition);
            try {
                playAudio(previousMusic.getDownload());
                Intent musicIntent = new Intent(ACTION_PREVIOUS);
                musicIntent.putExtra(ARG_MUSIC, previousMusic);
                sendBroadcast(musicIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onActionPauseOrResume(){
        boolean isPause = pauseOrResumeMedia();
        Intent musicIntent = new Intent(ACTION_PAUSE_OR_RESUME);
        musicIntent.putExtra(ARG_IS_PAUSE, isPause);
        musicIntent.putExtra(ARG_PLAYBACK_POSITION, playbackPosition / 1000);
        sendBroadcast(musicIntent);
    }

    public static IntentFilter getMusicIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLAY);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_PREVIOUS);
        intentFilter.addAction(ACTION_PAUSE_OR_RESUME);
        intentFilter.addAction(ACTION_PLAYER_STATUS);
        return intentFilter;
    }


    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
    }

    @SuppressLint("StaticFieldLeak")
    private void playAudio(final String url) throws Exception {
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();

    }


    private boolean pauseOrResumeMedia() {
        boolean isPause;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playbackPosition = mediaPlayer.getCurrentPosition();
            isPause = true;
        } else {
            mediaPlayer.seekTo(playbackPosition);
            mediaPlayer.start();
            isPause = false;
        }
        return isPause;
    }


    private void killMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        App.Log("onDestroy");
        super.onDestroy();
        killMediaPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        onActionNext();

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        App.Log("onError");
        return false;
    }

}
