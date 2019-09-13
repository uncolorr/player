package com.comandante.uncolor.vkmusic.main_activity.base_music_fragment;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.services.music.MusicService;
import com.comandante.uncolor.vkmusic.services.music.PlaylistRepository;
import com.comandante.uncolor.vkmusic.temp_music_adapter.MusicAdapter;

import java.util.ArrayList;

public abstract class BaseMusicFragment extends Fragment implements BaseMusicViewInterface {

    protected MusicAdapter adapter;

    protected RecyclerView.LayoutManager layoutManager;

    public BaseMusicFragment() {
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
    }

    public void initAdapter(BaseMusicPresenterInterface presenter){
        adapter = new MusicAdapter(getContext(), presenter);
    }

    @Override
    public Activity getViewActivity(){
        return getActivity();
    }

    @Override
    public void startPlayingTrack(BaseMusic music) {
        if(getContext() == null){
            return;
        }

        ArrayList<BaseMusic> musicItems = adapter.getMusicItems();
        int position = musicItems.indexOf(music);
        if(position < 0){
            return;
        }
        PlaylistRepository.get().setPlaylist(musicItems);
        PlaylistRepository.get().setCurrentMusic(music);
        PlaylistRepository.get().setPosition(position);
        Intent intent = new Intent(getContext(), MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY);
        intent.putExtra(MusicService.ARG_MUSIC, music);
        getContext().startService(intent);
    }

    @Override
    public void deleteTrack(BaseMusic music) {
        App.Log("BaseMusicFragment deleteTrack");
        if(adapter == null){
            return;
        }
        adapter.remove(music, MusicAdapter.REMOVE_MODE_CHANGE_STATE);
    }

    @Override
    public void startDownloading(BaseMusic music) {
        if(adapter == null){
            return;
        }
        adapter.setState(music, BaseMusic.STATE_DOWNLOADING);
    }

    @Override
    public void completeDownloading(BaseMusic music) {
        if(adapter == null){
            return;
        }
        adapter.setState(music, BaseMusic.STATE_COMPLETED);
    }

    @Override
    public void cancelDownloading(BaseMusic music) {
        if(adapter == null){
            return;
        }
        adapter.setState(music, BaseMusic.STATE_DEFAULT);
    }
}
