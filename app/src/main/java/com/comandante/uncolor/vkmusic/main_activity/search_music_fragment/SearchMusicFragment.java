package com.comandante.uncolor.vkmusic.main_activity.search_music_fragment;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.comandante.uncolor.vkmusic.Apis.request_bodies.SearchVkMusicBody;
import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.base_adapter.OnLoadMoreListener;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicFragment;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.music_adapter.MusicAdapter;
import com.comandante.uncolor.vkmusic.utils.IntentFilterManager;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchMusicFragment extends BaseMusicFragment implements SearchMusicContract.View, OnLoadMoreListener {

    @BindView(R.id.editTextSearch)
    EditText editTextSearch;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.recyclerViewSearch)
    RecyclerView recyclerViewSearch;

    @BindView(R.id.recyclerViewRecentRequests)
    RecyclerView recyclerViewRecentRequests;

    private SearchMusicContract.Presenter presenter;

    private BroadcastReceiver musicReceiver;

    private SearchVkMusicBody searchVkMusicBody;

    public static SearchMusicFragment newInstance() {
        Bundle args = new Bundle();
        SearchMusicFragment fragment = new SearchMusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_search, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        searchVkMusicBody = new SearchVkMusicBody();
        Objects.requireNonNull(getContext())
                .registerReceiver(musicReceiver, IntentFilterManager.getTempMusicIntentFilter());
        presenter = new SearchMusicPresenter(getContext(), this);
        initAdapter(presenter);
        editTextSearch.addTextChangedListener(new SearchTextWatcher());
        musicReceiver = presenter.getBroadcastReceiver();
        getContext().registerReceiver(musicReceiver, IntentFilterManager.getTempMusicIntentFilter());
        recyclerViewSearch.setLayoutManager(layoutManager);
        recyclerViewSearch.addOnScrollListener(adapter.getScrollListener());
        adapter.setOnLoadMoreListener(this);
        recyclerViewSearch.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(getContext() != null){
            getContext().unregisterReceiver(musicReceiver);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            adapter.checkCache(MusicAdapter.CHECK_FOR_PLAYLIST);
        }
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
    }

    @Override
    public void selectCurrentTrack(BaseMusic music) {
        adapter.selectCurrentTrack();
    }

    @Override
    public void removeLoadMoreItem() {
        adapter.removeLoadMoreItem();
    }

    @Override
    public void addLoadMoreItem() {
        adapter.addLoadMoreItem();
    }

    @Override
    public void onLoadMore() {
        searchVkMusicBody.setOffset(adapter.getMusicItemsCount());
        presenter.onSearchMusic(searchVkMusicBody, false);
    }

    private class SearchTextWatcher implements TextWatcher {

        private Timer timer = new Timer();
        private final long DELAY = 500;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            searchVkMusicBody.setQ(s);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            timer.cancel();
            timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(getSearchRunnable());
                            }
                        }
                    }, DELAY);
        }

        private Runnable getSearchRunnable(){
            return () -> {
                    adapter.clear();
                    searchVkMusicBody.resetOffset();
                    if(searchVkMusicBody.getQ().toString().isEmpty()){
                        return;
                    }
                    presenter.onSearchMusic(searchVkMusicBody, true);
            };
        }
    }
}
