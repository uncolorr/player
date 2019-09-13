package com.comandante.uncolor.vkmusic.services.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.comandante.uncolor.vkmusic.application.App;

public class MediaComponentsController {

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    private AudioManager audioManager;
    private PlayerController mediaPlayerController;

    private Context context;

    private IntentFilter intentFilterForAudioStreamReceiver = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();

    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    private final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPlay() {
            App.Log("session on play");
            updateMetadataInLockPlayerPanel();
        }

        @Override
        public void onPause() {
            App.Log("session on pause ");
            mediaPlayerController.pause();
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        @Override
        public void onStop() {
            App.Log("session on stop");
            context.unregisterReceiver(myNoisyAudioStreamReceiver);
            mediaSession.setActive(false);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            App.Log("session on next");
            mediaPlayerController.next(true);
            updateMetadataInLockPlayerPanel();
        }

        @Override
        public void onSkipToPrevious() {
            App.Log("session on previous");
            super.onSkipToPrevious();
            mediaPlayerController.previous();
            updateMetadataInLockPlayerPanel();
        }
    };


    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                            App.Log("can duck");
                            mediaPlayerController.setVolume(0.5f);
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                            App.Log("loss transient");
                            mediaPlayerController.pause();
                            mediaController.getTransportControls().pause();
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS):
                            App.Log("loss audiofocus");
                            mediaPlayerController.pause();
                            mediaController.getTransportControls().pause();
                            break;

                        case (AudioManager.AUDIOFOCUS_GAIN):
                            App.Log("audiofocus gain");
                            mediaPlayerController.setVolume(1f);
                            break;
                        default:
                            break;
                    }
                }
            };


    public MediaComponentsController(Context context, PlayerController mediaPlayerController) {
        this.context = context;
        this.mediaPlayerController = mediaPlayerController;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mediaSession = new MediaSessionCompat(context, "PlayerService");
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(mediaSessionCallback);
        try {
            mediaController = new MediaControllerCompat(
                    context, mediaSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.context.registerReceiver(myNoisyAudioStreamReceiver, intentFilterForAudioStreamReceiver);
    }

    public void releaseMediaSession(){
        if(mediaSession != null){
            mediaSession.release();
        }
    }

    public int requestAudioFocus(){
        return audioManager.requestAudioFocus(focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            App.Log("on receive becoming noisy receiver");
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaPlayerController.pause();
            }
        }
    }

    private void updateMetadataInLockPlayerPanel() {
        App.Log("update metadata");
        MediaMetadataCompat metadata = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, PlaylistRepository.get().getCurrentMusic().getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, PlaylistRepository.get().getCurrentMusic().getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, PlaylistRepository.get().getCurrentMusic().getArtist())
                .build();
        mediaSession.setActive(true);
        mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
        mediaSession.setMetadata(metadata);
    }
}
