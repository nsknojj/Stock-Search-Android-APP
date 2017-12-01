package com.example.nsknojj.stocksearch;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.nsknojj.stocksearch.util.JsonGetter;
import com.example.nsknojj.stocksearch.util.News;
import com.example.nsknojj.stocksearch.util.NewsListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by nsknojj on 11/29/2017.
 */

public class Tab3Fragment extends Fragment {

    String mText;
    View rootView;
    ArrayList<News> newsList = new ArrayList<>();
    NewsListAdapter newsListAdapter = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);
        rootView = inflater.inflate(R.layout.fragment_tab3, container, false);

        mText = getArguments().getString("mText");

        newsListAdapter = new NewsListAdapter(getContext(), newsList);
        ListView newsListView = rootView.findViewById(R.id.newsListView);
        newsListView.setAdapter(newsListAdapter);
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Log.d("news short clicked","pos: " + pos);
                News x = (News) adapterView.getItemAtPosition(pos);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(x.link));
                startActivity(browserIntent);
            }
        });
        new AsyncQueryTab3().execute();

        return rootView;
    }

    public class AsyncQueryTab3 extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... urls) {

            String url = getString(R.string.my_host) + "/api-feeds?symbol=" + mText;
            String res = null;
            try {
                res = JsonGetter.readUrl(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rootView.findViewById(R.id.progressBar3).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.fail3).setVisibility(View.GONE);
            rootView.findViewById(R.id.newsListView).setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject res = new JSONObject(s);
                newsList.clear();
                int ct = 0;
                Log.d("feeds", "get");
                JSONArray item = res.getJSONObject("rss").getJSONArray("channel").getJSONObject(0).getJSONArray("item");
                for (int i=0; i<item.length(); i++) {
                    if (item.getJSONObject(i).getJSONArray("link").getString(0).contains("seekingalpha.com/article")) {
                        ct ++;
                        newsList.add(new News(
                                item.getJSONObject(i).getJSONArray("title").getString(0),
                                item.getJSONObject(i).getJSONArray("sa:author_name").getString(0),
                                item.getJSONObject(i).getJSONArray("pubDate").getString(0),
                                item.getJSONObject(i).getJSONArray("link").getString(0)));
                    }
//                    if (ct>=5) break;
                }

                newsListAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
                rootView.findViewById(R.id.progressBar3).setVisibility(View.GONE);
                rootView.findViewById(R.id.fail3).setVisibility(View.VISIBLE);
                return;
            }
            rootView.findViewById(R.id.progressBar3).setVisibility(View.GONE);
            rootView.findViewById(R.id.newsListView).setVisibility(View.VISIBLE);
            return;
        }
    }
}
