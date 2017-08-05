package com.aihook.alertview;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * @author: shun
 * @date: 2017-08-05 16:10
 * @Desc:
 */
public class AlertViewAdapter extends BaseAdapter {
    private List<String> mDatas;
    private List<String> mDestructive;
    private AlertView.Style style;
    private String title, msg;

    public AlertViewAdapter(List<String> datas, List<String> destructive, AlertView.Style style, String title, String msg) {
        this.mDatas = datas;
        this.mDestructive = destructive;
        this.style = style;
        this.title = title;
        this.msg = msg;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String data = mDatas.get(position);
        Holder holder = null;
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.item_alertbutton, null);
            holder = creatHolder(view);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        holder.UpdateUI(parent.getContext(), data, position);
        return view;
    }

    public Holder creatHolder(View view) {
        return new Holder(view);
    }

    class Holder {
        private TextView tvAlert;
        private View divier_top;

        public Holder(View view) {
            tvAlert = (TextView) view.findViewById(R.id.tvAlert);
            divier_top = view.findViewById(R.id.divier_top);
        }

        public void UpdateUI(Context context, String data, int position) {
            tvAlert.setText(data);
            if (mDestructive != null && mDestructive.contains(data)) {
                tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_destructive));
            } else {
                tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_others));
            }
            if (style == AlertView.Style.ActionSheet && position == 0 && TextUtils.isEmpty(title) && TextUtils.isEmpty(msg)) {
                divier_top.setBackgroundResource(android.R.color.transparent);
            }
        }
    }
}