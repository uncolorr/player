package com.example.uncolor.vkmusic.music_adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.uncolor.vkmusic.R;
import com.example.uncolor.vkmusic.application.App;
import com.example.uncolor.vkmusic.auth_activity.music_fragment.BaseMusicFragmentPresenter;
import com.example.uncolor.vkmusic.models.BaseMusic;
import com.example.uncolor.vkmusic.models.VkMusic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Uncolor on 24.08.2018.
 */

public class MusicAdapter<T extends BaseMusic> extends RecyclerView.Adapter {

    public static final int MODE_CACHE = 1;
    public static final int MODE_ALL_MUSIC = 2;

    private List<T> items = new ArrayList<>();
    private BaseMusicFragmentPresenter presenter;
    private BaseMusic currentMusic;
    private Realm realm;
    private int mode;

    public MusicAdapter(BaseMusicFragmentPresenter presenter) {
        this.presenter = presenter;
        realm = Realm.getDefaultInstance();
        this.mode = MODE_ALL_MUSIC;
    }

    public void add(List<T> musics) {
        this.items.addAll(musics);
        checkCache(musics);
        if(mode == MODE_CACHE){
            Collections.reverse(this.items);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        this.items.clear();
        notifyDataSetChanged();
    }

    public void changeCurrentMusic(BaseMusic music) {
        this.currentMusic = music;
        notifyDataSetChanged();
    }

    public void startDownloadMusic(BaseMusic music) {
        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(items.get(i).getDownload(), music.getDownload())) {
                items.get(i).setState(BaseMusic.STATE_DOWNLOADING);
                notifyItemChanged(i);
            }
        }
    }

    public void setAlbumImageUrl(String url, int position){
        items.get(position).setAlbumImageUrl(url);
        notifyItemChanged(position);
    }

    public void completeDownloadMusic(BaseMusic music) {
        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(items.get(i).getDownload(), music.getDownload())) {
                items.get(i).setState(BaseMusic.STATE_COMPLETED);
                items.get(i).setLocalPath(music.getLocalPath());
                if (music instanceof VkMusic) {
                    VkMusic vkMusic = (VkMusic) items.get(i);
                    realm.beginTransaction();
                    realm.copyToRealm(vkMusic);
                    realm.commitTransaction();
                }
                notifyItemChanged(i);
            }
        }
    }

    private void checkCache(List<T> musics) {
        if (!items.isEmpty()) {
            if (items.get(0) instanceof VkMusic) {
                if(!realm.isInTransaction()) {
                    realm.beginTransaction();
                }
                RealmResults<VkMusic> results = realm.where(VkMusic.class).findAll();
                for (int i = 0; i < musics.size(); i++) {
                    if (results.contains(musics.get(i))) {
                        VkMusic music = realm.where(VkMusic.class)
                                .equalTo("id", musics.get(i).getId())
                                .findFirst();
                        if (music != null) {
                            if (isFileExists(music.getLocalPath())) {
                                musics.get(i).setLocalPath(music.getLocalPath());
                                musics.get(i).setState(BaseMusic.STATE_COMPLETED);
                            }
                            else {
                                music.deleteFromRealm();
                                this.items.remove(i);
                            }
                        }
                    }
                }
                realm.commitTransaction();
            }
        }
    }

    private boolean isFileExists(String localPath) {
        File file = new File(localPath);
        return file.exists();
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
        if (holder instanceof MusicViewHolder) {
            MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
            musicViewHolder.bind(items.get(position), currentMusic);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<T> getItems() {
        return items;
    }

    public void deleteTrack(BaseMusic music, int position) {
        if (music instanceof VkMusic) {
            File file = new File(music.getLocalPath());
            if (file.exists()) {
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    realm.beginTransaction();
                    RealmResults<VkMusic> results = realm.where(VkMusic.class)
                            .equalTo("url", music.getDownload())
                            .findAll();
                    results.deleteAllFromRealm();
                    realm.commitTransaction();
                    if(mode == MODE_CACHE){
                        items.remove(position);
                        notifyItemRemoved(position);
                    }
                    else if(mode == MODE_ALL_MUSIC){
                        items.get(position).setState(BaseMusic.STATE_DEFAULT);
                        notifyItemChanged(position);
                    }
                }
            }
        }
    }

    public void unselectCurrentTrack() {
        currentMusic = null;
        notifyDataSetChanged();
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return this.mode;
    }

}
