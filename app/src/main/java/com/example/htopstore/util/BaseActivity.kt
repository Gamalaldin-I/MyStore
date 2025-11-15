package com.example.htopstore.util

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

/**
 * Base Activity with automatic offline detection
 * All your activities can extend this for automatic offline support
 * 
 * NO CODE NEEDED in child activities!
 */
abstract class BaseActivity : AppCompatActivity() {

    private var offlineManager: OfflineManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        
        // Automatically setup offline monitoring for all activities
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        offlineManager = OfflineManager(this, rootView) {
            onNetworkRestored()
        }
        offlineManager?.startMonitoring()
    }

    /**
     * Override this in child activities to handle network restoration
     * Default: do nothing
     */
    protected open fun onNetworkRestored() {
        // Child activities can override this
    }

    override fun onDestroy() {
        super.onDestroy()
        offlineManager?.stopMonitoring()
    }
}
