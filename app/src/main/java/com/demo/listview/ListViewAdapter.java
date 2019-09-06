package com.demo.listview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListViewAdapter extends BaseAdapter {
    private List<String> data;
    private Context  context;
    public boolean isDeleteing = false;
    ViewHolder holder;
    public ListViewAdapter(Context context,List<String> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_info_item, parent, false);
            holder.list_view_tv = (TextView) convertView.findViewById(R.id.list_view_tv);
            holder.iv_drag = convertView.findViewById(R.id.iv_drag);
            holder.iv_del = convertView.findViewById(R.id.iv_del);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.list_view_tv.setText(getItem(position));
        holder.iv_del.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (isDeleteing == true) {
                        return;
                    }
                    isDeleteing = true;
                    data.remove(position);
                    notifyDataSetChanged();
                    isDeleteing = false;
                }
            });
        return convertView;
    }

    private class ViewHolder {
        TextView list_view_tv;
        ImageView iv_del,iv_drag;
    }

    public boolean exchange(int src, int dst) {
        boolean success = false;
        String srcItem = data.get(src);
        String dstItem = data.get(dst);
        if (src != -1 && dst != -1) {
            Collections.swap(data, src, dst);
            success = true;
        }
        if (success) {
            notifyDataSetChanged();
        }
        return success;
    }

}
