package com.kavindu.farmshare.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Utility class for network operations
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    
    /**
     * Check if the device has an active network connection
     * @param context Application context
     * @return true if connected, false otherwise
     */
    public static boolean isNetworkConnected(Context context) {
        if (context == null) return false;
        
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null && 
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
                
                // Network is connected, but let's not check server reachability here
                // as it would block the UI thread
                return true;
            }
            return false;
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected(); // Use isConnected instead of isConnectedOrConnecting
        }
    }
}