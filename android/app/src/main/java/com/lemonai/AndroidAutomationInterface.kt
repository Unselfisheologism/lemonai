package com.lemonai

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import org.json.JSONArray
import org.json.JSONObject

/**
 * AndroidAutomationInterface - JavaScript Interface for Android Automation
 *
 * This class provides the interface between JavaScript in the WebView and
 * Android native functionality for automation tasks.
 */
class AndroidAutomationInterface(private val context: Context) {
    companion object {
        private const val TAG = "AndroidAutomationInterface"
        private const val PREFS_NAME = "automation_prefs"
    }

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Request accessibility permission from the user
     * @return True if permission granted, false otherwise
     */
    @android.webkit.JavascriptInterface
    fun requestAccessibilityPermission(): Boolean {
        try {
            // Open the accessibility settings to allow the user to enable the service
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting accessibility permission", e)
            return false
        }
    }

    /**
     * Check if accessibility service is enabled
     * @return True if accessibility service is enabled
     */
    @android.webkit.JavascriptInterface
    fun isAccessibilityServiceEnabled(): Boolean {
        val packageNames = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        
        if (packageNames.isNullOrEmpty()) {
            return false
        }

        val components = packageNames.split(":")
        val targetService = "${context.packageName}/${AutomationService::class.java.canonicalName}"

        return components.any { it.contains(targetService) }
    }

    /**
     * Click an element matching the selector
     * @param selector The selector to match
     * @return True if click was successful
     */
    @android.webkit.JavascriptInterface
    fun clickElement(selector: String): Boolean {
        val service = AutomationService.getInstance()
        return service?.clickElement(selector) ?: false
    }

    /**
     * Fill a form field with text
     * @param selector The selector for the form field
     * @param text The text to fill
     * @return True if fill was successful
     */
    @android.webkit.JavascriptInterface
    fun fillFormField(selector: String, text: String): Boolean {
        val service = AutomationService.getInstance()
        return service?.fillFormField(selector, text) ?: false
    }

    /**
     * Switch to a different app
     * @param packageName The package name of the app to switch to
     * @return True if switch was successful
     */
    @android.webkit.JavascriptInterface
    fun switchToApp(packageName: String): Boolean {
        val service = AutomationService.getInstance()
        return service?.switchToApp(packageName) ?: false
    }

    /**
     * Get the current app package name
     * @return The package name of the current app
     */
    @android.webkit.JavascriptInterface
    fun getCurrentApp(): String {
        val service = AutomationService.getInstance()
        return service?.getCurrentApp() ?: ""
    }

    /**
     * Perform a long press on an element
     * @param selector The selector to match
     * @return True if long press was successful
     */
    @android.webkit.JavascriptInterface
    fun longPressElement(selector: String): Boolean {
        val service = AutomationService.getInstance()
        return service?.longPressElement(selector) ?: false
    }

    /**
     * Perform a swipe gesture
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param endX Ending X coordinate
     * @param endY Ending Y coordinate
     * @param duration Duration of the swipe in milliseconds
     * @return True if swipe was successful
     */
    @android.webkit.JavascriptInterface
    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Int): Boolean {
        val service = AutomationService.getInstance()
        return service?.swipe(startX, startY, endX, endY, duration) ?: false
    }

    /**
     * Find elements matching a selector
     * @param selector The selector to match
     * @return JSON string representing the found elements
     */
    @android.webkit.JavascriptInterface
    fun findElements(selector: String): String {
        val service = AutomationService.getInstance()
        return service?.findElements(selector) ?: "[]"
    }

    /**
     * Open a URL in a custom tab or browser
     * @param url The URL to open
     * @return True if successful
     */
    @android.webkit.JavascriptInterface
    fun openUrl(url: String): Boolean {
        return try {
            val uri = Uri.parse(url)
            val customTabsIntent = CustomTabsIntent.Builder()
                .addDefaultShareMenuItem()
                .setShowTitle(true)
                .build()
            
            customTabsIntent.launchUrl(context, uri)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URL: $url", e)
            // Fallback to regular browser
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } catch (fallbackException: Exception) {
                Log.e(TAG, "Error opening URL in fallback: $url", fallbackException)
                false
            }
        }
    }

    /**
     * Store data securely in Android Keystore
     * @param key The key for the data
     * @param data The data to store
     * @return True if storage was successful
     */
    @android.webkit.JavascriptInterface
    fun storeSecureData(key: String, data: String): Boolean {
        return try {
            with(sharedPreferences.edit()) {
                putString(key, data)
                apply()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error storing secure data for key: $key", e)
            false
        }
    }

    /**
     * Retrieve data from secure storage
     * @param key The key for the data
     * @return The stored data, or null if not found
     */
    @android.webkit.JavascriptInterface
    fun getSecureData(key: String): String? {
        return try {
            sharedPreferences.getString(key, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving secure data for key: $key", e)
            null
        }
    }

    /**
     * Remove data from secure storage
     * @param key The key for the data
     * @return True if removal was successful
     */
    @android.webkit.JavascriptInterface
    fun removeSecureData(key: String): Boolean {
        return try {
            with(sharedPreferences.edit()) {
                remove(key)
                apply()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing secure data for key: $key", e)
            false
        }
    }
}