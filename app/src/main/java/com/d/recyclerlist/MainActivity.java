package com.d.recyclerlist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.d.lib.recyclerlist.RecyclerList;
import com.d.recyclerlist.adapter.Adapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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
        adapter = new Adapter(this, getDatas(200), R.layout.adapter_item);
        list.setAdapter(adapter);
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
