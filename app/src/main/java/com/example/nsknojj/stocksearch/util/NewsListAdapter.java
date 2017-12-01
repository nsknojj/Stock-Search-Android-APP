package com.example.nsknojj.stocksearch.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.nsknojj.stocksearch.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by nsknojj on 11/29/2017.
 */

public class NewsListAdapter extends ArrayAdapter<News> {
    private Context mContext;
    public NewsListAdapter(Context context, ArrayList<News> rows) {
        super(context, 0, rows);
        mContext = context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.news_list_row, parent, false);
        }

        TextView title = view.findViewById(R.id.title);
        TextView author = view.findViewById(R.id.author);
        TextView date = view.findViewById(R.id.date);
        title.setText(getItem(position).title);
        author.setText("Author: "+getItem(position).author);

        DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        DateFormat format2 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        try {
            Date d = format.parse(getItem(position).date);
            date.setText("Date: "+format2.format(d));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

}
