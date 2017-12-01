package com.example.nsknojj.stocksearch.util;

/**
 * Created by nsknojj on 11/25/2017.
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class JsonGetter {

    public static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);

            URLConnection urlc = url.openConnection();
            urlc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0a2) Gecko/20110613 Firefox/6.0a2");
            InputStream stream = urlc.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                reader.close();
        }
        return "";
    }
}
