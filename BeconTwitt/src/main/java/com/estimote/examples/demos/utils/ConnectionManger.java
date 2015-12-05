package com.estimote.examples.demos.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by hp on 12/5/2015.
 */
public class ConnectionManger {

    public static boolean hasInternet(Context activity) {
        ConnectivityManager manager = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }
}
