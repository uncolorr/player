package com.example.uncolor.vkmusic.music_adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.auth_activity.music_fragment.BaseMusicFragmentPresenter;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.models.Music;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Uncolor on 24.08.2018.
 */

public class MusicAdapter<T extends BaseMusic> extends RecyclerView.Adapter {

    private List<T> items = new ArrayList<>();
    private BaseMusicFragmentPresenter presenter;

    public MusicAdapter(BaseMusicFragmentPresenter presenter) {
        this.presenter = presenter;
    }

    public void add(List<T> musics) {
        this.items.addAll(musics);
        notifyDataSetChanged();
    }

    public void clear(){
        this.items.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.music_item, parent, false);
        return new MusicViewHolder(view, presenter);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MusicViewHolder){
            MusicViewHolder musicViewHolder = (MusicViewHolder)holder;
            musicViewHolder.bind(items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<T> getItems() {
        return items;
    }
}
