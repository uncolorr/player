package com.comandante.uncolor.vkmusic.main_activity.playlists_fragment;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.music_adapter.MusicAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Uncolor on 24.08.2018.
 */

@EFragment(R.layout.fragment_playlists)
public class PlaylistsFragment extends Fragment {

    @ViewById
    RecyclerView recyclerViewMusic;

    private MusicAdapter musicAdapter;

    @AfterViews
    void init(){

    }

}
