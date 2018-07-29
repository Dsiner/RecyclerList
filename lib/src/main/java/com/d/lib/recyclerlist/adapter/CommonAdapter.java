package com.d.lib.recyclerlist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * CommonAdapter
 * Created by D on 2017/4/25.
 */
public abstract class CommonAdapter<T> {
    protected Context mContext;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;
    protected int mLayoutId;
    protected MultiItemTypeSupport<T> multiItemTypeSupport;
    protected DataSetObserver dataSetObserver;

    public CommonAdapter(Context context, List<T> datas, int layoutId) {
        mContext = context;
        mDatas = new ArrayList<T>();
        mDatas = datas == null ? new ArrayList<T>() : datas;
        mInflater = LayoutInflater.from(mContext);
        mLayoutId = layoutId;
    }

    public CommonAdapter(Context context, List<T> datas, MultiItemTypeSupport<T> multiItemTypeSupport) {
        mContext = context;
        mDatas = new ArrayList<T>();
        mDatas = datas == null ? new ArrayList<T>() : datas;
        mInflater = LayoutInflater.from(mContext);
        this.multiItemTypeSupport = multiItemTypeSupport;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        this.dataSetObserver = observer;
    }

    public void notifyDataSetChanged() {
        if (dataSetObserver != null) {
            dataSetObserver.notifyChanged();
        }
    }

    public void setDatas(List<T> datas) {
        if (mDatas != null && datas != null) {
            mDatas.clear();
            mDatas.addAll(datas);
        }
    }

    public List<T> getDatas() {
        return mDatas;
    }

    public int getItemViewType(int position) {
        if (multiItemTypeSupport != null) {
            multiItemTypeSupport.getItemViewType(position, position < mDatas.size() ? mDatas.get(position) : null);
        }
        return mLayoutId;
    }

    public int getViewTypeCount() {
        if (multiItemTypeSupport != null) {
            return multiItemTypeSupport.getViewTypeCount();
        }
        return 1;
    }

    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public T getItem(int position) {
        return mDatas == null ? null : mDatas.size() == 0 ? null : mDatas.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final CommonHolder holder = getViewHolder(position, convertView, parent);
        convert(position, holder, getItem(position));
        return holder.getConvertView();
    }

    public abstract void convert(int position, CommonHolder holder, T item);

    private CommonHolder getViewHolder(int position, View convertView, ViewGroup parent) {
        if (multiItemTypeSupport != null) {
            if (mDatas != null && mDatas.size() > 0) {
                return CommonHolder.get(mContext, convertView, parent,
                        multiItemTypeSupport.getLayoutId(position, mDatas.get(position)), position);
            }
            return CommonHolder.get(mContext, convertView, parent,
                    multiItemTypeSupport.getLayoutId(position, null), position);
        }
        return CommonHolder.get(mContext, convertView, parent, mLayoutId, position);
    }

    public interface DataSetObserver {
        void notifyChanged();
    }
}