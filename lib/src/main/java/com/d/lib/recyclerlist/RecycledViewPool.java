package com.d.lib.recyclerlist;

import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.d.lib.recyclerlist.adapter.CommonAdapter;
import com.d.lib.recyclerlist.adapter.CommonHolder;

import java.util.ArrayList;

/**
 * RecycledViewPool lets you share Views between multiple RecyclerViews.
 * <p>
 * If you want to recycle views across RecyclerViews, create an instance of RecycledViewPool
 * and use {@link RecyclerList#setRecycledViewPool(RecycledViewPool)}.
 * <p>
 * RecyclerView automatically creates a pool for itself if you don't provide one.
 */
public class RecycledViewPool {
    private static final boolean DEBUG = false;

    private SparseArray<ArrayList<CommonHolder>> mScrap =
            new SparseArray<ArrayList<CommonHolder>>();
    private SparseIntArray mMaxScrap = new SparseIntArray();
    private int mAttachCount = 0;

    private static final int DEFAULT_MAX_SCRAP = 7;

    public void clear() {
        mScrap.clear();
    }

    public void setMaxRecycledViews(int viewType, int max) {
        mMaxScrap.put(viewType, max);
        final ArrayList<CommonHolder> scrapHeap = mScrap.get(viewType);
        if (scrapHeap != null) {
            while (scrapHeap.size() > max) {
                scrapHeap.remove(scrapHeap.size() - 1);
            }
        }
    }

    public CommonHolder getRecycledView(int viewType) {
        final ArrayList<CommonHolder> scrapHeap = mScrap.get(viewType);
        if (scrapHeap != null && !scrapHeap.isEmpty()) {
            final int index = scrapHeap.size() - 1;
            final CommonHolder scrap = scrapHeap.get(index);
            scrapHeap.remove(index);
            return scrap;
        }
        return null;
    }

    int size() {
        int count = 0;
        for (int i = 0; i < mScrap.size(); i++) {
            ArrayList<CommonHolder> viewHolders = mScrap.valueAt(i);
            if (viewHolders != null) {
                count += viewHolders.size();
            }
        }
        return count;
    }

    public void putRecycledView(@NonNull CommonHolder scrap) {
        final int viewType = scrap.getItemViewType();
        final ArrayList scrapHeap = getScrapHeapForType(viewType);
        if (mMaxScrap.get(viewType) <= scrapHeap.size()) {
            return;
        }
        if (DEBUG && scrapHeap.contains(scrap)) {
            throw new IllegalArgumentException("this scrap item already exists");
        }
//        scrap.resetInternal();
        scrapHeap.add(scrap);
    }

    void attach(CommonAdapter adapter) {
        mAttachCount++;
    }

    void detach() {
        mAttachCount--;
    }


    /**
     * Detaches the old adapter and attaches the new one.
     * <p>
     * RecycledViewPool will clear its cache if it has only one adapter attached and the new
     * adapter uses a different ViewHolder than the oldAdapter.
     *
     * @param oldAdapter             The previous adapter instance. Will be detached.
     * @param newAdapter             The new adapter instance. Will be attached.
     * @param compatibleWithPrevious True if both oldAdapter and newAdapter are using the same
     *                               ViewHolder and view types.
     */
    void onAdapterChanged(CommonAdapter oldAdapter, CommonAdapter newAdapter,
                          boolean compatibleWithPrevious) {
        if (oldAdapter != null) {
            detach();
        }
        if (!compatibleWithPrevious && mAttachCount == 0) {
            clear();
        }
        if (newAdapter != null) {
            attach(newAdapter);
        }
    }

    private ArrayList<CommonHolder> getScrapHeapForType(int viewType) {
        ArrayList<CommonHolder> scrap = mScrap.get(viewType);
        if (scrap == null) {
            scrap = new ArrayList<>();
            mScrap.put(viewType, scrap);
            if (mMaxScrap.indexOfKey(viewType) < 0) {
                mMaxScrap.put(viewType, DEFAULT_MAX_SCRAP);
            }
        }
        return scrap;
    }
}