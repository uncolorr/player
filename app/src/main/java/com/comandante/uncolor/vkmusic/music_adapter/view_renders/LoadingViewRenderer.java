package com.comandante.uncolor.vkmusic.music_adapter.view_renders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.comandante.uncolor.vkmusic.R;
import com.comandante.uncolor.vkmusic.base_adapter.ViewRenderer;
import com.comandante.uncolor.vkmusic.models.Loading;
import com.comandante.uncolor.vkmusic.music_adapter.holders.TempLoadingViewHolder;

public class LoadingViewRenderer extends ViewRenderer<Loading, TempLoadingViewHolder> {

    public LoadingViewRenderer(int type, Context context) {
        super(type, context);
    }

    @Override
    public void bindView(@NonNull Loading model, @NonNull TempLoadingViewHolder holder) {

    }


    @NonNull
    @Override
    public TempLoadingViewHolder createViewHolder(@Nullable ViewGroup parent) {
        return new TempLoadingViewHolder(LayoutInflater.from(context).inflate(R.layout.loading_item, parent,
                false));
    }
}
