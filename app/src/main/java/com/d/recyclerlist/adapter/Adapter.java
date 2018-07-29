package com.d.recyclerlist.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.d.lib.recyclerlist.adapter.CommonAdapter;
import com.d.lib.recyclerlist.adapter.CommonHolder;
import com.d.recyclerlist.R;

import java.util.List;

/**
 * Adapter
 * Created by D on 2018/7/28.
 */
public class Adapter extends CommonAdapter<String> {

    public Adapter(Context context, List<String> datas, int layoutId) {
        super(context, datas, layoutId);
    }

    @Override
    public void convert(final int position, CommonHolder holder, String item) {
        holder.setText(R.id.tv_content, item);
        holder.getConvertView().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Click at: " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
