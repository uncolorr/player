package com.example.uncolor.vkmusic.main_activity.my_music_fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.example.uncolor.vkmusic.Apis.request_bodies.SearchVkMusicBody;
import com.example.uncolor.vkmusic.Apis.response_models.CaptchaErrorResponse;
import com.example.uncolor.vkmusic.CaptchaDialog;
import com.example.uncolor.vkmusic.IntentFilterManager;
import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.application.App;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.models.VkMusic;
import com.example.uncolor.vkmusic.music_adapter.MusicAdapter;
import com.example.uncolor.vkmusic.services.MusicService;
import com.example.uncolor.vkmusic.services.download.DownloadService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Uncolor on 24.08.2018.
 */

@EFragment(R.layout.fragment_my_music)
public class VkMusicFragment extends Fragment implements VkMusicFragmentContract.View {

    @ViewById
    RecyclerView recyclerViewMusic;

    @ViewById
    ProgressBar progressBar;

    @ViewById
    EditText editTextSearch;

    @ViewById
    RadioButton radioButtonAllMusic;

    @ViewById
    RadioButton radioButtonDownloadedMusic;

    private MusicAdapter<VkMusic> musicAdapter;

    private VkMusicFragmentContract.Presenter presenter;

    private GetVkMusicBody getVkMusicBody;

    private SearchVkMusicBody searchVkMusicBody;

    private BroadcastReceiver musicReceiver;

    private Realm realm;

    private Runnable searchRunnable;

    private CaptchaDialog captchaDialog;

    @AfterViews
    void init() {
        realm = Realm.getDefaultInstance();
        radioButtonAllMusic.setChecked(true);
        getVkMusicBody = new GetVkMusicBody();
        searchVkMusicBody = new SearchVkMusicBody();
        presenter = new VkMusicFragmentPresenter(getContext(), this);
        musicAdapter = new MusicAdapter<>(presenter);
        recyclerViewMusic.setAdapter(musicAdapter);
        recyclerViewMusic.setLayoutManager(
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.VERTICAL,
                        false));
        presenter.onLoadMusic(getVkMusicBody, true);
        musicReceiver = getMusicReceiver();
        if (getContext() != null) {
            getContext().registerReceiver(musicReceiver, IntentFilterManager.getMusicIntentFilter());
        }
        editTextSearch.addTextChangedListener(getTempTextWatcher());
        searchRunnable = getSearchRunnable();
    }

    private Runnable getSearchRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                if(searchVkMusicBody.getQ().length() == 0){
                    switch (musicAdapter.getMode()){
                        case MusicAdapter.MODE_CACHE:
                            presenter.onSearchMusic(searchVkMusicBody,
                                    musicAdapter.getMode(),
                                    false);
                            break;
                        case MusicAdapter.MODE_ALL_MUSIC:
                            presenter.onLoadMusic(getVkMusicBody, true);
                            break;
                    }
                }
                else {
                    presenter.onSearchMusic(searchVkMusicBody,
                            musicAdapter.getMode(),
                            false);
                }
            }
        };
    }

    private TextWatcher getTempTextWatcher() {
        return new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence q, int start, int before, int count) {
                searchVkMusicBody.setQ(q);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            private Timer timer = new Timer();
            private final long DELAY = 500; // milliseconds

            @Override
            public void afterTextChanged(final Editable s) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                if(getActivity() != null){
                                    getActivity().runOnUiThread(searchRunnable);
                                }
                            }
                        }, DELAY
                );
            }
        };

    }


    @Click(R.id.radioButtonAllMusic)
    void onAllMusicTabClick() {
        musicAdapter.setMode(MusicAdapter.MODE_ALL_MUSIC);
        musicAdapter.clear();
        presenter.onLoadMusic(getVkMusicBody, true);
    }

    @Click(R.id.radioButtonDownloadedMusic)
    void onDownloadedMusicTabClick() {
        realm.beginTransaction();
        RealmResults<VkMusic> results = realm.where(VkMusic.class).findAll();
        musicAdapter.setMode(MusicAdapter.MODE_CACHE);
        musicAdapter.clear();
        musicAdapter.add(results);
        if (realm.isInTransaction()) {
            realm.commitTransaction();
        }
    }

    private BroadcastReceiver getMusicReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                App.Log("on fragment receive");
                String action = intent.getAction();
                if (Objects.equals(action, DownloadService.ACTION_DOWNLOAD_STARTED)) {
                    BaseMusic music = intent.getParcelableExtra(DownloadService.ARG_MUSIC);
                    musicAdapter.startDownloadMusic(music);
                } else if (Objects.equals(action, DownloadService.ACTION_DOWNLOAD_COMPLETED)) {
                    BaseMusic music = intent.getParcelableExtra(DownloadService.ARG_MUSIC);
                    musicAdapter.completeDownloadMusic(music);
                } else if (Objects.equals(action, MusicService.ACTION_CLOSE)) {
                    musicAdapter.unselectCurrentTrack();
                } else {
                    BaseMusic music = intent.getParcelableExtra(MusicService.ARG_MUSIC);
                    if (music == null) {
                        return;
                    }
                    musicAdapter.changeCurrentMusic(music);
                }
            }
        };
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setMusicItems(List<VkMusic> items, boolean isRefreshing) {
        if (isRefreshing) {
            musicAdapter.clear();
        }
        musicAdapter.add(items);
    }

    @Override
    public void deleteMusic(VkMusic music, int position) {
        musicAdapter.deleteTrack(music, position);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getContext() != null) {
            getContext().unregisterReceiver(musicReceiver);
        }
    }

    @Override
    public void showErrorToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showCaptchaDialog(CaptchaErrorResponse captchaErrorResponse) {
        captchaDialog = new CaptchaDialog(getContext(), captchaErrorResponse);
        captchaDialog.setOnSendClickListener(getOnSendCaptchaClickListener(captchaErrorResponse));
        captchaDialog.show();

    }

    @Override
    public void setAlbumImageForMusic(String url, int position) {
        musicAdapter.setAlbumImageUrl(url, position);
    }

    private View.OnClickListener getOnSendCaptchaClickListener(final CaptchaErrorResponse captchaErrorResponse) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!captchaDialog.getCaptcha().isEmpty()){
                    searchVkMusicBody.setCaptchaKey(captchaDialog.getCaptcha());
                    searchVkMusicBody.setCaptchaSid(captchaErrorResponse.getCaptchaSID());
                    presenter.onSearchMusic(searchVkMusicBody, musicAdapter.getMode(), true);
                    captchaDialog.dismiss();
                }
            }
        };
    }

    @Override
    public ArrayList<VkMusic> getMusic() {
        return (ArrayList<VkMusic>) musicAdapter.getItems();
    }

    @Override
    public Activity getViewActivity() {
        return getActivity();
    }
}
