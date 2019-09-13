package com.comandante.uncolor.vkmusic.music_adapter.view_renders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.main_activity.base_music_fragment.BaseMusicPresenterInterface;
import com.comandante.uncolor.vkmusic.base_adapter.ViewRenderer;
import com.comandante.uncolor.vkmusic.models.BaseMusic;
import com.comandante.uncolor.vkmusic.music_adapter.holders.TempMusicViewHolder;

public class MusicViewRenderer extends ViewRenderer<BaseMusic, TempMusicViewHolder> {

    private BaseMusicPresenterInterface presenter;

    public MusicViewRenderer(int type, Context context, BaseMusicPresenterInterface presenter){
        super(type, context);
        this.presenter = presenter;

    }

    @Override
    public void bindView(@NonNull BaseMusic model, @NonNull TempMusicViewHolder holder) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public TempMusicViewHolder createViewHolder(@Nullable ViewGroup parent) {
        return new TempMusicViewHolder(LayoutInflater.from(context).inflate(R.layout.music_item, parent,
                false), presenter);
    }
}
