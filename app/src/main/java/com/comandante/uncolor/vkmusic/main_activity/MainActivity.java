package com.comandante.uncolor.vkmusic.main_activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.comandante.uncolor.vkmusic.services.download.NewDownloadService;
import com.comandante.uncolor.vkmusic.services.music.NewMusicService;
import com.comandante.uncolor.vkmusic.utils.IntentFilterManager;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.utils.DurationConverter;
import com.comandante.uncolor.vkmusic.widgets.SquareImageView;
import com.comandante.uncolor.vkmusic.widgets.StaticViewPager;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.makeramen.roundedimageview.RoundedImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Objects;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        SeekBar.OnSeekBarChangeListener{

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
    RoundedImageView imageViewPanelAlbum;

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
    ImageButton imageButtonRepeat;

    @ViewById
    ImageButton imageButtonShuffle;

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

    private MainPagerAdapter adapter;

    private BottomSheetBehavior sheetBehavior;

    private BroadcastReceiver musicReceiver;

    private BroadcastReceiver downloadReceiver;

    private Runnable musicPositionRunnable;

    private Handler handler;

    private int musicDuration;

    private ServiceConnection serviceConnectionForMusic;


    private NewMusicService newMusicService;


    private boolean isBoundedMusic = false;



    public static Intent getInstance(Context context) {
        return new Intent(context, MainActivity_.class);
    }

    @AfterViews
    void init() {
        sheetBehavior = BottomSheetBehavior.from(bigPlayerPanel);
        hidePlayer();
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPagingEnabled(false);
        musicReceiver = getMusicReceiver();
        downloadReceiver = getDownloadReceiver();
        registerReceiver(musicReceiver, IntentFilterManager.getMusicIntentFilter());
        registerReceiver(downloadReceiver, IntentFilterManager.getDownloadIntentFilter());
        handler = new Handler();
        musicPositionRunnable = getMusicPositionRunnable();
        seekBar.setOnSeekBarChangeListener(this);
        serviceConnectionForMusic = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                App.Log("onServiceConnected");
                newMusicService = ((NewMusicService.MusicBinder) service).getService();
                bindPlayerState();
                isBoundedMusic = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                App.Log("onServiceDisconnected");
                isBoundedMusic = false;
            }
        };
    }

    private BroadcastReceiver getDownloadReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                App.Log("New Download received");
                DownloadManager downloadManager = ((DownloadManager) getSystemService(DOWNLOAD_SERVICE));
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

                    App.Log("New Download complete");
                    /*long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = downloadManager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c
                                .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            progressBarDownloadProgress.setVisibility(View.GONE);
                        }
                    }*/
                }
            }
        };
    }

    private void bindPlayerState() {
        if (newMusicService.isPlaying()) {
            BaseMusic music = newMusicService.getCurrentMusic();
            App.Log("player playing");
            showPlayerPanel();
            setPauseButtons();
            setSongDescriptions(music);
            changeRepeatButtonState(newMusicService.isLooping());
            changeShuffleButtonState(newMusicService.isShuffling());
            handler.post(musicPositionRunnable);
        }
    }

    @Click(R.id.playerPanel)
    void onPlayerPanelClick() {
        showPlayer();
    }

    @Click(R.id.imageButtonHide)
    void onImageButtonHideClick() {
        hidePlayer();
    }

    @Click(R.id.imageButtonRepeat)
    void onRepeatButtonClick() {
        boolean isLooping = newMusicService.isLooping();
        newMusicService.setLooping(!isLooping);
        changeRepeatButtonState(!isLooping);
    }

    @Click(R.id.imageButtonShuffle)
    void onShuffleButtonClick() {
        boolean isShuffling = newMusicService.isShuffling();
        if (isShuffling) {
            newMusicService.unshuffle();
        } else {
            newMusicService.shuffle();
        }
        changeShuffleButtonState(!isShuffling);
    }


    @Click({R.id.imageButtonPanelPlay, R.id.imageButtonPlayerPlay})
    void onPlayButtonClick() {
        if (isBoundedMusic) {
            if (newMusicService.isPlaying()) {
                newMusicService.pause();
                handler.removeCallbacks(musicPositionRunnable);
                setPlayButtons();
            } else {
                newMusicService.resume();
                handler.post(musicPositionRunnable);
                setPauseButtons();
            }
        }
    }

    @Click({R.id.imageButtonPanelNext, R.id.imageButtonPlayerNext})
    void onNextButtonClick() {
        if (isBoundedMusic) {
            BaseMusic music = newMusicService.next(true);
            if(music != null) {
                setSongDescriptions(music);
                setPauseButtons();
                setPlaybackPosition(0);
                handler.removeCallbacks(musicPositionRunnable);
                Intent musicIntent = new Intent(NewMusicService.ACTION_NEXT);
                musicIntent.putExtra(NewMusicService.ARG_MUSIC, music);
                sendBroadcast(musicIntent);
            }
        }
    }

    @Click(R.id.imageButtonPlayerPrevious)
    void onPreviousButtonClick() {
        if (isBoundedMusic) {
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

    @Override
    public void onBackPressed() {
        if (!isPlayerPanelHidden()) {
            hidePlayer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(musicReceiver);
    }

    private Runnable getMusicPositionRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                if (isBoundedMusic) {
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
                if (intent == null) {
                    App.Log("intent null");
                }
                String action = intent.getAction();
                App.Log("point");
                if (Objects.equals(action, NewMusicService.ACTION_PLAY)) {
                    App.Log("On Action play");
                    Intent serviceIntent = new Intent(MainActivity.this, NewMusicService.class);
                    bindService(serviceIntent, serviceConnectionForMusic, 0);
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
                    App.Log("On Action next");
                    BaseMusic music = intent.getParcelableExtra("music");
                    setSongDescriptions(music);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    handler.removeCallbacks(musicPositionRunnable);
                }

                if (Objects.equals(action, NewMusicService.ACTION_PREVIOUS)) {
                    App.Log("On Action previous");
                    BaseMusic music = intent.getParcelableExtra("music");
                    setSongDescriptions(music);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    handler.removeCallbacks(musicPositionRunnable);
                }

                if (Objects.equals(action, NewMusicService.ACTION_BEGIN_PLAYING)) {
                    App.Log("On Action begin playing");
                    handler.removeCallbacks(musicPositionRunnable);
                    setPauseButtons();
                    setPlaybackPosition(0);
                    handler.post(musicPositionRunnable);
                }

                if (Objects.equals(action, NewMusicService.ACTION_CLOSE)) {
                    App.Log("on Action close");
                    handler.removeCallbacks(musicPositionRunnable);
                    hidePlayerPanel();
                }

            }
        };
    }

    private void changePlayingState() {
        if (isBoundedMusic) {
            if (newMusicService.isPlaying()) {
                App.Log("change playing");
                handler.post(musicPositionRunnable);
                setPauseButtons();
            } else {
                App.Log("change not playing");
                handler.removeCallbacks(musicPositionRunnable);
                setPlayButtons();
            }
        }
    }

    private void changeRepeatButtonState(boolean isLooping) {
        if (isLooping) {
            imageButtonRepeat.setBackground(ContextCompat.getDrawable(this,
                    R.drawable.button_repeat_activated));
            imageButtonRepeat.setColorFilter(ContextCompat.getColor(
                    this,
                    android.R.color.white),
                    PorterDuff.Mode.SRC_IN);
        } else {
            imageButtonRepeat.setBackground(ContextCompat.getDrawable(this,
                    R.drawable.button_repeat_not_activated));
            imageButtonRepeat.setColorFilter(ContextCompat.getColor(
                    this,
                    R.color.colorMain),
                    PorterDuff.Mode.SRC_IN);
        }
    }

    private void changeShuffleButtonState(boolean isShuffling) {
        if (isShuffling) {
            imageButtonShuffle.setBackground(ContextCompat.getDrawable(this,
                    R.drawable.button_repeat_activated));
            imageButtonShuffle.setColorFilter(ContextCompat.getColor(
                    this,
                    android.R.color.white),
                    PorterDuff.Mode.SRC_IN);
        } else {
            imageButtonShuffle.setBackground(ContextCompat.getDrawable(this,
                    R.drawable.button_repeat_not_activated));
            imageButtonShuffle.setColorFilter(ContextCompat.getColor(
                    this,
                    R.color.colorMain),
                    PorterDuff.Mode.SRC_IN);
        }
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

    public void setAlbumImage(String url) {
        if (url == null) {
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
    protected void onResume() {
        super.onResume();
        Intent serviceIntent = new Intent(this, NewMusicService.class);
        bindService(serviceIntent, serviceConnectionForMusic, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isBoundedMusic) return;
        unbindService(serviceConnectionForMusic);
        isBoundedMusic = false;
        handler.removeCallbacks(musicPositionRunnable);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_my_music:
                viewPager.setCurrentItem(0, true);
                break;
            case R.id.action_settings:
                viewPager.setCurrentItem(1, true);
                break;
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
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
