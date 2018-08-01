package com.d.recyclerlist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.d.lib.recyclerlist.RecyclerList;
import com.d.lib.recyclerlist.adapter.MultiItemTypeSupport;
import com.d.recyclerlist.adapter.Adapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int TYPE_SIMPLE = 0;
    private final static int TYPE_MULTIPLE = 1;

    private int type = TYPE_SIMPLE;
    private RecyclerList list;
    private Adapter adapter;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_notify:
                notifyAdapter();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (RecyclerList) findViewById(R.id.list);
        findViewById(R.id.btn_notify).setOnClickListener(this);
        init();
    }

    private void init() {
        adapter = type == TYPE_MULTIPLE ? getMultiAdapter() : getSimpleAdapter();
        list.setAdapter(adapter);
    }

    private Adapter getSimpleAdapter() {
        return new Adapter(this, getDatas(200), R.layout.adapter_item_0);
    }

    private Adapter getMultiAdapter() {
        return new Adapter(this, getDatas(200), new MultiItemTypeSupport<String>() {
            @Override
            public int getLayoutId(int position, String s) {
                switch (getItemViewType(position, s)) {
                    case 1:
                        return R.layout.adapter_item_1;
                    default:
                        return R.layout.adapter_item_0;
                }
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position, String s) {
                return position % 2;
            }
        });
    }

    private void notifyAdapter() {
        adapter.setDatas(getDatas(4));
        adapter.notifyDataSetChanged();
    }

    @NonNull
    public List<String> getDatas(int count) {
        List<String> datas = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            datas.add("" + i);
        }
        return datas;
    }
}
