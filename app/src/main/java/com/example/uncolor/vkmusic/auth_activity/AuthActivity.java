package com.example.uncolor.vkmusic.auth_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.uncolor.vkmusic.IntentFilterManager;
import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.application.App;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.services.MusicService;
import com.example.uncolor.vkmusic.utils.DurationConverter;
import com.example.uncolor.vkmusic.widgets.SquareImageView;
import com.example.uncolor.vkmusic.widgets.StaticViewPager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Objects;

@EActivity(R.layout.activity_auth)
public class AuthActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, SeekBar.OnSeekBarChangeListener{


    @ViewById
    StaticViewPager viewPager;

    @ViewById
    BottomNavigationView bottomNavigationView;

    @ViewById
    LinearLayout bigPlayerPanel;

    @ViewById
    LinearLayout playerPanel;

    @ViewById
    SquareImageView imageViewMusicPlate;

    @ViewById
    SquareImageView imageViewPanelAlbum;

    @ViewById
    TextView textViewPanelArtist;

    @ViewById
    TextView textViewPanelSongTitle;

    @ViewById
    TextView textViewPlayerArtist;

    @ViewById
    TextView textViewPlayerSongTitle;

    @ViewById
    ImageButton imageButtonPanelPlay;

    @ViewById
    ImageButton imageButtonPlayerPlay;

    @ViewById
    TextView textViewDuration;

    @ViewById
    TextView textViewCurrentPosition;

    @ViewById
    SeekBar seekBar;

    @ViewById
    ProgressBar progressBarMusic;

    private AuthFragmentPagerAdapter adapter;

    private BottomSheetBehavior sheetBehavior;

    private BroadcastReceiver musicReceiver;

    private int musicProgressPosition;

    private int musicDuration;

    private Runnable musicPositionRunnable;

    private Handler handler;


    @AfterViews
    void init(){
        sheetBehavior = BottomSheetBehavior.from(bigPlayerPanel);
        hidePlayer();
        adapter = new AuthFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setPagingEnabled(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        handler = new Handler();
        musicPositionRunnable = getMusicPositionRunnable();
        seekBar.setOnSeekBarChangeListener(this);
        musicReceiver = getMusicReceiver();
        registerReceiver(musicReceiver, IntentFilterManager.getMusicIntentFilter());
    }


    @Click(R.id.playerPanel)
    void onPlayerPanelClick() {
        showPlayer();
    }

    @Click(R.id.imageButtonHide)
    void onImageButtonHideClick() {
        hidePlayer();
    }

    @Click({R.id.imageButtonPanelPlay, R.id.imageButtonPlayerPlay})
    void onPlayButtonClick() {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.ACTION_PAUSE_OR_RESUME);
        startService(intent);
    }

    @Click({R.id.imageButtonPanelNext, R.id.imageButtonPlayerNext})
    void onNextButtonClick() {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.ACTION_NEXT);
        startService(intent);
    }

    @Click(R.id.imageButtonPlayerPrevious)
    void onPreviousButtonClick() {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.ACTION_PREVIOUS);
        startService(intent);
    }

    private boolean isPlayerPanelHidden() {
        return sheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN;
    }

    private void showPlayer() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void hidePlayer() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private Runnable getMusicPositionRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                musicProgressPosition++;
                progressBarMusic.setProgress(musicProgressPosition);
                seekBar.setProgress(musicProgressPosition);
                textViewCurrentPosition.setText(DurationConverter
                        .getDurationFormat(musicProgressPosition));
                if (musicProgressPosition < musicDuration) {
                    handler.postDelayed(musicPositionRunnable, 1000);
                }
            }
        };
    }

    private BroadcastReceiver getMusicReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                App.Log("onReceive");
                String action = intent.getAction();
                if (Objects.equals(action, MusicService.ACTION_PLAY)) {
                    BaseMusic music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                    setSongDescriptions(music);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    showPlayerPanel();
                    handler.removeCallbacks(musicPositionRunnable);
                }

                if (Objects.equals(action, MusicService.ACTION_PAUSE_OR_RESUME)) {
                    boolean isPause = intent.getBooleanExtra(MusicService.ARG_IS_PAUSE, true);
                    if (isPause) {
                        setPlayButtons();
                        handler.removeCallbacks(musicPositionRunnable);
                    } else {
                        setPauseButtons();
                        handler.post(musicPositionRunnable);
                    }
                }

                if (Objects.equals(action, MusicService.ACTION_NEXT)) {
                    BaseMusic music = intent.getParcelableExtra("music");
                    setSongDescriptions(music);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    handler.removeCallbacks(musicPositionRunnable);
                }

                if (Objects.equals(action, MusicService.ACTION_PREVIOUS)) {
                    BaseMusic music = intent.getParcelableExtra("music");
                    setSongDescriptions(music);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    handler.removeCallbacks(musicPositionRunnable);
                }

                if (Objects.equals(action, MusicService.ACTION_PLAYER_RESUME)) {
                    boolean isPause = intent.getBooleanExtra(MusicService.ARG_IS_PAUSE, true);
                    int playbackPosition = intent.getIntExtra(MusicService.ARG_PLAYBACK_POSITION, 0);
                    BaseMusic music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                    if (isPause) {
                        hidePlayerPanel();
                    } else {
                        showPlayerPanel();
                        setSongDescriptions(music);
                        setPauseButtons();
                        setDurationForBars(music.getDuration());
                        setPlaybackPosition(playbackPosition);
                        handler.post(musicPositionRunnable);
                    }
                }

                if (Objects.equals(action, MusicService.ACTION_BEGIN_PLAYING)) {
                    handler.removeCallbacks(musicPositionRunnable);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    handler.post(musicPositionRunnable);
                }

                if (Objects.equals(action, MusicService.ACTION_CLOSE)) {
                    App.Log("Action close received");
                    handler.removeCallbacks(musicPositionRunnable);
                    hidePlayerPanel();
                }

            }
        };
    }

    private void showPlayerPanel() {
        progressBarMusic.setVisibility(View.VISIBLE);
        playerPanel.setVisibility(View.VISIBLE);
    }

    private void hidePlayerPanel() {
        progressBarMusic.setVisibility(View.GONE);
        playerPanel.setVisibility(View.GONE);
    }

    private void setPlayButtons() {
        imageButtonPanelPlay.setImageResource(R.drawable.play);
        imageButtonPlayerPlay.setImageResource(R.drawable.play);
    }

    private void setPauseButtons() {
        imageButtonPanelPlay.setImageResource(R.drawable.pause);
        imageButtonPlayerPlay.setImageResource(R.drawable.pause);
    }

    private void setSongDescriptions(BaseMusic music) {
        textViewPanelSongTitle.setText(music.getTitle());
        textViewPanelArtist.setText(music.getArtist());
        textViewPlayerSongTitle.setText(music.getTitle());
        textViewPlayerArtist.setText(music.getArtist());
        musicDuration = music.getDuration();
        textViewDuration.setText(DurationConverter.getDurationFormat(music.getDuration()));
        setDurationForBars(music.getDuration());
        setAlbumImage(music.getAlbumImageUrl());
    }

    private void setPlaybackPosition(int playbackPosition) {
        musicProgressPosition = playbackPosition;
        progressBarMusic.setProgress(playbackPosition);
        seekBar.setProgress(playbackPosition);
    }

    public void setAlbumImage(String url){
        if(url == null){
            imageViewMusicPlate.setImageResource(R.drawable.album_default);
            imageViewPanelAlbum.setImageResource(R.drawable.album_default);
            return;
        }
        Glide.with(this).load(url).into(imageViewMusicPlate);
        Glide.with(this).load(url).into(imageViewPanelAlbum);

    }

    public void setDurationForBars(int duration) {
        seekBar.setMax(duration);
        progressBarMusic.setMax(duration);
    }

    @Override
    public void onBackPressed() {
        if (!isPlayerPanelHidden()) {
            hidePlayer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAYER_RESUME);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(musicPositionRunnable);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_music:
                viewPager.setCurrentItem(0, true);
                break;
            case R.id.action_auth:
                viewPager.setCurrentItem(1, true);
                break;
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser){
            App.Log("progress: " + Integer.toString(progress));
            setPlaybackPosition(progress);
            Intent intent = new Intent(this, MusicService.class);
            intent.setAction(MusicService.ACTION_SEEK_BAR_MOVING);
            intent.putExtra(MusicService.ARG_PLAYBACK_POSITION, progress);
            startService(intent);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
