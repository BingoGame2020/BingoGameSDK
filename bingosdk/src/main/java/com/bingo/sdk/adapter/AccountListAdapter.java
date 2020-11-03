package com.bingo.sdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bingo.sdk.inner.bean.Account;
import com.bingo.sdk.utils.ResourceManager;

import java.util.List;

public class AccountListAdapter extends BaseAdapter {
    private Context context;
    private List<Account> list;

    public AccountListAdapter(Context context, List<Account> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Account getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = LayoutInflater.from(context).inflate(ResourceManager.getLayout(context, "layout_bingo_account_list_item"), parent, false);
            holder.iv_delete = convertView.findViewById(ResourceManager.getId(context, "iv_delete"));
            holder.tv_uid = convertView.findViewById(ResourceManager.getId(context, "tv_uid"));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Account item = getItem(position);
        holder.tv_uid.setText(item.getUid());
        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDelete(item);
                }
            }
        });
        return convertView;
    }


    private OnItemDeleteListener listener;

    public void setOnDeleteListener(OnItemDeleteListener listener) {
        this.listener = listener;
    }

    public interface OnItemDeleteListener {
        void onDelete(Account account);
    }

    private class ViewHolder {
        private TextView tv_uid;
        private ImageView iv_delete;
    }
}
