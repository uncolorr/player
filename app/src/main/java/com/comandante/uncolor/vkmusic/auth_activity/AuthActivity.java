package com.comandante.uncolor.vkmusic.auth_activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.main_activity.MainActivity;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.services.music.NewMusicService;
import com.comandante.uncolor.vkmusic.utils.DurationConverter;
import com.comandante.uncolor.vkmusic.utils.IntentFilterManager;
import com.comandante.uncolor.vkmusic.widgets.SquareImageView;
import com.comandante.uncolor.vkmusic.widgets.StaticViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AuthActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, SeekBar.OnSeekBarChangeListener{

    @BindView(R.id.viewPager)
    StaticViewPager viewPager;

    @BindView(R.id.bottomNavigationView)
    BottomNavigationView bottomNavigationView;

    @BindView(R.id.bigPlayerPanel)
    LinearLayout bigPlayerPanel;

    @BindView(R.id.playerPanel)
    LinearLayout playerPanel;

    @BindView(R.id.imageViewMusicPlate)
    SquareImageView imageViewMusicPlate;

    @BindView(R.id.imageViewPanelAlbum)
    RoundedImageView imageViewPanelAlbum;

    @BindView(R.id.textViewPanelArtist)
    TextView textViewPanelArtist;

    @BindView(R.id.textViewPanelSongTitle)
    TextView textViewPanelSongTitle;

    @BindView(R.id.textViewPlayerArtist)
    TextView textViewPlayerArtist;

    @BindView(R.id.textViewPlayerSongTitle)
    TextView textViewPlayerSongTitle;

    @BindView(R.id.imageButtonPanelPlay)
    ImageButton imageButtonPanelPlay;

    @BindView(R.id.imageButtonPlayerPlay)
    ImageButton imageButtonPlayerPlay;

    @BindView(R.id.imageButtonShuffle)
    ImageButton imageButtonShuffle;

    @BindView(R.id.imageButtonRepeat)
    ImageButton imageButtonRepeat;

    @BindView(R.id.textViewDuration)
    TextView textViewDuration;

    @BindView(R.id.textViewCurrentPosition)
    TextView textViewCurrentPosition;

    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @BindView(R.id.progressBarMusic)
    ProgressBar progressBarMusic;

    private ServiceConnection serviceConnection;

    private NewMusicService newMusicService;

    private AuthFragmentPagerAdapter adapter;

    private BottomSheetBehavior sheetBehavior;

    private BroadcastReceiver musicReceiver;

    private int musicDuration;

    private Runnable musicPositionRunnable;

    private Handler handler;

    private boolean isBounded;


    public static Intent getInstance(Context context){
        return new Intent(context, AuthActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);
        init();
    }

    private void init(){
        if(App.isAuth()){
            startActivity(MainActivity.getInstance(this));
            finish();
            return;
        }
        imageButtonRepeat.setVisibility(View.INVISIBLE);
        imageButtonShuffle.setVisibility(View.INVISIBLE);
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
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                App.Log("onServiceConnected");
                newMusicService  = ((NewMusicService.MusicBinder)service).getService();
                bindPlayerState();
                isBounded = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                App.Log("onServiceDisconnected");
                isBounded = false;
            }
        };
    }

    @OnClick(R.id.playerPanel)
    void onPlayerPanelClick() {
        showPlayer();
    }

    @OnClick(R.id.imageButtonHide)
    void onImageButtonHideClick() {
        hidePlayer();
    }

    @OnClick({R.id.imageButtonPanelPlay, R.id.imageButtonPlayerPlay})
    void onPlayButtonClick() {
        if(isBounded){
            if(newMusicService.isPlaying()){
                newMusicService.pause();
                handler.removeCallbacks(musicPositionRunnable);
                setPlayButtons();
            }
            else {
                newMusicService.resume();
                handler.post(musicPositionRunnable);
                setPauseButtons();
            }
        }
    }

    private void changePlayingState(){
        if(isBounded){
            if(newMusicService.isPlaying()){
                handler.post(musicPositionRunnable);
                setPauseButtons();
            }
            else {
                handler.removeCallbacks(musicPositionRunnable);
                setPlayButtons();
            }
        }
    }

    @OnClick({R.id.imageButtonPanelNext, R.id.imageButtonPlayerNext})
    void onNextButtonClick() {
        if(isBounded) {
            BaseMusic music = newMusicService.next(true);
            setSongDescriptions(music);
            setPauseButtons();
            setPlaybackPosition(0);
            handler.removeCallbacks(musicPositionRunnable);
            Intent musicIntent = new Intent(NewMusicService.ACTION_NEXT);
            musicIntent.putExtra(NewMusicService.ARG_MUSIC, music);
            sendBroadcast(musicIntent);
        }
    }

    @OnClick(R.id.imageButtonPlayerPrevious)
    void onPreviousButtonClick() {
        if(isBounded) {
            BaseMusic music = newMusicService.previous();
            setSongDescriptions(music);
            setPauseButtons();
            setPlaybackPosition(0);
            handler.removeCallbacks(musicPositionRunnable);
            Intent musicIntent = new Intent(NewMusicService.ACTION_PREVIOUS);
            musicIntent.putExtra(NewMusicService.ARG_MUSIC, music);
            sendBroadcast(musicIntent);
        }
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
                if(isBounded) {
                    int currentPlaybackPosition = newMusicService.getCurrentPlaybackPosition();
                    progressBarMusic.setProgress(currentPlaybackPosition);
                    seekBar.setProgress(currentPlaybackPosition);
                    textViewCurrentPosition.setText(DurationConverter
                            .getDurationFormat(currentPlaybackPosition));
                    if (currentPlaybackPosition < musicDuration) {
                        handler.postDelayed(musicPositionRunnable, 1000);
                    }
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
                if (Objects.equals(action, NewMusicService.ACTION_PLAY)) {
                    Intent serviceIntent = new Intent(AuthActivity.this, NewMusicService.class);
                    bindService(serviceIntent, serviceConnection, 0);
                    BaseMusic music = intent.getParcelableExtra(NewMusicService.ARG_MUSIC);
                    setSongDescriptions(music);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    showPlayerPanel();
                    handler.removeCallbacks(musicPositionRunnable);
                }

                if (Objects.equals(action, NewMusicService.ACTION_PAUSE_OR_RESUME)) {
                    App.Log("new pause resume");
                    changePlayingState();
                }

                if (Objects.equals(action, NewMusicService.ACTION_NEXT)) {
                    BaseMusic music = intent.getParcelableExtra("music");
                    setSongDescriptions(music);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    handler.removeCallbacks(musicPositionRunnable);
                }

                if (Objects.equals(action, NewMusicService.ACTION_PREVIOUS)) {
                    BaseMusic music = intent.getParcelableExtra("music");
                    setSongDescriptions(music);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    handler.removeCallbacks(musicPositionRunnable);
                }

                if (Objects.equals(action, NewMusicService.ACTION_BEGIN_PLAYING)) {
                    handler.removeCallbacks(musicPositionRunnable);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    handler.post(musicPositionRunnable);
                }

                if (Objects.equals(action, NewMusicService.ACTION_CLOSE)) {
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
        App.Log("onResume auth");
        Intent serviceIntent = new Intent(this, NewMusicService.class);
        bindService(serviceIntent, serviceConnection, 0);
    }

    private void bindPlayerState(){
        if(newMusicService.isPlaying()){
            BaseMusic music = newMusicService.getCurrentMusic();
            App.Log("player playing");
            showPlayerPanel();
            setPauseButtons();
            setSongDescriptions(music);
            handler.post(musicPositionRunnable);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isBounded) return;
        unbindService(serviceConnection);
        isBounded = false;
        handler.removeCallbacks(musicPositionRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(musicReceiver != null) {
            unregisterReceiver(musicReceiver);
        }
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
            newMusicService.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
