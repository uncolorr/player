package com.comandante.uncolor.vkmusic.main_activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.main_activity.downloaded_fragment.DownloadedFragment;
import com.comandante.uncolor.vkmusic.main_activity.main_music_fragment.MainMusicFragment;
import com.comandante.uncolor.vkmusic.main_activity.search_music_fragment.SearchMusicFragment;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.services.music.PlaylistRepository;
import com.comandante.uncolor.vkmusic.services.music.MusicService;
import com.comandante.uncolor.vkmusic.utils.DurationConverter;
import com.comandante.uncolor.vkmusic.utils.IntentFilterManager;
import com.comandante.uncolor.vkmusic.widgets.SquareImageView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.makeramen.roundedimageview.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        SeekBar.OnSeekBarChangeListener, MainActivityContract.View {

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

    private MainMusicFragment mainMusicFragment;
    private SearchMusicFragment searchMusicFragment;
    private DownloadedFragment downloadedFragment;
    private FragmentManager fm = getSupportFragmentManager();
    private Fragment activeFragment;

    private BottomSheetBehavior sheetBehavior;

    private BroadcastReceiver musicReceiver;

    private MusicService newMusicService;

    private boolean isBoundedMusic = false;

    private MainActivityContract.Presenter presenter;

    private Handler handler = new Handler();

    private ProgressUpdateRunnable progressUpdateRunnable = new ProgressUpdateRunnable();

    ServiceConnection serviceConnectionForMusic = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            App.Log("onServiceConnected");
            newMusicService = ((MusicService.TempMusicBinder) service).getService();
            isBoundedMusic = true;
            if(newMusicService.isPlaying()) {
                presenter.onBindPlayerState(PlaylistRepository.get().getCurrentMusic(),
                        newMusicService.isLooping(),
                        newMusicService.isShuffling());
                startProgressUpdate();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            App.Log("onServiceDisconnected");
            isBoundedMusic = false;
        }
    };


    public static Intent getInstance(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        presenter = new MainActivityPresenter(this, this);
        sheetBehavior = BottomSheetBehavior.from(bigPlayerPanel);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        initFragments();
        musicReceiver = presenter.getTempMusicReceiver();
        registerReceiver(musicReceiver, IntentFilterManager.getTempMusicIntentFilter());
        checkServiceConnection();
    }

    private void initFragments(){
        mainMusicFragment = MainMusicFragment.newInstance();
        activeFragment = mainMusicFragment;
        fm.beginTransaction().add(R.id.main_container, mainMusicFragment, "MF")
                .commit();
    }

    @OnClick(R.id.playerPanel)
    void onPlayerPanelClick() {
        showPlayerPanel();
    }

    @OnClick(R.id.imageButtonHide)
    void onImageButtonHideClick() {
        hidePlayerPanel();
    }

    @OnClick(R.id.imageButtonRepeat)
    void onRepeatButtonClick() {
        boolean isLooping = newMusicService.isLooping();
        newMusicService.setLooping(!isLooping);
        setLoopingState(!isLooping);
    }

    @OnClick(R.id.imageButtonShuffle)
    void onShuffleButtonClick() {
        boolean isShuffling = newMusicService.isShuffling();
        if (isShuffling) {
            newMusicService.setShuffle(false);
        } else {
            newMusicService.setShuffle(true);
        }
        setShufflingState(!isShuffling);
    }


    @OnClick({R.id.imageButtonPanelPlay, R.id.imageButtonPlayerPlay})
    void onPlayButtonClick() {
        if (isBoundedMusic) {
            if(newMusicService.isPreparing()){
                return;
            }
            if (newMusicService.isPlaying()) {
                newMusicService.pause();
                presenter.onPauseTrack();
            } else {
                newMusicService.resume();
                presenter.onResumeTrack();
            }
        }
    }

    @OnClick({R.id.imageButtonPanelNext, R.id.imageButtonPlayerNext})
    void onNextButtonClick() {
        if (isBoundedMusic) {
            BaseMusic music = newMusicService.next(true);
            if(music != null) {
                presenter.onSwitchTrack(MusicService.ACTION_NEXT, music);
            }
        }
    }

    @OnClick(R.id.imageButtonPlayerPrevious)
    void onPreviousButtonClick() {
        if (isBoundedMusic) {
            BaseMusic music = newMusicService.previous();
            presenter.onSwitchTrack(MusicService.ACTION_PREVIOUS, music);
        }
    }

    private boolean isPlayerPanelHidden() {
        return sheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN;
    }

    @Override
    public void showPlayerPanel() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void hidePlayerPanel() {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public void showPlayerBar() {
        progressBarMusic.setVisibility(View.VISIBLE);
        playerPanel.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePlayerBar() {
        progressBarMusic.setVisibility(View.GONE);
        playerPanel.setVisibility(View.GONE);
    }

    @Override
    public void setPlayButtons() {
        imageButtonPanelPlay.setImageResource(R.drawable.play);
        imageButtonPlayerPlay.setImageResource(R.drawable.play);
    }

    @Override
    public void setPauseButtons() {
        imageButtonPanelPlay.setImageResource(R.drawable.pause);
        imageButtonPlayerPlay.setImageResource(R.drawable.pause);
    }

    @Override
    public void setSongDescriptions(BaseMusic music) {
        textViewPanelSongTitle.setText(music.getTitle());
        textViewPanelArtist.setText(music.getArtist());
        textViewPlayerSongTitle.setText(music.getTitle());
        textViewPlayerArtist.setText(music.getArtist());
        textViewDuration.setText(DurationConverter.getDurationFormat(music.getDuration()));
        seekBar.setMax(music.getDuration());
        progressBarMusic.setMax(music.getDuration());
    }

    @Override
    public void setLoopingState(boolean isLooping) {
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

    @Override
    public void setShufflingState(boolean isShuffling) {
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

    @Override
    public void updateMusicPosition() {
        if(isBoundedMusic){
           int position = newMusicService.getCurrentPlaybackPosition();
           textViewCurrentPosition.setText(DurationConverter.getDurationFormat(position));
           seekTo(position);
        }
    }

    @Override
    public void startProgressUpdate() {
        App.Log("startProgressUpdate");
        handler.post(progressUpdateRunnable);
    }

    @Override
    public void pauseProgressUpdate() {
        App.Log("pauseProgressUpdate");
        handler.removeCallbacks(progressUpdateRunnable);
    }

    @Override
    public void resumeProgressUpdate() {
        App.Log("resumeProgressUpdate");
        handler.post(progressUpdateRunnable);
    }

    @Override
    public void interruptProgressUpdate() {
        App.Log("interruptProgressUpdate");
        handler.removeCallbacks(progressUpdateRunnable);
        progressUpdateRunnable = null;
        handler = null;
    }

    @Override
    public void broadcastSwitchedTrack(String action, BaseMusic music) {
        Intent musicIntent = new Intent(action);
        musicIntent.putExtra(MusicService.ARG_MUSIC, music);
        sendBroadcast(musicIntent);
    }

    @Override
    public void checkServiceConnection() {
        if(isBoundedMusic){
            return;
        }
        Intent serviceIntent = new Intent(this, MusicService.class);
        bindService(serviceIntent, serviceConnectionForMusic, 0);
    }

    private void seekTo(int playbackPosition) {
        progressBarMusic.setProgress(playbackPosition);
        seekBar.setProgress(playbackPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkServiceConnection();
        startProgressUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isBoundedMusic) return;
        pauseProgressUpdate();
    }


    @Override
    public void onBackPressed() {
        if (!isPlayerPanelHidden()) {
            hidePlayerPanel();
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnectionForMusic);
        unregisterReceiver(musicReceiver);
        interruptProgressUpdate();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if(isBoundedMusic) {
                newMusicService.seekTo(progress);
                updateMusicPosition();
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_my_music:
                if(mainMusicFragment == null){
                    mainMusicFragment = MainMusicFragment.newInstance();
                }
                if(activeFragment == mainMusicFragment){
                    return true;
                }
                fm.beginTransaction().hide(activeFragment).show(mainMusicFragment).commit();
                activeFragment = mainMusicFragment;
                return true;

            case R.id.action_search:
                if(searchMusicFragment == null){
                    searchMusicFragment = SearchMusicFragment.newInstance();
                    fm.beginTransaction().add(R.id.main_container, searchMusicFragment, "SF")
                            .hide(searchMusicFragment).commit();
                }
                if(activeFragment == searchMusicFragment){
                    return true;
                }
                fm.beginTransaction().hide(activeFragment).show(searchMusicFragment).commit();
                activeFragment = searchMusicFragment;
                return true;

            case R.id.action_downloaded:
                if(downloadedFragment == null){
                    downloadedFragment = DownloadedFragment.newInstance();
                    fm.beginTransaction().add(R.id.main_container, downloadedFragment, "DF")
                            .hide(downloadedFragment).commit();
                }
                if(activeFragment == downloadedFragment){
                    return true;
                }
                fm.beginTransaction().hide(activeFragment).show(downloadedFragment).commit();
                activeFragment = downloadedFragment;
                return true;
        }
        return false;
    }

    private class ProgressUpdateRunnable implements Runnable{
        @Override
        public void run() {
            MainActivity.this.updateMusicPosition();
            handler.postDelayed(this, 1000);
        }
    }
}
