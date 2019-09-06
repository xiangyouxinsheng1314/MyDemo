package com.demo.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.R;

import java.util.List;

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>  {
    private List<String> data;
    private ItemTouchHelper mItemTouchHelper;
    public RecyclerAdapter(List<String> data) {
        this.data = data;
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper){
        mItemTouchHelper = itemTouchHelper;
    }

    public List<String> getDataList() {
        return data;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_info_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.tv_recycler.setText(data.get(i));
        viewHolder.iv_drag.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mItemTouchHelper.startDrag(viewHolder);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_recycler;
        ImageView iv_del,iv_drag;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_recycler = itemView.findViewById(R.id.list_view_tv);
            iv_drag = itemView.findViewById(R.id.iv_drag);
            iv_del = itemView.findViewById(R.id.iv_del);
        }
    }
}
