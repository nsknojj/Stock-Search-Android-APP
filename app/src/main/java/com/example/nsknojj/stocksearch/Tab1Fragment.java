package com.example.nsknojj.stocksearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.nsknojj.stocksearch.util.DetailRow;
import com.example.nsknojj.stocksearch.util.JsonGetter;
import com.example.nsknojj.stocksearch.util.StockDetailAdapter;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by nsknojj on 11/29/2017.
 */

public class Tab1Fragment extends Fragment {


    String mText;
    WebView mWebView2;
    ImageButton starBtn;
    Button buttonChange = null;
    ListView mStockDetail = null;
    WebSettings mWebSettings;
    ArrayList<DetailRow> rows = new ArrayList<>();
    StockDetailAdapter adapter = null;
    String selected_indicator = "Price", current_indicator = null;
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        mText = getArguments().getString("mText");
        rootView = inflater.inflate(R.layout.fragment_tab1, container, false);

        rootView.findViewById(R.id.star).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                starStock(view);
            }
        });

        rootView.findViewById(R.id.fb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareFB(view);
            }
        });

        rootView.findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                indChange(view);
            }
        });

        buttonChange = rootView.findViewById(R.id.button4);
        mWebView2 = rootView.findViewById(R.id.webView2);
        mWebSettings = mWebView2.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        starBtn = rootView.findViewById(R.id.star);
        setStarStatus();
        rows.clear();
        adapter = new StockDetailAdapter(getContext(), rows);
        ListView lv = rootView.findViewById(R.id.detail_list);
        Log.d("my", lv.toString());
        lv.setAdapter(adapter);

        mStockDetail = rootView.findViewById(R.id.detail_list);
        String go_url = getString(R.string.my_host) + "/api-stock?symbol=" + mText.toUpperCase();
        new AsyncQueryTaskTab1().execute(go_url);

        Spinner mSpinner = rootView.findViewById(R.id.spinner);
        ArrayList<String> indicators = new ArrayList<>(Arrays.asList("Price", "SMA", "EMA", "STOCH", "RSI", "ADX", "CCI", "BBANDS", "MACD"));
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(super.getContext(), android.R.layout.simple_spinner_item, indicators);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter2);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                selected_indicator = item.toString();
                checkButtonChange();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        mWebView2.loadUrl("file:///android_asset/ind.html");

        mWebView2.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                mWebView2.evaluateJavascript("callJS('"+mText+"')", null);
                selected_indicator = "Price";
                indChange(rootView.findViewById(R.id.text_fav));
            }
        });

        return rootView;
    }

    public void indChange(View view) {
        mWebView2.evaluateJavascript("show('"+selected_indicator+"')", null);
        current_indicator = selected_indicator;
        checkButtonChange();
    }

    public void checkButtonChange() {
        if (selected_indicator == current_indicator)
            buttonChange.setEnabled(false);
        else
            buttonChange.setEnabled(true);
    }

    public class AsyncQueryTaskTab1 extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String searchUrl = urls[0];
            String res = null;
            try {
                res = JsonGetter.readUrl(searchUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rootView.findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
            mStockDetail.setVisibility(View.GONE);
            rootView.findViewById(R.id.fail2).setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject js = new JSONObject(s);
                if (!js.has("Meta Data")) throw new Exception("No Meta Data");
                JSONObject meta = js.getJSONObject("Meta Data");
                JSONObject time_series = js.getJSONObject("Time Series (Daily)");
                String time_stamp = meta.getString("3. Last Refreshed");
                if (!time_stamp.contains(":")) {
                    time_stamp += " 16:00:00";
                }
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                format2.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                Date now = format.parse(time_stamp);
                time_stamp = format2.format(now);
                String timestamp = time_series.names().getString(0);
                String symbol = meta.getString("2. Symbol").toUpperCase();
                double close = time_series.getJSONObject(timestamp).getDouble("4. close");
                double open = time_series.getJSONObject(timestamp).getDouble("1. open");

                JSONObject tmp = time_series.getJSONObject(time_series.names().getString(1));

                double previous_close = tmp.getDouble("4. close");
                double change = close - previous_close;
                double change_percent = (close - previous_close) / previous_close;
                double days_range_low = time_series.getJSONObject(timestamp).getDouble("3. low");
                double days_range_high = time_series.getJSONObject(timestamp).getDouble("2. high");
                String volume = time_series.getJSONObject(timestamp).getString("5. volume");

                rows.clear();
                rows.add(new DetailRow("Stock Symbol", symbol));
                rows.add(new DetailRow("Last Price", String.format("%.2f", close)));
                rows.add(new DetailRow("Change", String.format("%.2f (%.2f%%)", change, change_percent*100)));
                rows.add(new DetailRow("Timestamp", time_stamp));
                rows.add(new DetailRow("Open", String.format("%.2f", open)));
                rows.add(new DetailRow("Close", String.format("%.2f", previous_close)));
                rows.add(new DetailRow("Day's Range", String.format("%.2f - %.2f", days_range_low, days_range_high)));
                rows.add(new DetailRow("Volume", volume));

                setStarStatus();

            } catch (Exception e) {
                e.printStackTrace();
                rootView.findViewById(R.id.progressBar2).setVisibility(View.GONE);
                rootView.findViewById(R.id.fail2).setVisibility(View.VISIBLE);
                return;
            }

            adapter.notifyDataSetChanged();
            rootView.findViewById(R.id.progressBar2).setVisibility(View.GONE);
            mStockDetail.setVisibility(View.VISIBLE);
        }
    }

    public void addStock(String symbol) {
        Log.d("my", "star add stock");
        SharedPreferences a = getContext().getSharedPreferences("favList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = a.edit();
        editor.putString(symbol, rows.get(1).value + '+' + rows.get(2).value);
        editor.apply();
        MainActivity.favList.add(new DetailRow(symbol, rows.get(1).value + '+' + rows.get(2).value));
        MainActivity.favAdapter.notifyDataSetChanged();
    }

    public void removeStock(String symbol) {
        Log.d("my", "star remove stock");
        SharedPreferences a = getContext().getSharedPreferences("favList", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = a.edit();
        editor.remove(symbol);
        editor.apply();
        MainActivity.favList.remove(new DetailRow(symbol, "A"));
        Log.d("my", symbol + ' ' + ((Integer)MainActivity.favList.size()).toString());
        MainActivity.favAdapter.notifyDataSetChanged();
    }

    public void starStock(View view) {
        Log.d("my", "star clicked");
        SharedPreferences a = getContext().getSharedPreferences("favList", Context.MODE_PRIVATE);
        boolean inList = a.getAll().containsKey(mText);
        if (inList) removeStock(mText);
        else addStock(mText);
        setStarStatus();
    }


    public void setStarStatus() {
        Log.d("my", "star status");
        SharedPreferences a = getContext().getSharedPreferences("favList", Context.MODE_PRIVATE);
        boolean inList = a.getAll().containsKey(mText);
        boolean loading = rows.isEmpty();
        if (inList) {
            starBtn.setEnabled(true);
            starBtn.setImageResource(R.drawable.fullstar);
        } else {
            starBtn.setImageResource(R.drawable.star);
            if (loading) starBtn.setEnabled(false);
            else starBtn.setEnabled(true);
        }
    }


    String highchartsUrl = "http://export.highcharts.com/";

    public void shareFB(View view) {
        Log.d("FB", "clicked");
//        CallbackManager callbackManager = CallbackManager.Factory.create();
        ValueCallback<String> callback = new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.d("FB receive chart config", s);
                if (s.equals("undefined") || s.equals("null")) {
                    Toast.makeText(getContext(), "Cannot load chart data!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String config = s;

                RequestQueue MyRequestQueue = Volley.newRequestQueue(getContext());

                StringRequest MyStringRequest = new StringRequest(Request.Method.POST, highchartsUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("highchart response", "response");
                        showFBDialog(highchartsUrl + response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //This code is executed if there is an error.
                    }
                }) {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<String, String>();
                        MyData.put("options", config); //Add the data you'd like to send to the server.
                        MyData.put("async", "true");
                        MyData.put("type", "image/png");
                        MyData.put("filename", "chart");
                        return MyData;
                    }
                };
                MyRequestQueue.add(MyStringRequest);
                MyRequestQueue.start();
            }
        };
        mWebView2.evaluateJavascript("fetchChart('"+current_indicator+"')", callback);
    }

    CallbackManager callbackManager = CallbackManager.Factory.create();

    public void showFBDialog(String export_url) {
        Log.d("share url", export_url);
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(export_url))
                .build();
        ShareDialog shareDialog = new ShareDialog(getActivity());
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.d("result", "A");
                Toast.makeText(getActivity(), "Posted", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancel() {
                Toast.makeText(getActivity(), "Not posted", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getActivity(), "Post error", Toast.LENGTH_SHORT).show();
            }
        });

        shareDialog.show(content, ShareDialog.Mode.FEED);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        DetailActivity.this.onActivityResult(requestCode, resultCode, data);
//    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}