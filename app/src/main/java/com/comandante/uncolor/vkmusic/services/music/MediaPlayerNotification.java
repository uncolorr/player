package com.comandante.uncolor.vkmusic.services.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.auth_activity.AuthActivity;
import com.comandante.uncolor.vkmusic.main_activity.MainActivity;
import com.comandante.uncolor.vkmusic.models.BaseMusic;

/**
 * Created by Uncolor on 24.09.2018.
 */

public class MediaPlayerNotification {

    public static final int NOTIFICATION_ID = 5646466;

    public static final String NOTIFICATION_CHANNEL_ID = "4535243424";
    public static final String NOTIFICATION_CHANNEL_NAME = "com.comandante.uncolor.vmusic";

    private Context context;

    private PendingIntent pendingIntentPrevious;
    private PendingIntent pendingIntentPlay;
    private PendingIntent pendingIntentNext;
    private PendingIntent pendingIntentClose;
    private PendingIntent pendingIntentContent;

    private RemoteViews views;
    private RemoteViews bigViews;

    private BaseMusic currentMusic;
    private boolean isPlaying;

    public MediaPlayerNotification(Context context, BaseMusic currentMusic, boolean isPlaying) {
        this.context = context;
        this.isPlaying = isPlaying;
        this.currentMusic = currentMusic;
        views = new RemoteViews(context.getPackageName(), R.layout.player_status_bar);
        bigViews = new RemoteViews(context.getPackageName(), R.layout.player_status_bar);
        pendingIntentContent = createContentPendingIntent();
        pendingIntentPlay = createPendingIntent(NewMusicService.ACTION_PAUSE_OR_RESUME);
        pendingIntentPrevious = createPendingIntent(NewMusicService.ACTION_PREVIOUS);
        pendingIntentNext = createPendingIntent(NewMusicService.ACTION_NEXT);
        pendingIntentClose = createPendingIntent(NewMusicService.ACTION_CLOSE);
        setPendingIntents();
        setMusicInfo();
    }

    private void setPendingIntents() {
        views.setOnClickPendingIntent(R.id.status_bar_play, pendingIntentPlay);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pendingIntentPlay);
        views.setOnClickPendingIntent(R.id.status_bar_next, pendingIntentNext);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pendingIntentNext);
        views.setOnClickPendingIntent(R.id.status_bar_prev, pendingIntentPrevious);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, pendingIntentPrevious);
        views.setOnClickPendingIntent(R.id.status_bar_close, pendingIntentClose);
        bigViews.setOnClickPendingIntent(R.id.status_bar_close, pendingIntentClose);
    }

    private void setMusicInfo() {
        int playButtonDrawable;
        if (isPlaying) {
            playButtonDrawable = R.drawable.pause;
        } else {
            playButtonDrawable = R.drawable.play;
        }

        views.setImageViewResource(R.id.status_bar_play, playButtonDrawable);
        bigViews.setImageViewResource(R.id.status_bar_play, playButtonDrawable);
        views.setTextViewText(R.id.status_bar_track_name, currentMusic.getTitle());
        bigViews.setTextViewText(R.id.status_bar_track_name, currentMusic.getTitle());
        views.setTextViewText(R.id.status_bar_artist_name, currentMusic.getArtist());
        bigViews.setTextViewText(R.id.status_bar_artist_name, currentMusic.getArtist());
    }

    public Notification getNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_check_mark)
                .setCustomContentView(views)
                .setCustomBigContentView(bigViews)
                .setContentIntent(pendingIntentContent)
                .build();
        return notification;
    }

    private PendingIntent createPendingIntent(String action) {
        Intent intent = new Intent(context, NewMusicService.class);
        intent.setAction(action);
        return PendingIntent.getService(context,
                0, intent,
                0);
    }

    private PendingIntent createContentPendingIntent() {
        Class clazz;
        if(App.isAuth()){
            clazz = MainActivity.class;
        }
        else {
            clazz = AuthActivity.class;
        }
        Intent notificationIntent = new Intent(context, clazz);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context,
                0, notificationIntent, 0);
    }
}
