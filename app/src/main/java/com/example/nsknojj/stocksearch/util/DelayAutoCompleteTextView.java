package com.example.nsknojj.stocksearch.util;

/**
 * Created by nsknojj on 11/26/2017.
 */

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
//import android.support.v7.widget.AppCompatAutoCompleteTextView;

public class DelayAutoCompleteTextView extends AutoCompleteTextView {
    public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private static int DELAYED_MESSAGE = 0;
    private static int DELAY = 350;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DelayAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mHandler.removeMessages(DELAYED_MESSAGE);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(DELAYED_MESSAGE, keyCode, 0, text), DELAY);
    }

    @Override
    public void onFilterComplete(int count) {
        super.onFilterComplete(count);
        mLoadingIndicator.setVisibility(View.GONE);

    }

    private View mLoadingIndicator;
    public void setLoadingIndicator(View progressBar) {
        mLoadingIndicator = progressBar;
    }
}