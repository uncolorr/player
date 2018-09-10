package com.example.uncolor.vkmusic.main_activity.my_music_fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.example.uncolor.vkmusic.IntentFilterManager;
import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.application.App;
import com.example.uncolor.vkmusic.application.AppPermissionManager;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.models.Music;
import com.example.uncolor.vkmusic.models.VkMusic;
import com.example.uncolor.vkmusic.music_adapter.MusicAdapter;
import com.example.uncolor.vkmusic.services.MusicService;
import com.example.uncolor.vkmusic.services.download.DownloadService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Uncolor on 24.08.2018.
 */

@EFragment(R.layout.fragment_my_music)
public class VkMusicFragment extends Fragment implements VkMusicFragmentContract.View {

    @ViewById
    RecyclerView recyclerViewMusic;

    @ViewById
    ProgressBar progressBar;

    private MusicAdapter<VkMusic> musicAdapter;

    private VkMusicFragmentContract.Presenter presenter;

    private GetVkMusicBody getVkMusicBody;

    private BroadcastReceiver musicReceiver;

    @AfterViews
    void init() {
        getVkMusicBody = new GetVkMusicBody();
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
    }

    private BroadcastReceiver getMusicReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Objects.equals(action, DownloadService.ACTION_DOWNLOAD_STARTED)) {
                    BaseMusic music = intent.getParcelableExtra(DownloadService.ARG_MUSIC);
                    musicAdapter.startDownloadMusic(music);
                }
                else if (Objects.equals(action, DownloadService.ACTION_DOWNLOAD_COMPLETED)) {
                    BaseMusic music = intent.getParcelableExtra(DownloadService.ARG_MUSIC);
                    musicAdapter.completeDownloadMusic(music);
                }
                else {
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
        boolean fromCache = true;
        musicAdapter.deleteTrack(music, position, fromCache);
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
    public ArrayList<VkMusic> getMusic() {
        return (ArrayList<VkMusic>) musicAdapter.getItems();
    }


    @Override
    public Activity getViewActivity() {
        return getActivity();
    }
}
