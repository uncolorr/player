package com.comandante.uncolor.vkmusic.main_activity.downloaded_fragment;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicFragment;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.temp_music_adapter.MusicAdapter;
import com.comandante.uncolor.vkmusic.utils.IntentFilterManager;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadedFragment extends BaseMusicFragment implements DownloadedFragmentContract.View{

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private BroadcastReceiver musicReceiver;

    private DownloadedFragmentContract.Presenter presenter;

    public static DownloadedFragment newInstance() {

        Bundle args = new Bundle();

        DownloadedFragment fragment = new DownloadedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloaded, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        presenter = new DownloadedFragmentPresenter(getContext(), this);
        initAdapter(presenter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        musicReceiver = presenter.getMusicBroadcastReceiver();
        Objects.requireNonNull(getContext())
                .registerReceiver(musicReceiver,
                        IntentFilterManager.getTempMusicIntentFilter());
        loadMusicFromDatabase();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void loadMusicFromDatabase(){
        presenter.onLoadMusicFromDatabase();
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void deleteTrack(BaseMusic music) {
        App.Log("DownloadFragment deleteTrack");
        adapter.remove(music, MusicAdapter.REMOVE_MODE_DESTROY);
    }

    @Override
    public void completeDownloading(BaseMusic music) {
        adapter.add(music);
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void cancelDownloading(BaseMusic music) {

    }

    @Override
    public void startDownloading(BaseMusic music) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        App.Log("Downloaded fragment onHiddenChanged hidden: " + hidden);
        super.onHiddenChanged(hidden);
        if(!hidden){
            loadMusicFromDatabase();
        }
    }

    @Override
    public void addMusicItems(List<? extends BaseMusic> tracks, boolean isRefreshing) {
        adapter.clear();
        adapter.addList(tracks, MusicAdapter.CHECK_FOR_DATABASE);
        adapter.reverse();
    }

    @Override
    public void selectCurrentTrack(BaseMusic music) {
        adapter.selectCurrentTrack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getContext()).unregisterReceiver(musicReceiver);
    }
}