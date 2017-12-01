package com.example.nsknojj.stocksearch.util;

/**
 * Created by nsknojj on 11/27/2017.
 */

public class DetailRow {
    public String key, value;
    public DetailRow(String _key, String _value) {
        key = _key;
        value = _value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DetailRow other = (DetailRow) obj;
        if (!key.equals(other.key))
            return false;
        return true;
    }
}
