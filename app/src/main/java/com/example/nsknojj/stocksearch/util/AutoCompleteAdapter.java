package com.example.nsknojj.stocksearch.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.nsknojj.stocksearch.MainActivity;
import com.example.nsknojj.stocksearch.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nsknojj on 11/25/2017.
 */

public class AutoCompleteAdapter extends BaseAdapter implements Filterable {

    public class Symbol {
        public String Symbol, Name, Exchange;
    }

    private static final int MAX_RESULTS = 5;
    private Context mContext;
    private List<Symbol> resultList = new ArrayList<Symbol>();

    public AutoCompleteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Symbol getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            // You should fetch the LayoutInflater once in your constructor
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_2, null);
        }
        ((TextView)convertView.findViewById(R.id.text1)).setText(getItem(position).Symbol + " - "
                + getItem(position).Name + " (" + getItem(position).Exchange + ")");
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<Symbol> books = findBooks(mContext, constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = books;
                    filterResults.count = books.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<Symbol>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    /**
     * Returns a search result for the given stock symbol.
     */

    private List<Symbol> findBooks(Context context, String symbol) {
        JSONArray json;
        ArrayList<Symbol> ret = new ArrayList<Symbol>();
//        ((Activity)mContext).findViewById(R.id.progressBar4).setVisibility(View.VISIBLE);
        try {
            json = new JSONArray(JsonGetter.readUrl(context.getString(R.string.my_host) + "/api-complete/" + symbol));
//            System.out.println(json.toString());
            int n = json.length()>5? 5: json.length();
            for (int i=0; i<n; i++) {
                Symbol x = new Symbol();
                x.Symbol = json.getJSONObject(i).getString("Symbol");
                x.Name = json.getJSONObject(i).getString("Name");
                x.Exchange = json.getJSONObject(i).getString("Exchange");
                ret.add(x);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        ((Activity)mContext).findViewById(R.id.progressBar4).setVisibility(View.GONE);
        return ret;
    }
}
