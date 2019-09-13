package com.comandante.uncolor.vkmusic.base_adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public abstract class ViewRenderer <M extends ItemModel, VH extends RecyclerView.ViewHolder>
{
    protected int type;
    protected Context context;
    public ViewRenderer(int type, Context context){
        this.type = type;
        this.context = context;
    }

    public abstract void bindView(@NonNull M model, @NonNull VH holder);

    @NonNull
    public abstract VH createViewHolder(@Nullable ViewGroup parent);

    protected int getType(){
        return type;
    }
}