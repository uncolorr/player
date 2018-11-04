package com.comandante.uncolor.vkmusic.main_activity.my_music_fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.comandante.uncolor.vkmusic.Apis.request_bodies.SearchVkMusicBody;
import com.comandante.uncolor.vkmusic.Apis.response_models.CaptchaErrorResponse;
import com.comandante.uncolor.vkmusic.services.music.NewMusicService;
import com.comandante.uncolor.vkmusic.utils.LoadingDialog;
import com.comandante.uncolor.vkmusic.utils.MessageReporter;
import com.comandante.uncolor.vkmusic.widgets.CaptchaDialog;
import com.comandante.uncolor.vkmusic.utils.IntentFilterManager;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.main_activity.settings_fragment.SettingsFragment;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.models.VkMusic;
import com.comandante.uncolor.vkmusic.music_adapter.MusicAdapter;
import com.comandante.uncolor.vkmusic.music_adapter.OnLoadMoreListener;
import com.comandante.uncolor.vkmusic.services.download.DownloadService;
import com.comandante.uncolor.vkmusic.widgets.ResignInDialog;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
    AdView adView;

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

    @ViewById
    LinearLayout linearLayoutFailure;

    private MusicAdapter<VkMusic> musicAdapter;

    private VkMusicFragmentContract.Presenter presenter;

    private GetVkMusicBody getVkMusicBody;

    private SearchVkMusicBody searchVkMusicBody;

    private BroadcastReceiver musicReceiver;

    private Realm realm;

    private Runnable searchRunnable;

    private CaptchaDialog captchaDialog;

    private ResignInDialog resignInDialog;

    private AlertDialog dialogProcessing;

    @AfterViews
    void init() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        resignInDialog = new ResignInDialog(getContext());
        resignInDialog.setOnSignInClickListener(getSignInClickListener());
        dialogProcessing = LoadingDialog.newInstanceWithoutCancelable(getContext(), LoadingDialog.LABEL_LOADING);
        realm = Realm.getDefaultInstance();
        radioButtonAllMusic.setChecked(true);
        getVkMusicBody = new GetVkMusicBody();
        searchVkMusicBody = new SearchVkMusicBody();
        presenter = new VkMusicFragmentPresenter(getContext(), this);
        musicAdapter = new MusicAdapter<>(presenter);
        musicAdapter.setOnLoadMoreListener(getOnLoadMoreListener());
        recyclerViewMusic.setAdapter(musicAdapter);
        recyclerViewMusic.setLayoutManager(
                new LinearLayoutManager(getContext(),
                        LinearLayoutManager.VERTICAL,
                        false));
        recyclerViewMusic.addOnScrollListener(musicAdapter.getScrollListener());
        musicReceiver = getMusicReceiver();
        if (getContext() != null) {
            getContext().registerReceiver(musicReceiver, IntentFilterManager.getMusicIntentFilter());
        }
        editTextSearch.addTextChangedListener(getTempTextWatcher());
        searchRunnable = getSearchRunnable();
        presenter.onLoadMusic(getVkMusicBody, true);
    }

    private View.OnClickListener getSignInClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onSignInButtonClick(resignInDialog.getLogin(),
                        resignInDialog.getPassword());
            }
        };
    }

    @Override
    public void showProcess() {
        dialogProcessing.show();
    }

    @Override
    public void hideProcess() {
        dialogProcessing.dismiss();
    }

    @Override
    public void showErrorMessage() {
        FlurryAgent.logEvent(getContext().getString(R.string.log_auth_failed));
        MessageReporter.showMessage(getContext(), "Ошибка", "Ошибка при авторизации");
    }

    private Runnable getSearchRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                FlurryAgent.logEvent(getContext().getString(R.string.log_search_after_auth));
                if (searchVkMusicBody.getQ().length() == 0) {
                    switch (musicAdapter.getMode()) {
                        case MusicAdapter.MODE_CACHE:
                            searchVkMusicBody.resetOffset();
                            presenter.onSearchMusic(searchVkMusicBody,
                                    musicAdapter.getMode(), false, true);
                            break;
                        case MusicAdapter.MODE_ALL_MUSIC:
                            getVkMusicBody = new GetVkMusicBody();
                            presenter.onLoadMusic(getVkMusicBody, true);
                            break;
                    }
                } else {
                    searchVkMusicBody.resetOffset();
                    presenter.onSearchMusic(searchVkMusicBody,
                            musicAdapter.getMode(), false, true);
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
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(searchRunnable);
                                }
                            }
                        }, DELAY);
            }
        };

    }


    @Click(R.id.radioButtonAllMusic)
    void onAllMusicTabClick() {
        if (musicAdapter.getMode() != MusicAdapter.MODE_ALL_MUSIC) {
            editTextSearch.getText().clear();
            musicAdapter.setMode(MusicAdapter.MODE_ALL_MUSIC);
            musicAdapter.clear();
            getVkMusicBody = new GetVkMusicBody();
            hideFailureMessage();
            presenter.onLoadMusic(getVkMusicBody, true);
        }
    }

    @Click(R.id.radioButtonDownloadedMusic)
    void onDownloadedMusicTabClick() {
        if (musicAdapter.getMode() != MusicAdapter.MODE_CACHE) {
            hideFailureMessage();
            editTextSearch.getText().clear();
            realm.beginTransaction();
            RealmResults<VkMusic> results = realm.where(VkMusic.class).findAll();
            musicAdapter.setMode(MusicAdapter.MODE_CACHE);
            musicAdapter.clear();
            musicAdapter.add(results);
            if (realm.isInTransaction()) {
                realm.commitTransaction();
            }
        }
    }

    @Click(R.id.buttonResignIn)
    void onResignInButtonClick(){
        resignInDialog.show();
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
                }else if (Objects.equals(action, DownloadService.ACTION_DOWNLOAD_FAILURE)) {
                    BaseMusic music = intent.getParcelableExtra(DownloadService.ARG_MUSIC);
                    musicAdapter.setDefaultMusicState(music);
                    showErrorToast("Ошибка при скачивании трека");
                } else if (Objects.equals(action, NewMusicService.ACTION_CLOSE)) {
                    musicAdapter.unselectCurrentTrack();
                } else if (Objects.equals(action, SettingsFragment.ACTION_CLEAR_CACHE)) {
                    musicAdapter.checkCache();
                } else {
                    BaseMusic music = intent.getParcelableExtra(NewMusicService.ARG_MUSIC);
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
        if (musicAdapter.getMusicItemsCount() == 0) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideProgress() {
        if(progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void addLoadMoreProgress() {
        musicAdapter.addLoadingItem();
    }

    @Override
    public void removeLoadMoreProgress() {
        musicAdapter.removeLoadingItem();
    }

    @Override
    public void showFailureMessage() {
        linearLayoutFailure.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideFailureMessage() {
        linearLayoutFailure.setVisibility(View.GONE);
        resignInDialog.clear();
    }

    @Override
    public void showReSignInDialog() {
        resignInDialog.show();
    }

    @Override
    public void hideReSignInDialog() {
        resignInDialog.dismiss();
    }

    @Override
    public void setMusicItems(List<VkMusic> items, boolean isRefreshing) {
        if (isRefreshing) {
            musicAdapter.clear();
        }
        musicAdapter.add(items);
        musicAdapter.setLoaded();
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
    public void showCaptchaDialog(CaptchaErrorResponse captchaErrorResponse, boolean isRefreshing) {
        captchaDialog = new CaptchaDialog(getContext(), captchaErrorResponse);
        captchaDialog.setOnSendClickListener(getOnSendCaptchaClickListener(captchaErrorResponse, isRefreshing));
        captchaDialog.show();
    }

    @Override
    public void setAlbumImageForMusic(String url, int position) {
        musicAdapter.setAlbumImageUrl(url, position);
    }

    private OnLoadMoreListener getOnLoadMoreListener() {
        return new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                App.Log("onLoadMore");
                if (editTextSearch.getText().toString().isEmpty()) {
                    getVkMusicBody.setOffset(musicAdapter.getMusicItemsCount());
                    presenter.onLoadMusic(getVkMusicBody, false);
                } else {
                    searchVkMusicBody.setOffset(musicAdapter.getMusicItemsCount());
                    presenter.onSearchMusic(searchVkMusicBody, musicAdapter.getMode(),
                            false, false);
                }
            }
        };
    }

    private View.OnClickListener getOnSendCaptchaClickListener(final CaptchaErrorResponse captchaErrorResponse,
                                                               final boolean isRefreshing) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!captchaDialog.getCaptcha().isEmpty()) {
                    searchVkMusicBody.setCaptchaKey(captchaDialog.getCaptcha());
                    searchVkMusicBody.setCaptchaSid(captchaErrorResponse.getCaptchaSID());
                    presenter.onSearchMusic(searchVkMusicBody, musicAdapter.getMode(),
                            true, isRefreshing);
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
