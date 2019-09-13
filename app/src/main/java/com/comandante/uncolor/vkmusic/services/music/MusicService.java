package com.comandante.uncolor.vkmusic.services.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.models.BaseMusic;

import java.io.IOException;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, PlayerController {

    public static final String ACTION_PLAY = "com.radio.uncolor.action._PLAY";
    public static final String ACTION_PREVIOUS = "com.radio.uncolor.action._PREVIOUS";
    public static final String ACTION_NEXT = "com.radio.radio.action._NEXT";
    public static final String ACTION_CLOSE = "com.radio.radio.action._CLOSE";
    public static final String ACTION_PAUSE = "com.radio.radio.action._PAUSE";
    public static final String ACTION_RESUME = "com.radio.radio.action._RESUME";
    public static final String ACTION_PAUSE_OR_RESUME = "com.radio.uncolor.action._PAUSE_RESUME";
    public static final String ACTION_BEGIN_PLAYING = "com.radio.uncolor.action._BEGIN_PLAYING";

    public static final String ARG_MUSIC = "music";

    private static final int TIME_LIMIT_FOR_TURN_PREVIOUS = 5000;

    private MediaPlayer mediaPlayer;

    private MediaComponentsController componentsController;

    private TempMusicBinder binder = new TempMusicBinder();
    private boolean isPreparing;
    private boolean isLooping;
    private boolean isShuffling;
    private int playbackPosition;
    private int shuffleSeed;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        shuffleSeed = Shuffler.generateShuffleSeed();
        componentsController = new MediaComponentsController(this, this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null){
            return super.onStartCommand(null, flags, startId);
        }
        String action = intent.getAction();
        if(action == null){
            return super.onStartCommand(intent, flags, startId);
        }
        int result = componentsController.requestAudioFocus();
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return super.onStartCommand(intent, flags, startId);
        }

        Bundle extras = intent.getExtras();
        switch (action){
            case ACTION_PLAY:
                if(extras == null){
                    return super.onStartCommand(intent, flags, startId);
                }
                onActionPlay(extras);
                break;

            case ACTION_NEXT:
                next(true);
                break;

            case ACTION_PREVIOUS:
                previous();
                break;

            case ACTION_PAUSE_OR_RESUME:
                if(mediaPlayer.isPlaying()){
                    pause();
                }
                else {
                    resume();
                }
                break;

            case ACTION_CLOSE:
                onActionClose();
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (isPreparing) {
            App.Log("onPrepared");
            isPreparing = false;
            mp.start();
            showMediaPlayerNotification();
            sendBroadcast(ACTION_BEGIN_PLAYING);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next(false);
    }

    private void onActionPlay(Bundle extras) {
        if(extras == null){
            return;
        }

        BaseMusic music = extras.getParcelable(ARG_MUSIC);

        List<BaseMusic> playlist = PlaylistRepository.get().getPlaylist();
        int playlistPosition = PlaylistRepository.get().getPosition();

        if(music == null){
            return;
        }
        if(playlist == null){
            throw new RuntimeException("playlist is null");
        }
        if(playlist.isEmpty()){
            return;
        }
        if(playlistPosition < 0){
            return;
        }

        if (isShuffling) {
            Shuffler.shuffle(playlist, shuffleSeed);
        }

        try {
            play(music);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onActionClose() {
        NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            App.Log("check close");
            notificationManager.cancel(MediaPlayerNotification.NOTIFICATION_ID);
            Intent musicIntent = new Intent(ACTION_CLOSE);
            sendBroadcast(musicIntent);
            stopSelf();
        }
    }

    //play
    //pause
    //resume
    //next
    //previous
    //isPlaying
    //isShuffling
    //isLooping
    //setLooping
    //shuffle(boolean)
    //seekTo

    private void play(BaseMusic music) throws IOException {
        isPreparing = true;
        playbackPosition = 0;
        mediaPlayer.reset();
        mediaPlayer.setDataSource(getMusicPath(music));
        mediaPlayer.prepareAsync();
        showMediaPlayerNotification();
        Bundle extras = new Bundle();
        extras.putParcelable(ARG_MUSIC, music);
        sendBroadcast(ACTION_PLAY, extras);
    }

    @Override
    public void pause() {
        App.Log("call pause");
        if(mediaPlayer == null){
            return;
        }
        mediaPlayer.pause();
        playbackPosition = mediaPlayer.getCurrentPosition();
        showMediaPlayerNotification();
        sendBroadcast(ACTION_PAUSE);
    }

    @Override
    public void resume() {
        mediaPlayer.seekTo(playbackPosition);
        mediaPlayer.start();
        showMediaPlayerNotification();
        sendBroadcast(ACTION_RESUME);

    }

    @Override
    public void setVolume(float volume) {
        mediaPlayer.setVolume(volume, volume);
    }

    @Override
    public BaseMusic next(boolean fromUser) {
        List<BaseMusic> playlist = PlaylistRepository.get().getPlaylist();
        int playlistPosition = PlaylistRepository.get().getPosition();

        if (playlist.isEmpty()) {
            return null;
        }
        if (playlistPosition == playlist.size() - 1) {
            if (fromUser) {
                return null;
            } else {
                pause();
                mediaPlayer.seekTo(0);
                return null;
            }
        }
        if ((!(isLooping && !fromUser))) {
            playlistPosition++;
        }

        PlaylistRepository.updatePosition(playlistPosition);
        App.Log("playlist position: " + playlistPosition);

        try {
            play(playlist.get(playlistPosition));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playlist.get(playlistPosition);
    }


    @Override
    public BaseMusic previous() {
        List<BaseMusic> playlist = PlaylistRepository.get().getPlaylist();
        int playlistPosition = PlaylistRepository.get().getPosition();

        if (playlist.isEmpty()) {
            return null;
        }

        int currentPlaybackPosition = mediaPlayer.getCurrentPosition();
        if (currentPlaybackPosition < TIME_LIMIT_FOR_TURN_PREVIOUS && playlistPosition != 0) {
            playlistPosition--;
        }

        PlaylistRepository.updatePosition(playlistPosition);
        App.Log("playlist position: " + playlistPosition);
        try {
            play(playlist.get(playlistPosition));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playlist.get(playlistPosition);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public boolean isLooping() {
        return isLooping;
    }

    public boolean isPreparing() {
        return isPreparing;
    }


    public boolean isShuffling(){
        return isShuffling;
    }

    public void setLooping(boolean isLooping){
        this.isLooping = isLooping;
    }

    public void setShuffle(boolean isShuffling){
        List<BaseMusic> playlist = PlaylistRepository.get().getPlaylist();
        this.isShuffling = isShuffling;
        if(isShuffling) {
            Shuffler.shuffle(playlist, shuffleSeed);
        } else {
            Shuffler.unshuffle(playlist, shuffleSeed);
        }
    }


    public void seekTo(int playbackPosition) {
        mediaPlayer.seekTo(playbackPosition * 1000);
        this.playbackPosition = mediaPlayer.getCurrentPosition();
    }

    private void sendBroadcast(String action) {
        Intent musicIntent = new Intent(action);
        sendBroadcast(musicIntent);
    }

    private void sendBroadcast(String action, Bundle extras) {
        Intent musicIntent = new Intent(action);
        musicIntent.putExtras(extras);
        sendBroadcast(musicIntent);
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

    private String getMusicPath(BaseMusic music) {
        switch (music.getState()) {
            case BaseMusic.STATE_DEFAULT:
            case BaseMusic.STATE_DOWNLOADING:
                return music.getDownload();
            case BaseMusic.STATE_COMPLETED:
                return music.getLocalPath();
        }
        return music.getDownload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.Log("Service onDestroy");
        killMediaPlayer();
        componentsController.releaseMediaSession();
    }

    public int getCurrentPlaybackPosition() {
        if(mediaPlayer == null){
            return 0;
        }
        return mediaPlayer.getCurrentPosition() / 1000;
    }


    private void showMediaPlayerNotification() {
        if (PlaylistRepository.get().isEmpty()) {
            return;
        }
        MediaPlayerNotification mediaPlayerNotification =
                new MediaPlayerNotification(
                        this,
                        PlaylistRepository.get().getCurrentMusic(),
                        mediaPlayer.isPlaying());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground(mediaPlayerNotification.getNotification());
        else {
            startForeground(MediaPlayerNotification.NOTIFICATION_ID, mediaPlayerNotification.getNotification());
        }
    }

    private void startMyOwnForeground(Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(MediaPlayerNotification.NOTIFICATION_CHANNEL_ID,
                    MediaPlayerNotification.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
            startForeground(MediaPlayerNotification.NOTIFICATION_ID, notification);
        }
    }

    public class TempMusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
