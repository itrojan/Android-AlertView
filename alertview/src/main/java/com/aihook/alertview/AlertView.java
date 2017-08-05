package com.aihook.alertview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: shun
 * @date: 2017-08-05 16:10
 * @Desc:
 */

public class AlertView {

    public enum Style {
        ActionSheet,
        Alert
    }

    public static final int HORIZONTAL_BUTTONS_MAXCOUNT = 2;
    public static final int CANCELPOSITION = -1;//点击取消按钮返回 －1，其他按钮从0开始算

    private String title;
    private String msg;
    private String[] destructive;
    private String[] others;
    private List<String> mDestructive;
    private List<String> mOthers;
    private String cancel;
    private ArrayList<String> mDatas = new ArrayList<String>();

    private WeakReference<Context> contextWeak;
    private ViewGroup rootView;//AlertView 的 根View
    private ViewGroup loAlertHeader;//窗口headerView

    private Style style = Style.Alert;

    private OnDismissListener onDismissListener;
    private OnItemClickListener onItemClickListener;
    private boolean isShowing;

    private AlertDialog mAlertDialog; //AlertView
    private Dialog mDialog; //ActionSheet

    public AlertView(Builder builder) {
        this.contextWeak = new WeakReference<>(builder.context);
        this.style = builder.style;
        this.title = builder.title;
        this.msg = builder.msg;
        this.cancel = builder.cancel;
        this.destructive = builder.destructive;
        this.others = builder.others;
        this.onItemClickListener = builder.onItemClickListener;

        initData(title, msg, cancel, destructive, others);
        initViews();
        initEvents();
    }

    public AlertView(String title, String msg, String cancel, String[] destructive, String[] others, Context context, Style style, OnItemClickListener onItemClickListener) {
        this.contextWeak = new WeakReference<>(context);
        if (style != null) this.style = style;
        this.onItemClickListener = onItemClickListener;

        initData(title, msg, cancel, destructive, others);
        initViews();
        initEvents();
    }

    /**
     * 获取数据
     */
    protected void initData(String title, String msg, String cancel, String[] destructive, String[] others) {

        this.title = title;
        this.msg = msg;
        if (destructive != null) {
            this.mDestructive = Arrays.asList(destructive);
            this.mDatas.addAll(mDestructive);
        }
        if (others != null) {
            this.mOthers = Arrays.asList(others);
            this.mDatas.addAll(mOthers);
        }
        if (cancel != null) {
            this.cancel = cancel;
            if (style == Style.Alert && mDatas.size() < HORIZONTAL_BUTTONS_MAXCOUNT) {
                this.mDatas.add(0, cancel);
            }
        }

    }

    protected void initViews() {
        Context context = contextWeak.get();
        if (context == null) return;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        switch (style) {
            case ActionSheet:
                initActionSheetViews(layoutInflater);
                break;
            case Alert:
                initAlertViews(layoutInflater);
                break;
        }
    }

    protected void initHeaderView(ViewGroup viewGroup) {
        loAlertHeader = (ViewGroup) viewGroup.findViewById(R.id.loAlertHeader);
        //标题和消息
        TextView tvAlertTitle = (TextView) viewGroup.findViewById(R.id.tvAlertTitle);
        TextView tvAlertMsg = (TextView) viewGroup.findViewById(R.id.tvAlertMsg);
        if (title != null) {
            tvAlertTitle.setText(title);
        } else {
            tvAlertTitle.setVisibility(View.GONE);
        }
        if (msg != null) {
            tvAlertMsg.setText(msg);
        } else {
            tvAlertMsg.setVisibility(View.GONE);
        }
    }

    protected void initListView() {
        Context context = contextWeak.get();
        if (context == null) return;

        ListView alertButtonListView = (ListView) rootView.findViewById(R.id.alertButtonListView);
        //把cancel作为footerView
        if (cancel != null && style == Style.Alert) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
            TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
            tvAlert.setText(cancel);
            tvAlert.setClickable(true);
//            tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
            tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_cancel));
            tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
            tvAlert.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
            alertButtonListView.addFooterView(itemView);
        }
        AlertViewAdapter adapter = new AlertViewAdapter(mDatas, mDestructive, style, title, msg);
        alertButtonListView.setAdapter(adapter);
        alertButtonListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (onItemClickListener != null) onItemClickListener.onItemClick(AlertView.this, position);
                dismiss();
            }
        });
    }

    protected void initActionSheetViews(LayoutInflater layoutInflater) {
        Context context = contextWeak.get();
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_actionsheet, null);
        initHeaderView(rootView);

        initListView();
        TextView tvAlertCancel = (TextView) rootView.findViewById(R.id.tvAlertCancel);
        if (cancel != null) {
            tvAlertCancel.setVisibility(View.VISIBLE);
            tvAlertCancel.setText(cancel);
        }
        tvAlertCancel.setOnClickListener(new OnTextClickListener(CANCELPOSITION));

        mDialog = new Dialog(context, R.style.ActionSheetDialogStyle);
        mDialog.setContentView(rootView);
        Window dialogWindow = mDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.y = context.getResources().getDimensionPixelSize(R.dimen.margin_actionsheet_left_right);
        dialogWindow.setAttributes(lp);
    }

    protected void initAlertViews(LayoutInflater layoutInflater) {
        Context context = contextWeak.get();
        rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_alertview_alert, null);
        initHeaderView(rootView);

        int position = 0;
        //如果总数据小于等于HORIZONTAL_BUTTONS_MAXCOUNT，则是横向button
        if (mDatas.size() <= HORIZONTAL_BUTTONS_MAXCOUNT) {
            ViewStub viewStub = (ViewStub) rootView.findViewById(R.id.viewStubHorizontal);
            viewStub.inflate();
            LinearLayout loAlertButtons = (LinearLayout) rootView.findViewById(R.id.loAlertButtons);
            for (int i = 0; i < mDatas.size(); i++) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_alertbutton, null);
                TextView tvAlert = (TextView) itemView.findViewById(R.id.tvAlert);
                tvAlert.setClickable(true);
                //如果不是第一个按钮
                if (i != 0) {
                    //添加上按钮之间的分割线
                    View divier = itemView.findViewById(R.id.divier);
                    divier.setVisibility(View.VISIBLE);
                }

                //设置点击效果
                if (mDatas.size() == 1) {
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_bottom);
                } else if (i == 0) {//设置最左边的按钮效果
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_left);
                } else if (i == mDatas.size() - 1) {//设置最右边的按钮效果
                    tvAlert.setBackgroundResource(R.drawable.bg_alertbutton_right);
                }
                String data = mDatas.get(i);
                tvAlert.setText(data);

                //取消按钮的样式
                if (data == cancel) {
//                    tvAlert.setTypeface(Typeface.DEFAULT_BOLD);
                    tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_cancel));
                    tvAlert.setOnClickListener(new OnTextClickListener(CANCELPOSITION));
                    position = position - 1;
                }
                //高亮按钮的样式
                else if (mDestructive != null && mDestructive.contains(data)) {
                    tvAlert.setTextColor(context.getResources().getColor(R.color.textColor_alert_button_destructive));
                }

                tvAlert.setOnClickListener(new OnTextClickListener(position));
                position++;
                loAlertButtons.addView(itemView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            }
        } else {
            ViewStub viewStub = (ViewStub) rootView.findViewById(R.id.viewStubVertical);
            viewStub.inflate();
            initListView();
        }

        mAlertDialog = new AlertDialog.Builder(context).create();
        mAlertDialog.setView(rootView);
    }

    protected void initEvents() {
    }

    public AlertView addExtView(View extView) {
        loAlertHeader.addView(extView);
        return this;
    }

    /**
     * 添加这个View到Activity的根视图
     */
    public void show() {
        if (isShowing()) {
            return;
        }
        isShowing = true;
        if (style == Style.Alert) {
            mAlertDialog.show();
        } else {
            mDialog.show();
        }
    }

    /**
     * 检测该View是不是已经添加到根视图
     *
     * @return 如果视图已经存在该View返回true
     */
    public boolean isShowing() {
        if (style == Style.Alert) {
            return mAlertDialog.isShowing();
        } else {
            return mDialog.isShowing();
        }
    }

    public void dismiss() {
        if (style == Style.Alert) {
            mAlertDialog.dismiss();
            mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dismissImmediately();
                }
            });
        } else {
            mDialog.dismiss();
            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dismissImmediately();
                }
            });
        }
    }

    public void dismissImmediately() {
        isShowing = false;
        if (onDismissListener != null) {
            onDismissListener.onDismiss(this);
        }

    }

    public AlertView setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
        return this;
    }

    class OnTextClickListener implements View.OnClickListener {
        private int position;

        public OnTextClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) onItemClickListener.onItemClick(AlertView.this, position);
            dismiss();
        }
    }

    public AlertView setCancelable(boolean isCancelable) {
        if (style == Style.Alert) {
            mAlertDialog.setCancelable(isCancelable);
        } else {
            mDialog.setCancelable(isCancelable);
        }
        return this;
    }

    /**
     * Builder for arguments
     */
    public static class Builder {
        private Context context;
        private Style style;
        private String title;
        private String msg;
        private String cancel;
        private String[] destructive;
        private String[] others;
        private OnItemClickListener onItemClickListener;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setStyle(Style style) {
            if (style != null) {
                this.style = style;
            }
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder setCancelText(String cancel) {
            this.cancel = cancel;
            return this;
        }

        public Builder setDestructive(String... destructive) {
            this.destructive = destructive;
            return this;
        }

        public Builder setOthers(String[] others) {
            this.others = others;
            return this;
        }

        public Builder setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        public AlertView build() {
            return new AlertView(this);
        }
    }
}
