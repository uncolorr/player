package com.comandante.uncolor.vkmusic.main_activity.main_music_fragment;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.comandante.uncolor.vkmusic.Apis.request_bodies.GetVkMusicBody;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.base_adapter.OnLoadMoreListener;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicFragment;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.temp_music_adapter.MusicAdapter;
import com.comandante.uncolor.vkmusic.utils.IntentFilterManager;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainMusicFragment extends BaseMusicFragment implements MainMusicFragmentContract.View,
        OnLoadMoreListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private MainMusicFragmentContract.Presenter presenter;

    private GetVkMusicBody vkMusicBody;

    private BroadcastReceiver musicReceiver;

    public static MainMusicFragment newInstance() {
        Bundle args = new Bundle();
        MainMusicFragment fragment = new MainMusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_main, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init(){
        presenter = new MainMusicFragmentPresenter(getContext(), this);
        initAdapter(presenter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(adapter.getScrollListener());
        adapter.setOnLoadMoreListener(this);
        recyclerView.setAdapter(adapter);

        vkMusicBody = new GetVkMusicBody();

        musicReceiver = presenter.getMusicBroadcastReceiver();
        Objects.requireNonNull(getActivity()).registerReceiver(musicReceiver,
                IntentFilterManager.getTempMusicIntentFilter());
        presenter.onLoadMusicByRequest(vkMusicBody, true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Objects.requireNonNull(getActivity()).unregisterReceiver(musicReceiver);
        super.onDestroy();
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
    public void addMusicItems(List<? extends BaseMusic> tracks, boolean isRefreshing) {
        if(isRefreshing){
            adapter.clear();
        }
        adapter.addList(tracks);
        adapter.setLoaded();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            adapter.checkCache(MusicAdapter.CHECK_FOR_PLAYLIST);
        }
    }


    @Override
    public void addLoadMoreItem() {
        adapter.addLoadMoreItem();
    }

    @Override
    public void removeLoadMoreItem() {
        adapter.removeLoadMoreItem();
    }

    @Override
    public void selectCurrentTrack(BaseMusic music) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoadMore() {
        vkMusicBody.setOffset(adapter.getMusicItemsCount());
        presenter.onLoadMusicByRequest(vkMusicBody, false);
    }
}
