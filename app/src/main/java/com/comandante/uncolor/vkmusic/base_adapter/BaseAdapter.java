package com.comandante.uncolor.vkmusic.base_adapter;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter extends RecyclerView.Adapter {

    protected List<ItemModel> items = new ArrayList<>();
    
    protected OnLoadMoreListener onLoadMoreListener;

    @NonNull
    private final SparseArray<ViewRenderer> renderers = new SparseArray<>();
    private boolean loading;

    @NonNull
    @Override
    public
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final ViewRenderer renderer = renderers.get(viewType);
        if (renderer != null) {
            return renderer.createViewHolder(parent);
        }

        throw new RuntimeException("Not supported Item View Type: " + viewType);
    }

    public void registerRenderer(@NonNull final ViewRenderer renderer) {
        final int type = renderer.getType();

        if (renderers.get(type) == null) {
            renderers.put(type, renderer);
        } else {
            throw new RuntimeException("ViewRenderer already exist with this type: " + type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public
    void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final ItemModel item = items.get(position);
        final ViewRenderer renderer = renderers.get(item.getType());
        if (renderer != null) {
            renderer.bindView(item, holder);
        } else {
            throw new RuntimeException("Not supported View Holder: " + holder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener){
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public RecyclerView.OnScrollListener getScrollListener(){
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                    int visibleThreshold = 1;
                    int lastVisibleItem, totalItemCount;
                    LinearLayoutManager linearLayoutManager =
                            (LinearLayoutManager) recyclerView.getLayoutManager();
                    if(linearLayoutManager == null) {
                        return;
                    }

                    if (dy > 0) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                            loading = true;
                        }
                    }
                }
            };
        }

    public void setLoaded() {
        loading = false;
    }


    public abstract void add(ItemModel item);
    public abstract void addList(List<? extends ItemModel> items);
    public abstract void remove(ItemModel itemModel);
    public abstract void clear();

    public abstract boolean isEmpty();
}
