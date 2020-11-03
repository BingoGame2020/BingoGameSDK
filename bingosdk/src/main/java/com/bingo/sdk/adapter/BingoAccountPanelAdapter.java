package com.bingo.sdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bingo.sdk.inner.bean.FloatWindow;
import com.bingo.sdk.utils.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class BingoAccountPanelAdapter extends RecyclerView.Adapter<BingoAccountPanelAdapter.MyViewHolder> {
    private Context context;
    private List<FloatWindow> list;

    public BingoAccountPanelAdapter(Context context, List<FloatWindow> list) {
        this.context = context;
        this.list = list != null ? list : new ArrayList<FloatWindow>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(ResourceManager.getLayout(context, "layout_bingo_panel_item"), parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final FloatWindow window = list.get(position);
        holder.tv_name.setText(window.getName());
        holder.layout_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(window);
                }
            }
        });
        int type = window.getType();
        if (type == 1) {
            holder.iv_icon.setImageResource(ResourceManager.getDrawable(context, "bingo_ic_account"));
        } else if (type == 2) {
            holder.iv_icon.setImageResource(ResourceManager.getDrawable(context, "bingo_ic_gift"));

        } else if (type == 3) {
            holder.iv_icon.setImageResource(ResourceManager.getDrawable(context, "bingo_ic_notice"));

        } else if (type == 4) {
            holder.iv_icon.setImageResource(ResourceManager.getDrawable(context, "bingo_ic_customer_service"));

        } else if (type == 5) {
            holder.iv_icon.setImageResource(ResourceManager.getDrawable(context, "bingo_ic_offical_account"));

        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private ImageView iv_icon;
        private ConstraintLayout layout_main;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(ResourceManager.getId(context, "iv_float_account"));
            tv_name = itemView.findViewById(ResourceManager.getId(context, "tv_panel_name"));
            layout_main = itemView.findViewById(ResourceManager.getId(context, "layout_float_account"));
        }
    }


    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    public interface OnItemClickListener {
        void onItemClick(FloatWindow window);
    }
}
