package com.example.uncolor.vkmusic.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.application.App;
import com.example.uncolor.vkmusic.main_activity.MainActivity_;
import com.example.uncolor.vkmusic.models.BaseMusic;

import java.util.ArrayList;
import java.util.Objects;

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

    public static final String ARG_PLAYLIST = "playlist";
    public static final String ARG_MUSIC = "music";
    public static final String ARG_POSITION = "position";
    public static final String ARG_IS_PAUSE = "isPause";
    public static final String ARG_PLAYBACK_POSITION = "playbackPosition";

    private static final int NOTIFICATION_ID = 564646;

    private static final int TIME_LIMIT_FOR_TURN_PREVIOUS = 5000;

    private MediaPlayer mediaPlayer;
    private ArrayList<BaseMusic> playlist = new ArrayList<>();
    private int playlistPosition;
    private BaseMusic music;
    private int playbackPosition;
    private Notification status;
    private boolean isPreparing = false;


    @Override
    public void onCreate() {
        super.onCreate();
        App.Log("onCreate");
        initMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if (action != null) {
                App.Log("Action: " + action);
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

                if (Objects.equals(action, ACTION_CLOSE)) {
                    onActionClose();
                }
            }
            showNotification();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void onActionClose() {
        App.Log("onActionClose");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager != null) {
            App.Log("send");
            notificationManager.cancel(NOTIFICATION_ID);
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
        App.Log("onActionPauseOrResume");
        if(isPreparing){
            isPreparing = false;
            Intent musicIntent = new Intent(ACTION_PAUSE_OR_RESUME);
            musicIntent.putExtra(ARG_IS_PAUSE, true);
            musicIntent.putExtra(ARG_PLAYBACK_POSITION, playbackPosition / 1000);
            sendBroadcast(musicIntent);
        }
        else {
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

    @SuppressLint("StaticFieldLeak")
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
                mediaPlayer.release();
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
        if(mediaPlayer.getCurrentPosition() != 0) {
            onActionNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        App.Log("onError");
        return false;
    }


    private void showNotification() {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.player_status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(), R.layout.player_status_bar);
        if(App.isAuth()){

        }
        Intent notificationIntent = new Intent(this, MainActivity_.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                notificationIntent,
                0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent pendingPreviousIntent = PendingIntent.getService(this,
                0,
                previousIntent,
                0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(ACTION_PAUSE_OR_RESUME);
        PendingIntent pendingPlayIntent = PendingIntent.getService(this,
                0,
                playIntent,
                0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent pendingNextIntent = PendingIntent.getService(this,
                0, nextIntent,
                0);

        Intent closeIntent = new Intent(this, MusicService.class);
        closeIntent.setAction(ACTION_CLOSE);
        PendingIntent pendingCloseIntent = PendingIntent.getService(this,
                0, closeIntent,
                0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pendingPlayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pendingPlayIntent);
        views.setOnClickPendingIntent(R.id.status_bar_next, pendingNextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pendingNextIntent);
        views.setOnClickPendingIntent(R.id.status_bar_prev, pendingPreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, pendingPreviousIntent);
        views.setOnClickPendingIntent(R.id.status_bar_close, pendingCloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_close, pendingCloseIntent);

        if(playlist.isEmpty()){
            return;
        }

        BaseMusic currentMusic = playlist.get(playlistPosition);
        int playButtonDrawable;
        if(mediaPlayer.isPlaying()){
            playButtonDrawable = R.drawable.pause;
        }
        else {
            playButtonDrawable = R.drawable.play;
        }

        views.setImageViewResource(R.id.status_bar_play, playButtonDrawable);
        bigViews.setImageViewResource(R.id.status_bar_play, playButtonDrawable);
        views.setTextViewText(R.id.status_bar_track_name, currentMusic.getTitle());
        bigViews.setTextViewText(R.id.status_bar_track_name, currentMusic.getTitle());
        views.setTextViewText(R.id.status_bar_artist_name, currentMusic.getArtist());
        bigViews.setTextViewText(R.id.status_bar_artist_name, currentMusic.getArtist());
        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_check_mark;
        status.contentIntent = pendingIntent;
        startForeground(NOTIFICATION_ID, status);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        App.Log("onPrepared");
        if(isPreparing) {
            isPreparing = false;
            mediaPlayer.start();
            showNotification();
            Intent musicIntent = new Intent(ACTION_BEGIN_PLAYING);
            sendBroadcast(musicIntent);
        }
    }
}
