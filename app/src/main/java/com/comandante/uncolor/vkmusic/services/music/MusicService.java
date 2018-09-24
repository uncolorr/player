package com.comandante.uncolor.vkmusic.services.music;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.models.BaseMusic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    public static final String ACTION_PLAY = "com.example.uncolor.action.PLAY";
    public static final String ACTION_PREVIOUS = "com.example.uncolor.action.PREVIOUS";
    public static final String ACTION_NEXT = "com.example.uncolor.action.NEXT";
    public static final String ACTION_CLOSE = "com.example.uncolor.action.CLOSE";
    public static final String ACTION_PAUSE_OR_RESUME = "com.example.uncolor.action.PAUSE_RESUME";
    public static final String ACTION_PLAYER_RESUME = "com.example.uncolor.action.PLAYER_RESUME";
    public static final String ACTION_BEGIN_PLAYING = "com.example.uncolor.action.BEGIN_PLAYING";
    public static final String ACTION_SEEK_BAR_MOVING = "com.example.uncolor.action.SEEK_BAR_MOVING";
    public static final String ACTION_CHANGE_LOOPING = "com.example.uncolor.action.IS_LOOPING";
    public static final String ACTION_SHUFFLE_PLAYLIST = "com.example.uncolor.action.SHUFFLE_PLAYLIST";

    public static final String ARG_PLAYLIST = "playlist";
    public static final String ARG_MUSIC = "music";
    public static final String ARG_POSITION = "position";
    public static final String ARG_IS_PAUSE = "isPause";
    public static final String ARG_IS_LOOPING = "isLooping";
    public static final String ARG_IS_SHUFFLING = "isShuffling";
    public static final String ARG_PLAYBACK_POSITION = "playbackPosition";

    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    private static final int TIME_LIMIT_FOR_TURN_PREVIOUS = 5000;

    private static final int SHUFFLE_SEED = 42;

    private MediaPlayer mediaPlayer;
    private ArrayList<BaseMusic> playlist = new ArrayList<>();
    private int playlistPosition;
    private BaseMusic music;
    private int playbackPosition;
    private Notification status;
    private boolean isLooping = false;
    private boolean isShuffling = false;
    private boolean isPreparing = false;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    private AudioManager audioManager;

    private final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_STOP
                            | PlaybackStateCompat.ACTION_PAUSE
                            | PlaybackStateCompat.ACTION_PLAY_PAUSE
                            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);


    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
              new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                            mediaPlayer.setVolume(0.5f, 0.5f);
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                            pauseOrResumeMedia();
                            showMediaPlayerNotification();
                            mediaController.getTransportControls().pause();
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS):
                            if(mediaPlayer == null){
                                return;
                            }
                            onActionPauseOrResume();
                            showMediaPlayerNotification();
                            mediaController.getTransportControls().pause();
                            break;

                        case (AudioManager.AUDIOFOCUS_GAIN):
                            mediaPlayer.setVolume(1f, 1f);
                            break;
                        default:
                            break;
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        App.Log("onCreate");
        initMediaPlayer();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(mediaSessionCallback);
        try {
            mediaController = new MediaControllerCompat(
                    this, mediaSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                App.Log("Action: " + action);
                Bundle extras = intent.getExtras();

                if (Objects.equals(action, ACTION_PLAYER_RESUME)) {
                    onActionPlayerStatus();
                } else {
                    int result = audioManager.requestAudioFocus(focusChangeListener,
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN);

                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        if (Objects.equals(action, ACTION_PLAY)) {
                            onActionPlay(extras);
                        }

                        if (Objects.equals(action, ACTION_PAUSE_OR_RESUME)) {
                            onActionPauseOrResume();
                        }

                        if (Objects.equals(action, ACTION_NEXT)) {
                            onActionNext(true);
                        }

                        if (Objects.equals(action, ACTION_PREVIOUS)) {
                            onActionPrevious();
                        }

                    }

                    if (Objects.equals(action, ACTION_SEEK_BAR_MOVING)) {
                        onActionSeekBarMoving(extras);
                    }

                    if (Objects.equals(action, ACTION_CLOSE)) {
                        onActionClose();
                    }

                    if (Objects.equals(action, ACTION_CHANGE_LOOPING)) {
                        onActionChangeLooping();
                    }

                    if (Objects.equals(action, ACTION_SHUFFLE_PLAYLIST)) {
                        onActionShufflePlaylist();
                    }
                }
                showMediaPlayerNotification();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void onActionShufflePlaylist() {
        if (!playlist.isEmpty()) {
            if (!isShuffling) {
                Collections.shuffle(playlist, new Random(SHUFFLE_SEED));
                isShuffling = true;
            } else {
                BaseMusic currentMusic = playlist.get(playlistPosition);
                unshuffle(new Random(SHUFFLE_SEED));
                for (int i = 0; i < playlist.size(); i++) {
                    if (currentMusic.getId() == playlist.get(i).getId()) {
                        playlistPosition = i;
                    }
                }
                isShuffling = false;
            }
        }
    }

    private void unshuffle(Random rnd) {
        int[] seq = new int[playlist.size()];
        for (int i = seq.length; i >= 1; i--) {
            seq[i - 1] = rnd.nextInt(i);
        }
        for (int i = 0; i < seq.length; i++) {
            Collections.swap(playlist, i, seq[i]);
        }
    }

    private void onActionChangeLooping() {
        isLooping = !isLooping;
    }

    private void onActionClose() {
        NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(MediaPlayerNotification.NOTIFICATION_ID);
            Intent musicIntent = new Intent(ACTION_CLOSE);
            sendBroadcast(musicIntent);
            stopSelf();
        }
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
        try {
            boolean isPause = !mediaPlayer.isPlaying();
            Intent musicIntent = new Intent(ACTION_PLAYER_RESUME);
            int currentPlaybackPosition = mediaPlayer.getCurrentPosition() / 1000;
            musicIntent.putExtra(ARG_IS_PAUSE, isPause);
            musicIntent.putExtra(ARG_IS_LOOPING, isLooping);
            musicIntent.putExtra(ARG_IS_SHUFFLING, isShuffling);
            if (!isPause) {
                musicIntent.putExtra(ARG_PLAYBACK_POSITION, currentPlaybackPosition);
                musicIntent.putExtra(ARG_MUSIC, music);
            }
            sendBroadcast(musicIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void onActionPlay(Bundle extras) {
        if (extras != null) {
            music = extras.getParcelable(ARG_MUSIC);
            playlist = extras.getParcelableArrayList(ARG_PLAYLIST);
            playlistPosition = extras.getInt(ARG_POSITION);
            if (isShuffling) {
                Collections.shuffle(playlist);
            }
            if (music != null) {
                App.Log("download url: " + music.getDownload());
                try {
                    playAudio(getMusicPath(music));
                    mediaController.getTransportControls().play();
                    Intent musicIntent = new Intent(ACTION_PLAY);
                    musicIntent.putExtra(ARG_MUSIC, music);
                    sendBroadcast(musicIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onActionNext(boolean fromUser) {
        App.Log("onActionNext");
        if (fromUser) {
            if (playlistPosition != playlist.size() - 1) {
                playlistPosition++;
                if (!playlist.isEmpty()) {
                    BaseMusic nextMusic = playlist.get(playlistPosition);
                    App.Log(nextMusic.getArtist());
                    App.Log(nextMusic.getTitle());
                    try {
                        music = nextMusic;
                        playAudio(getMusicPath(nextMusic));
                        Intent musicIntent = new Intent(ACTION_NEXT);
                        musicIntent.putExtra(ARG_MUSIC, nextMusic);
                        sendBroadcast(musicIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return;
        }
        if (isLooping) {
            if (!playlist.isEmpty()) {
                BaseMusic nextMusic = playlist.get(playlistPosition);
                App.Log(nextMusic.getArtist());
                App.Log(nextMusic.getTitle());
                try {
                    music = nextMusic;
                    playAudio(getMusicPath(nextMusic));
                    Intent musicIntent = new Intent(ACTION_NEXT);
                    musicIntent.putExtra(ARG_MUSIC, nextMusic);
                    sendBroadcast(musicIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (playlistPosition != playlist.size() - 1) {
                playlistPosition++;
                if (!playlist.isEmpty()) {
                    BaseMusic nextMusic = playlist.get(playlistPosition);
                    App.Log(nextMusic.getArtist());
                    App.Log(nextMusic.getTitle());
                    try {
                       updateMetadataInLockPlayerPanel();
                        music = nextMusic;
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
    }

    private void updateMetadataInLockPlayerPanel(){
        MediaMetadataCompat metadata = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, playlist.get(playlistPosition).getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, playlist.get(playlistPosition).getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, playlist.get(playlistPosition).getArtist())
                .build();
        mediaSession.setActive(true);
        mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        mediaSession.setMetadata(metadata);
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
                    music = previousMusic;
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
        App.Log("onActionPauseOrResume");
        if(mediaPlayer == null){
            return;
        }
        if (isPreparing) {
            isPreparing = false;
            Intent musicIntent = new Intent(ACTION_PAUSE_OR_RESUME);
            musicIntent.putExtra(ARG_IS_PAUSE, true);
            musicIntent.putExtra(ARG_PLAYBACK_POSITION, playbackPosition / 1000);
            sendBroadcast(musicIntent);
        } else {
            boolean isPause = pauseOrResumeMedia();
            Intent musicIntent = new Intent(ACTION_PAUSE_OR_RESUME);
            musicIntent.putExtra(ARG_IS_PAUSE, isPause);
            musicIntent.putExtra(ARG_PLAYBACK_POSITION, playbackPosition / 1000);
            sendBroadcast(musicIntent);
        }
    }


    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }

    private void playAudio(final String path) throws Exception {
        isPreparing = true;
        playbackPosition = 0;
        mediaPlayer.reset();
        mediaPlayer.setDataSource(path);
        mediaPlayer.prepareAsync();
    }


    private boolean pauseOrResumeMedia() {
        boolean isPause;
        if (mediaPlayer.isPlaying()) {
            App.Log("playing");
            mediaPlayer.pause();
            playbackPosition = mediaPlayer.getCurrentPosition();
            isPause = true;
        } else {
            App.Log("not playing");
            App.Log("playback position: " + playbackPosition);
            mediaPlayer.seekTo(playbackPosition);
            mediaPlayer.start();
            isPause = false;
        }
        return isPause;
    }


    private void killMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        App.Log("onDestroy Music service");
        super.onDestroy();
        killMediaPlayer();
        mediaSession.release();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        App.Log("onCompletion");
        App.Log("current position: " + mediaPlayer.getCurrentPosition());
        if (mediaPlayer.getCurrentPosition() != 0) {
            onActionNext(false);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        App.Log("onError");
        return false;
    }


    private void showMediaPlayerNotification() {
       if(playlist.isEmpty()){
           return;
       }
       MediaPlayerNotification mediaPlayerNotification =
               new MediaPlayerNotification(
                       this,
                       playlist.get(playlistPosition),
                       mediaPlayer.isPlaying());
        startForeground(MediaPlayerNotification.NOTIFICATION_ID, mediaPlayerNotification.getNotification());
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        App.Log("onPrepared");
        if (isPreparing) {
            isPreparing = false;
            mediaPlayer.start();
            showMediaPlayerNotification();
            Intent musicIntent = new Intent(ACTION_BEGIN_PLAYING);
            sendBroadcast(musicIntent);
        }
    }

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            App.Log("session on play");
            updateMetadataInLockPlayerPanel();
            mediaPlayer.start();

        }

        @Override
        public void onPause() {
            mediaPlayer.pause();
            playbackPosition = mediaPlayer.getCurrentPosition();
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        @Override
        public void onStop() {
            App.Log("media session onStop");
            // Все, больше мы не "главный" плеер, уходим со сцены
            mediaSession.setActive(false);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        @Override
        public void onSkipToNext() {
            App.Log("onSkipToNext");
            super.onSkipToNext();
            onActionNext(true);
            updateMetadataInLockPlayerPanel();
        }

        @Override
        public void onSkipToPrevious() {
            App.Log("onSkipToPrevious");
            super.onSkipToPrevious();
            onActionPrevious();
           updateMetadataInLockPlayerPanel();
        }
    };
}
