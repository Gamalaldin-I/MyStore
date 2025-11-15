package com.example.htopstore.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.htopstore.databinding.OfflineScreenBinding

/**
 * Simple offline overlay manager
 * Shows/hides offline screen in any activity
 */
class OfflineManager(
    private val context: Context,
    private val rootView: ViewGroup,
    private val onRetry: () -> Unit = {}
) {
    
    private var offlineView: View? = null
    private var binding: OfflineScreenBinding? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * Start monitoring network and show/hide offline screen automatically
     */
    fun startMonitoring() {
        // Check initial state
        if (!isNetworkAvailable()) {
            show()
        }

        // Setup network monitoring
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                (context as? android.app.Activity)?.runOnUiThread {
                    hide()
                }
            }

            override fun onLost(network: Network) {
                (context as? android.app.Activity)?.runOnUiThread {
                    show()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        }
    }

    /**
     * Show offline overlay
     */
    fun show() {
        if (offlineView == null) {
            binding = OfflineScreenBinding.inflate(LayoutInflater.from(context), rootView, false)
            offlineView = binding?.root
            
            binding?.btnRetry?.setOnClickListener {
                if (isNetworkAvailable()) {
                    hide()
                }
            }
            
            rootView.addView(offlineView)
        }
        offlineView?.visibility = View.VISIBLE
    }

    /**
     * Hide offline overlay
     */
    fun hide() {
        onRetry()
        offlineView?.visibility = View.GONE
    }

    /**
     * Stop monitoring and cleanup
     */
    fun stopMonitoring() {
        networkCallback?.let { callback ->
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Callback wasn't registered
            }
        }
        networkCallback = null
        
        offlineView?.let { rootView.removeView(it) }
        offlineView = null
        binding = null
    }

    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }
}