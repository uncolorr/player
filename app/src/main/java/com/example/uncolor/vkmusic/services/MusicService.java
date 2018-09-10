package com.example.uncolor.vkmusic.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
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
    public static final String ACTION_PLAYER_RESUME = "com.example.uncolor.action.PLAYER_RESUME";
    public static final String ACTION_BEGIN_PLAYING = "com.example.uncolor.action.BEGIN_PLAYING";
    public static final String ACTION_SEEK_BAR_MOVING = "com.example.uncolor.action.SEEK_BAR_MOVING";


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
    private AsyncTask asyncTask;


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
        if(action != null) {
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

            if (Objects.equals(action, ACTION_PLAYER_RESUME)) {
                onActionPlayerStatus();
            }

            if (Objects.equals(action, ACTION_SEEK_BAR_MOVING)) {
                onActionSeekBarMoving(extras);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void onActionSeekBarMoving(Bundle extras) {
        if (extras != null) {
            playbackPosition = extras.getInt(ARG_PLAYBACK_POSITION, 0) * 1000;
            App.Log("current position: " + mediaPlayer.getCurrentPosition());
            mediaPlayer.seekTo(playbackPosition);
        }
    }

    private void onActionPlayerStatus() {
        App.Log("onActionPlayerStatus");
        boolean isPause = !mediaPlayer.isPlaying();
        Intent musicIntent = new Intent(ACTION_PLAYER_RESUME);
        int currentPlaybackPosition = mediaPlayer.getCurrentPosition() / 1000;
        musicIntent.putExtra(ARG_IS_PAUSE, isPause);
        if (!isPause) {
            musicIntent.putExtra(ARG_PLAYBACK_POSITION, currentPlaybackPosition);
            musicIntent.putExtra(ARG_MUSIC, playlist.get(playlistPosition));
        }
        sendBroadcast(musicIntent);
    }


    private void onActionPlay(Bundle extras) {
        if (extras != null) {
            music = extras.getParcelable(ARG_MUSIC);
            playlist = extras.getParcelableArrayList(ARG_PLAYLIST);
            playlistPosition = extras.getInt(ARG_POSITION);
            if (music != null) {
                App.Log("download url: " + music.getDownload());
                try {
                    playAudio(getMusicPath(music));
                    Intent musicIntent = new Intent(ACTION_PLAY);
                    musicIntent.putExtra(ARG_MUSIC, music);
                    sendBroadcast(musicIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onActionNext() {
        if (playlistPosition != playlist.size() - 1) {
            playlistPosition++;
            if (!playlist.isEmpty()) {
                BaseMusic nextMusic = playlist.get(playlistPosition);
                App.Log(nextMusic.getArtist());
                App.Log(nextMusic.getTitle());
                try {
                    playAudio(getMusicPath(nextMusic));
                    Intent musicIntent = new Intent(ACTION_NEXT);
                    musicIntent.putExtra(ARG_MUSIC, nextMusic);
                    sendBroadcast(musicIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getMusicPath(BaseMusic music) {
        switch (music.getState()) {
            case BaseMusic.STATE_DEFAULT:
            case BaseMusic.STATE_DOWNLOADING:
                return music.getDownload();
            case BaseMusic.STATE_COMPLETED:
                return music.getLocalPath();
        }
        return null;
    }

    private void onActionPrevious() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        if (currentPosition < TIME_LIMIT_FOR_TURN_PREVIOUS) {
            if (playlistPosition != 0) {
                playlistPosition--;
                BaseMusic previousMusic = playlist.get(playlistPosition);
                try {
                    playAudio(getMusicPath(previousMusic));
                    Intent musicIntent = new Intent(ACTION_PREVIOUS);
                    musicIntent.putExtra(ARG_MUSIC, previousMusic);
                    sendBroadcast(musicIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            BaseMusic previousMusic = playlist.get(playlistPosition);
            try {
                playAudio(getMusicPath(previousMusic));
                Intent musicIntent = new Intent(ACTION_PREVIOUS);
                musicIntent.putExtra(ARG_MUSIC, previousMusic);
                sendBroadcast(musicIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onActionPauseOrResume() {
        boolean isPause = pauseOrResumeMedia();
        Intent musicIntent = new Intent(ACTION_PAUSE_OR_RESUME);
        musicIntent.putExtra(ARG_IS_PAUSE, isPause);
        musicIntent.putExtra(ARG_PLAYBACK_POSITION, playbackPosition / 1000);
        sendBroadcast(musicIntent);
    }


    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
    }

    @SuppressLint("StaticFieldLeak")
    private void playAudio(final String path) throws Exception {
        if (asyncTask != null) {
            asyncTask.cancel(false);
        }
        asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent musicIntent = new Intent(ACTION_BEGIN_PLAYING);
                sendBroadcast(musicIntent);
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
