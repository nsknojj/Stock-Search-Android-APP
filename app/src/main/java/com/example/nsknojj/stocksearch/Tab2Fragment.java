package com.example.nsknojj.stocksearch;

import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.nsknojj.stocksearch.util.JsonGetter;

/**
 * Created by nsknojj on 11/29/2017.
 */

public class Tab2Fragment extends Fragment {

    String mText;
    View rootView;
    WebView mWebView;
    WebSettings mWebSettings;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        rootView = inflater.inflate(R.layout.fragment_tab2, container, false);

        mText = getArguments().getString("mText");

        mWebView = (WebView) rootView.findViewById(R.id.webView1);
        mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/his.html");

        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                try {
                    String go_url = getString(R.string.my_host) + "/api-chart/Price?symbol=" + mText;
                    Log.d("my", go_url);

                    new AsyncQueryTask().execute(go_url);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


        return rootView;
    }

    public class AsyncQueryTask extends AsyncTask<String, Void, String> {

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
            rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
            rootView.findViewById(R.id.fail).setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
            if (s==null || s.substring(0, 10).contains("Error"))
                rootView.findViewById(R.id.fail).setVisibility(View.VISIBLE);
            else {
                mWebView.setVisibility(View.VISIBLE);
                mWebView.evaluateJavascript("callJS("+s+")", null);
            }
        }
    }
}
