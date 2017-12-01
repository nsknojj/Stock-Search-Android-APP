package com.example.nsknojj.stocksearch.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.nsknojj.stocksearch.R;

import java.util.ArrayList;

/**
 * Created by nsknojj on 11/28/2017.
 */

public class FavListAdapter extends ArrayAdapter<DetailRow> {
    private Context mContext;
    public FavListAdapter(Context context, ArrayList<DetailRow> rows) {
        super(context, 0, rows);
        mContext = context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.fav_list_row, parent, false);
        }

        TextView key = view.findViewById(R.id.key);
        TextView price = view.findViewById(R.id.price);
        TextView change = view.findViewById(R.id.change);
        key.setText(getItem(position).key);
        String v = getItem(position).value;
        int p = v.indexOf('+');
        if (p==-1) p = 0;
        price.setText(v.substring(0, p));
        change.setText(v.substring(p+1));
        if (v.contains("-")) change.setTextColor(Color.RED);
        else change.setTextColor(Color.parseColor("#42aa04"));
        return view;
    }

}
