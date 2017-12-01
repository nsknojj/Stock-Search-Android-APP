package com.example.nsknojj.stocksearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nsknojj.stocksearch.util.AutoCompleteAdapter;
import com.example.nsknojj.stocksearch.util.DelayAutoCompleteTextView;
import com.example.nsknojj.stocksearch.util.DetailRow;
import com.example.nsknojj.stocksearch.util.FavListAdapter;
import com.example.nsknojj.stocksearch.util.JsonGetter;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    public AutoCompleteTextView bookTitle;
    public static ArrayList<DetailRow> favList = new ArrayList<>();
    public static FavListAdapter favAdapter = null;
    private String symbol = null;
    String sortBy="Default", order="Ascending";

    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.example.nsknojj.stocksearch", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {
            Log.e("KeyHash:", e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        printKeyHash();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bookTitle = findViewById(R.id.stock_symbol);
        bookTitle.setAdapter(new AutoCompleteAdapter(this));
        bookTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                AutoCompleteAdapter.Symbol book = (AutoCompleteAdapter.Symbol) adapterView.getItemAtPosition(position);
                bookTitle.setText(book.Symbol + " - " + book.Name + " (" + book.Exchange + ")");
            }
        });

        ((DelayAutoCompleteTextView)bookTitle).setLoadingIndicator(findViewById(R.id.progressBar4));

        final ListView mFavList = findViewById(R.id.favList);
        favAdapter = new FavListAdapter(this, favList);
        mFavList.setAdapter(favAdapter);
        mFavList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {

                Log.d("long clicked","pos: " + pos);
                DetailRow x = (DetailRow) adapterView.getItemAtPosition(pos);
                symbol = x.key;

//                ListPopupWindow popupWindow = new ListPopupWindow(MainActivity.this);
//                popupWindow.setAdapter();
                PopupMenu pop = new PopupMenu(MainActivity.this, view.findViewById(R.id.price), Gravity.CENTER);
                pop.getMenuInflater().inflate(R.menu.popup, pop.getMenu());
                pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId()==R.id.yes) {
                            Toast.makeText(MainActivity.this, "Selected Yes", Toast.LENGTH_SHORT).show();
                            SharedPreferences a = getSharedPreferences("favList", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = a.edit();
                            editor.remove(symbol);
                            editor.apply();
                            favList.remove(new DetailRow(symbol, "A"));
                            favAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(MainActivity.this, "Selected No", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                pop.show();

                return true;
            }
        });
        mFavList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Log.d("short clicked","pos: " + pos);
                DetailRow x = (DetailRow) adapterView.getItemAtPosition(pos);
                goToDetailFromFav(x.key);
            }
        });
        loadFavList();

        Spinner sortBySpinner = findViewById(R.id.spinner2);
        Spinner orderSpinner = findViewById(R.id.spinner3);
        ArrayList<String> sortByArray = new ArrayList<>(Arrays.asList("Sort by", "Default", "Symbol", "Price", "Change"));
        ArrayList<String> orderArray = new ArrayList<>(Arrays.asList("Order", "Ascending", "Descending"));

        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, sortByArray) {

            @Override
            public boolean isEnabled(int position) {
                return position != 0 && !getItem(position).equals(sortBy);
            }

            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View v = convertView;
                if (v == null) {
                    Context mContext = this.getContext();
                    LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(android.R.layout.simple_spinner_dropdown_item, null);
                }

                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setText(getItem(position));
                tv.setHeight(140);
                tv.setGravity(Gravity.CENTER);
                if (!isEnabled(position)) tv.setTextColor(Color.GRAY);
                return v;
            }
        };

        ArrayAdapter<String> orderAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, orderArray) {

            @Override
            public boolean isEnabled(int position) {
                return position != 0 && !getItem(position).equals(order);
            }

            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View v = convertView;
                if (v == null) {
                    Context mContext = this.getContext();
                    LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(android.R.layout.simple_spinner_dropdown_item, null);
                }

                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setText(getItem(position));
                tv.setHeight(140);
                tv.setGravity(Gravity.CENTER);
                if (!isEnabled(position)) tv.setTextColor(Color.GRAY);
                return v;
            }
        };
        sortBySpinner.setAdapter(sortByAdapter);
        orderSpinner.setAdapter(orderAdapter);

        sortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                sortBy = item.toString();
                sortFavList();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object item = parent.getItemAtPosition(pos);
                order = item.toString();
                sortFavList();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Switch autoSwitch = findViewById(R.id.auto_switch);
        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("autoSwitch", ((Boolean) b).toString());
                if (!b) cancelAutoRefresh();
                else autoRefresh();
            }
        });
    }

    public void sortFavList() {
        if (sortBy.equals("Default")) {
            loadFavList();
        } else if (sortBy.equals("Symbol")) {
            favList.sort(new Comparator<DetailRow>() {
                @Override
                public int compare(DetailRow x, DetailRow y) {
                    return x.key.compareTo(y.key);
                }
            });
        } else if (sortBy.equals("Price")) {
            favList.sort(new Comparator<DetailRow>() {
                @Override
                public int compare(DetailRow x, DetailRow y) {
                    Double a = Double.parseDouble(x.value.substring(0, x.value.indexOf('+')));
                    Double b = Double.parseDouble(y.value.substring(0, y.value.indexOf('+')));
                    return a.compareTo(b);
                }
            });
        } else if (sortBy.equals("Change")) {
            favList.sort(new Comparator<DetailRow>() {
                @Override
                public int compare(DetailRow x, DetailRow y) {
                    Double a = Double.parseDouble(x.value.substring(x.value.indexOf('+')+1, x.value.indexOf('(')));
                    Double b = Double.parseDouble(y.value.substring(y.value.indexOf('+')+1, y.value.indexOf('(')));
                    return a.compareTo(b);
                }
            });
        }
        if (order.equals("Descending")) {
            Collections.reverse(favList);
        }
        favAdapter.notifyDataSetChanged();
    }

    public void loadFavList() {
        favList.clear();
        SharedPreferences a = getSharedPreferences("favList", Context.MODE_PRIVATE);
        Map<String, ?> map  = a.getAll();
        for (Map.Entry<String,?> pair : map.entrySet()){
            favList.add(new DetailRow(pair.getKey(), pair.getValue().toString()));
        }
        favAdapter.notifyDataSetChanged();
//        favList.add(new DetailRow("AA", "BB"));
    }

    public void goToDetail(View view) {
        // Do something in response to button
        String text = bookTitle.getText().toString();
        text = text.trim().split(" ")[0].toUpperCase();

        String pattern = "^[A-Z0-9.]+$";
        Pattern p = Pattern.compile(pattern);
        if (!p.matcher(text).find()) {
            Toast.makeText(this, "Please enter a stock name or symbol", Toast.LENGTH_SHORT).show();
            return;
        }
/* (!/^[A-Za-z0-9\.]+$/.test(query)) { */

        Context context = MainActivity.this;
        Class destinationActivity = TabActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    public void goToDetailFromFav(String text) {
        Context context = MainActivity.this;
        Class destinationActivity = TabActivity.class;
        Intent intent = new Intent(context, destinationActivity);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    static AsyncTaskRefresh fetchThread;
    static ScheduledFuture<?> autoRefreshThread;
    static int ct;
//    Thread autoRefreshThread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//
//        }
//    });

    public void autoRefresh() {
        ScheduledExecutorService s = Executors.newScheduledThreadPool(1);
        ct = 0;
        autoRefreshThread = s.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                MainActivity.this.runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        if (fetchThread == null || fetchThread.getStatus() == AsyncTask.Status.FINISHED) {
                            ct = 0;
                            fetchRefresh(findViewById(R.id.button3));
                        } else {
                            ct++;
                            int t = favList.size()*2>10? favList.size()*2:10;
                            if (ct >= t) {
                                ct = 0;
                                fetchThread.onPostExecute(null);
                                fetchThread.cancel(true);
//                                fetchRefresh(findViewById(R.id.button3));
                            }
                        }
                    }
                });

            }
        }, 300, 1000, TimeUnit.MILLISECONDS);
    }

    public void cancelAutoRefresh() {
        autoRefreshThread.cancel(true);
        if (fetchThread!=null && fetchThread.getStatus()!= AsyncTask.Status.FINISHED) {
            fetchThread.onPostExecute(null);
            fetchThread.cancel(true);
        }
    }

    public void fetchRefresh(View view) {
        fetchThread = new AsyncTaskRefresh();
        fetchThread.execute();
    }

    public void goClear(View view) {
        bookTitle.setText("");
    }


    public class AsyncTaskRefresh extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... urls) {
            try {
                ArrayList<Thread> pool = new ArrayList<>();
                for (int j = 0; j < favList.size(); j++) {
                    final int i = j;
                    final String url = getString(R.string.my_host) + "/api-stock?symbol=" + favList.get(i).key;
                    Thread x = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String s = JsonGetter.readUrl(url);
                                if (s == null) return;
                                JSONObject js = null;

                                js = new JSONObject(s);
                                if (!js.has("Meta Data")) return;
                                JSONObject meta = js.getJSONObject("Meta Data");
                                JSONObject time_series = js.getJSONObject("Time Series (Daily)");
                                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                                format.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                                format2.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                                String timestamp = time_series.names().getString(0);
                                String symbol = meta.getString("2. Symbol").toUpperCase();
                                double close = time_series.getJSONObject(timestamp).getDouble("4. close");

                                JSONObject tmp = time_series.getJSONObject(time_series.names().getString(1));

                                double previous_close = tmp.getDouble("4. close");
                                double change = close - previous_close;
                                double change_percent = (close - previous_close) / previous_close;
                                favList.get(i).key = symbol;
                                String value = String.format("%.2f", close) + '+' + String.format("%.2f (%.2f%%)", change, change_percent * 100);
                                favList.get(i).value = value;
                                SharedPreferences a = getSharedPreferences("favList", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = a.edit();
                                editor.putString(symbol, value);
                                editor.apply();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    x.start();
                    pool.add(x);
                }

                for (Thread t : pool) {
                    t.join();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.d("myRefresh", "Start");
            super.onPreExecute();
            findViewById(R.id.spinner2).setEnabled(false);
            findViewById(R.id.spinner3).setEnabled(false);
            findViewById(R.id.button3).setEnabled(false);
            findViewById(R.id.favProgressBar).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            findViewById(R.id.button3).setEnabled(true);
            findViewById(R.id.favProgressBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.spinner2).setEnabled(true);
            findViewById(R.id.spinner3).setEnabled(true);
            favAdapter.notifyDataSetChanged();
            Log.d("myRefresh", "End");
        }
    }


}
