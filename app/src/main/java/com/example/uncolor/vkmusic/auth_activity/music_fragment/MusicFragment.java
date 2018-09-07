package com.example.uncolor.vkmusic.auth_activity.music_fragment;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.example.uncolor.vkmusic.Apis.request_bodies.GetMusicRequestBody;
import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.models.Music;
import com.example.uncolor.vkmusic.music_adapter.MusicAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Uncolor on 04.09.2018.
 */

@EFragment(R.layout.fragment_music)
public class MusicFragment extends Fragment implements MusicFragmentContract.View {

    @ViewById
    ProgressBar progressBar;

    @ViewById
    RecyclerView recyclerViewMusic;

    private MusicFragmentContract.Presenter presenter;

    private MusicAdapter<Music> adapter;

    private GetMusicRequestBody getMusicRequestBody;

    @AfterViews
    void init(){
        getMusicRequestBody = new GetMusicRequestBody();
        presenter = new MusicFragmentPresenter(getContext(),  this);
        adapter = new MusicAdapter<>(presenter);
        recyclerViewMusic.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        recyclerViewMusic.setAdapter(adapter);
        presenter.onLoadMusic(getMusicRequestBody, true);
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
    public void setMusicItems(List<Music> items, boolean isRefreshing) {
        if(isRefreshing){
            adapter.clear();
        }
        adapter.add(items);
    }
}
