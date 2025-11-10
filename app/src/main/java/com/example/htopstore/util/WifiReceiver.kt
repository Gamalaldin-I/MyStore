package com.example.htopstore.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

class WifiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        val isWifiConnected =
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (isWifiConnected) {
            Toast.makeText(context, "Wi-Fi Connected ✅", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Wi-Fi Disconnected ❌", Toast.LENGTH_SHORT).show()
        }
    }
}
