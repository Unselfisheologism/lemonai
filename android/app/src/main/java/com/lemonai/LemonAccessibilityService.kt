package com.lemonai

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class LemonAccessibilityService : AccessibilityService() {
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("LemonAccessibilityService", "Service connected")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                Log.d("LemonAccessibilityService", "View clicked: ${event.className}")
            }
            AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                Log.d("LemonAccessibilityService", "View focused: ${event.className}")
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d("LemonAccessibilityService", "Window state changed: ${event.className}")
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d("LemonAccessibilityService", "Service interrupted")
    }
    
    fun switchToApp(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
                return true
            } ?: false
        } catch (e: Exception) {
            Log.e("LemonAccessibilityService", "Error switching to app: $packageName", e)
            false
        }
    }
    
    fun findAndClick(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return if (nodes.isNotEmpty()) {
            val node = nodes[0]
            if (node.isClickable) {
                node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                true
            } else {
                // Try to find a parent that is clickable
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        parent.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                        return true
                    }
                    parent = parent.parent
                }
                false
            }
        } else {
            false
        }
    }
}