package com.sloy.sevibus.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class DragFavoritaCallback extends ItemTouchHelper.Callback {

    private final FavoritaDragHelperAdapter favoritaDragHelperAdapter;

    public DragFavoritaCallback(FavoritaDragHelperAdapter favoritaDragHelperAdapter) {
        this.favoritaDragHelperAdapter = favoritaDragHelperAdapter;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return favoritaDragHelperAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    public interface FavoritaDragHelperAdapter {
        boolean onItemMove(int fromPosition, int toPosition);
    }
}
