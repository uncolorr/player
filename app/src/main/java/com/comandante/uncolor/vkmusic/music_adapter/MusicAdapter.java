package com.comandante.uncolor.vkmusic.music_adapter;

import android.content.Context;

import com.comandante.uncolor.vkmusic.application.App;
import com.comandante.uncolor.vkmusic.base_adapter.BaseAdapter;
import com.comandante.uncolor.vkmusic.base_adapter.ItemModel;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicPresenterInterface;
import com.comandante.uncolor.vkmusic.database.DatabaseManager;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.models.Loading;
import com.comandante.uncolor.vkmusic.models.VkMusic;
import com.comandante.uncolor.vkmusic.music_adapter.view_renders.LoadingViewRenderer;
import com.comandante.uncolor.vkmusic.music_adapter.view_renders.MusicViewRenderer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicAdapter extends BaseAdapter {

    public static final int REMOVE_MODE_CHANGE_STATE = 1;
    public static final int REMOVE_MODE_DESTROY = 2;

    public static final int CHECK_FOR_DATABASE = 3;
    public static final int CHECK_FOR_PLAYLIST = 4;

    private boolean isReversed = false;

    public MusicAdapter(Context context, BaseMusicPresenterInterface presenter) {
        registerRenderer(new MusicViewRenderer(VkMusic.TYPE, context, presenter));
        registerRenderer(new LoadingViewRenderer(Loading.TYPE, context));
    }

    @Override
    public void add(ItemModel item) {
        items.add(0, item);
        notifyItemInserted(0);
    }

    @Override
    public void addList(List<? extends ItemModel> items) {
        addList(items, CHECK_FOR_PLAYLIST);
    }


    public void addList(List<? extends ItemModel> items, int checkFor) {
        this.items.addAll(items);
        checkCache(checkFor);
        notifyDataSetChanged();
    }

    @Override
    public void remove(ItemModel itemModel) {
        remove(itemModel, REMOVE_MODE_DESTROY);
    }

    public void remove(ItemModel itemModel, int removeMode) {
        int index;

        index = items.indexOf(itemModel);
        if (index == -1) {
            return;
        }

        boolean isDeleted = DatabaseManager.get().removeTrack((VkMusic) itemModel);

        if (!isDeleted) {
            return;
        }

        switch (removeMode) {
            case REMOVE_MODE_CHANGE_STATE:
                ((BaseMusic) items.get(index)).setLocalPath(null);
                ((BaseMusic) items.get(index)).setState(BaseMusic.STATE_DEFAULT);
                notifyItemChanged(index);
                break;
            case REMOVE_MODE_DESTROY:
                items.remove(index);
                notifyItemRemoved(index);
                break;
        }
    }

    @Override
    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }


    public int getMusicItemsCount() {
        int counter = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof BaseMusic) {
                counter++;
            }
        }
        return counter;
    }

    public void addLoadMoreItem() {
        items.add(new Loading());
        notifyItemInserted(items.size() - 1);
    }

    public void removeLoadMoreItem() {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof Loading) {
                items.remove(items.get(i));
                notifyItemRemoved(i);
            }
        }
    }

    public ArrayList<BaseMusic> getMusicItems() {
        ArrayList<BaseMusic> musicItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof BaseMusic) {
                musicItems.add((BaseMusic) items.get(i));
            }
        }
        return musicItems;
    }

    public void selectCurrentTrack() {
        notifyDataSetChanged();
    }

    public void setState(BaseMusic music, int state) {
        int index;
        index = items.indexOf(music);
        if (index == -1) {
            return;
        }
        ((BaseMusic) items.get(index)).setState(state);
        ((BaseMusic) items.get(index)).setLocalPath(music.getLocalPath());
        notifyItemChanged(index);
    }

    public void reverse() {
        if (isReversed) {
            return;
        }
        isReversed = true;
        Collections.reverse(this.items);
    }

    private void checkFilesExists() {
        for (int i = 0; i < items.size(); i++) {
            if (!(items.get(i) instanceof VkMusic)) {
                continue;
            }
            VkMusic music = (VkMusic) items.get(i);
            App.Log("music path: " + music.getLocalPath());
            if (!(new File(music.getLocalPath()).exists())) {
                DatabaseManager.get().removeTrack(music);
                items.remove(i);
            }
        }
    }

    public void checkCache(int checkFor) {
        for (int i = 0; i < items.size(); i++) {
            if (!(items.get(i) instanceof VkMusic)) {
                continue;
            }

            VkMusic music = (VkMusic) items.get(i);

            if(checkFor == CHECK_FOR_PLAYLIST){
                if(music.getLocalPath() == null) {
                    long id = music.getId();
                    VkMusic data = DatabaseManager.get().findById(id);
                    if (data != null) {
                        music.setLocalPath(data.getLocalPath());
                        ((VkMusic) items.get(i)).setLocalPath(music.getLocalPath());
                        ((VkMusic) items.get(i)).setState(BaseMusic.STATE_COMPLETED);
                    }
                }else {
                    if (!(new File(music.getLocalPath()).exists())) {
                        DatabaseManager.get().removeTrack(music);
                        ((VkMusic) items.get(i)).setLocalPath(null);
                        ((VkMusic) items.get(i)).setState(BaseMusic.STATE_DEFAULT);
                    }
                    else {
                        ((VkMusic) items.get(i)).setLocalPath(music.getLocalPath());
                        ((VkMusic) items.get(i)).setState(BaseMusic.STATE_COMPLETED);
                    }
                }
                notifyItemChanged(i);
            }

            if(checkFor == CHECK_FOR_DATABASE){
                if(music.getLocalPath() == null){
                    DatabaseManager.get().removeTrack(music);
                    items.remove(i);
                    notifyItemRemoved(i);
                    continue;
                }
                if (!(new File(music.getLocalPath()).exists())) {
                    DatabaseManager.get().removeTrack(music);
                    items.remove(i);
                    notifyItemRemoved(i);
                }
            }
        }
    }
}

