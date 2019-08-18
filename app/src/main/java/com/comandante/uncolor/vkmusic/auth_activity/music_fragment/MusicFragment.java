package com.comandante.uncolor.vkmusic.auth_activity.music_fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetMusicRequestBody;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.models.Music;
import com.comandante.uncolor.vkmusic.music_adapter.MusicAdapter;
import com.comandante.uncolor.vkmusic.services.music.NewMusicService;
import com.comandante.uncolor.vkmusic.utils.IntentFilterManager;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Uncolor on 04.09.2018.
 */


public class MusicFragment extends Fragment implements MusicFragmentContract.View {

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.recyclerViewMusic)
    RecyclerView recyclerViewMusic;

    @BindView(R.id.editTextSearch)
    EditText editTextSearch;



    private MusicFragmentContract.Presenter presenter;

    private MusicAdapter<Music> musicAdapter;

    private GetMusicRequestBody getMusicRequestBody;

    private BroadcastReceiver musicReceiver;

    private Runnable searchRunnable;

    public static MusicFragment newInstance() {

        Bundle args = new Bundle();

        MusicFragment fragment = new MusicFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;

    }

    private void init() {
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
        App.getContext().registerReceiver(musicReceiver, IntentFilterManager.getMusicIntentFilter());
    }

    private BroadcastReceiver getMusicReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                App.Log("on fragment receive");
                String action = intent.getAction();
                if (Objects.equals(action, NewMusicService.ACTION_CLOSE)) {
                    musicAdapter.unselectCurrentTrack();
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
      App.getContext().unregisterReceiver(musicReceiver);
    }

    @Override
    public void setMusicItems(List<Music> items, boolean isRefreshing) {
        if (isRefreshing) {
            musicAdapter.clear();
        }
        musicAdapter.add(items);
    }

    @Override
    public void showErrorToast(String message) {
        Toast.makeText(getContext(), message,  Toast.LENGTH_LONG).show();
    }
}
