package com.example.nsknojj.stocksearch.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nsknojj.stocksearch.R;

import java.util.ArrayList;

/**
 * Created by nsknojj on 11/27/2017.
 */

public class StockDetailAdapter extends ArrayAdapter<DetailRow> {

    private Context mContext = null;

    public StockDetailAdapter(Context context, ArrayList<DetailRow> rows) {
        super(context, 0, rows);
        mContext = context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.stock_list_row, parent, false);
        }

        TextView key = (TextView) view.findViewById(R.id.key);
        TextView value = (TextView) view.findViewById(R.id.value);
        key.setText(getItem(position).key);
        value.setText(getItem(position).value);
        if (getItem(position).key=="Change") {
            String change = getItem(position).value;
            if(change.charAt(0) == '-') {
                ImageView imageView = (ImageView) view.findViewById(R.id.arrow);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.down);
            }
            else {
                ImageView imageView = (ImageView) view.findViewById(R.id.arrow);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.up);
            }
        }
        return view;
    };
}
