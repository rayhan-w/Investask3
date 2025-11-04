package com.kavindu.farmshare.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * Utility class for handling Bluetooth operations and errors
 */
public class BluetoothUtils {
    private static final String TAG = "BluetoothUtils";

    /**
     * Safely check Bluetooth state
     * This method checks Bluetooth state without using the deprecated energy info API
     */
    public static void safeCheckBluetoothState() {
        try {
            // Get the default Bluetooth adapter
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            
            // If Bluetooth is not supported or adapter is null, return early
            if (adapter == null) {
                Log.d(TAG, "Bluetooth is not supported on this device");
                return;
            }

            // Check if Bluetooth is enabled
            if (adapter.isEnabled()) {
                Log.d(TAG, "Bluetooth is enabled");
            } else {
                Log.d(TAG, "Bluetooth is disabled");
            }
        } catch (Exception e) {
            // Catch any other potential errors
            Log.w(TAG, "Error accessing Bluetooth adapter: " + e.getMessage());
        }
    }

    /**
     * Check if Bluetooth is supported and enabled
     * @param context Application context
     * @return true if Bluetooth is supported and enabled, false otherwise
     */
    public static boolean isBluetoothEnabled(Context context) {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            return adapter != null && adapter.isEnabled();
        } catch (Exception e) {
            Log.w(TAG, "Error checking Bluetooth status: " + e.getMessage());
            return false;
        }
    }
}