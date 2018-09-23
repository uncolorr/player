package com.comandante.uncolor.vkmusic.auth_activity.music_fragment;

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

import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetMusicRequestBody;
import com.comandante.uncolor.vkmusic.IntentFilterManager;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.models.Music;
import com.comandante.uncolor.vkmusic.music_adapter.MusicAdapter;
import com.comandante.uncolor.vkmusic.services.MusicService;
import com.flurry.android.FlurryAgent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Uncolor on 04.09.2018.
 */

@EFragment(R.layout.fragment_music)
public class MusicFragment extends Fragment implements MusicFragmentContract.View {

    @ViewById
    ProgressBar progressBar;

    @ViewById
    RecyclerView recyclerViewMusic;

    @ViewById
    EditText editTextSearch;

    private MusicFragmentContract.Presenter presenter;

    private MusicAdapter<Music> musicAdapter;

    private GetMusicRequestBody getMusicRequestBody;

    private BroadcastReceiver musicReceiver;

    private Runnable searchRunnable;

    @AfterViews
    void init() {
        getMusicRequestBody = new GetMusicRequestBody();
        presenter = new MusicFragmentPresenter(getContext(), this);
        musicAdapter = new MusicAdapter<>(presenter);
        recyclerViewMusic.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        recyclerViewMusic.setAdapter(musicAdapter);
        presenter.onLoadMusic(getMusicRequestBody, true);
        musicReceiver = getMusicReceiver();
        searchRunnable = getSearchRunnable();
        editTextSearch.addTextChangedListener(getSearchTextWatcher());
        getContext().registerReceiver(musicReceiver, IntentFilterManager.getMusicIntentFilter());
    }

    private BroadcastReceiver getMusicReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                App.Log("on fragment receive");
                String action = intent.getAction();
                if (Objects.equals(action, MusicService.ACTION_CLOSE)) {
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

    private TextWatcher getSearchTextWatcher() {
        return new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence q, int start, int before, int count) {
                getMusicRequestBody.setQuery(q.toString());
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

    private Runnable getSearchRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                FlurryAgent.logEvent(getContext().getString(R.string.log_search_before_auth));
                presenter.onLoadMusic(getMusicRequestBody, true);
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
    public ArrayList<Music> getMusic() {
        return (ArrayList<Music>) musicAdapter.getItems();
    }

    @Override
    public void setAlbumImageForMusic(String url, int position) {
        musicAdapter.setAlbumImageUrl(url, position);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(musicReceiver);
    }

    @Override
    public void setMusicItems(List<Music> items, boolean isRefreshing) {
        if (isRefreshing) {
            musicAdapter.clear();
        }
        musicAdapter.add(items);
    }
}
