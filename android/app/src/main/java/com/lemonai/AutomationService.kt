package com.lemonai

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * AutomationService - Android Accessibility Service for UI Automation
 *
 * This service enables automation of UI interactions in other apps,
 * similar to the Blurr approach for mobile workflow automation.
 */
class AutomationService : AccessibilityService() {
    companion object {
        private const val TAG = "AutomationService"
        private var instance: AutomationService? = null
    }

    private val webViewCallbacks = ConcurrentHashMap<String, (String) -> Unit>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "AutomationService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            // Log events for debugging
            Log.d(TAG, "Accessibility event: ${it.eventType}, package: ${it.packageName}")
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "AutomationService interrupted")
    }

    /**
     * Find elements matching a selector in the current UI
     * @param selector The selector to match (supports text, content description, resource ID)
     * @return JSON string representing the found elements
     */
    fun findElements(selector: String): String {
        val root = rootInActiveWindow
        if (root == null) {
            Log.e(TAG, "Root node is null")
            return "[]"
        }

        val elements = mutableListOf<JSONObject>()
        searchNodes(root, selector, elements)

        return elements.joinToString(",", prefix = "[", postfix = "]") { it.toString() }
    }

    /**
     * Search for nodes matching the selector recursively
     * @param node The node to start searching from
     * @param selector The selector to match
     * @param results The list to add matching nodes to
     */
    private fun searchNodes(node: AccessibilityNodeInfo?, selector: String, results: MutableList<JSONObject>) {
        if (node == null) return

        // Check if this node matches the selector
        if (matchesSelector(node, selector)) {
            val element = JSONObject().apply {
                put("id", node.viewIdResourceName ?: "")
                put("text", node.text?.toString() ?: "")
                put("contentDescription", node.contentDescription?.toString() ?: "")
                put("className", node.className.toString())
                put("packageName", node.packageName?.toString() ?: "")
                
                // Get bounds in screen coordinates
                val bounds = Rect()
                node.getBoundsInScreen(bounds)
                put("bounds", JSONObject().apply {
                    put("left", bounds.left)
                    put("top", bounds.top)
                    put("right", bounds.right)
                    put("bottom", bounds.bottom)
                })
                
                put("visible", node.isVisibleToUser)
                put("enabled", node.isEnabled)
                put("clickable", node.isClickable)
            }
            results.add(element)
        }

        // Recursively search children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                searchNodes(child, selector, results)
                child.recycle()
            }
        }
    }

    /**
     * Check if a node matches the selector
     * @param node The node to check
     * @param selector The selector to match
     * @return True if the node matches the selector
     */
    private fun matchesSelector(node: AccessibilityNodeInfo, selector: String): Boolean {
        // Simple selector matching - in a real implementation, this would be more sophisticated
        return when {
            selector.startsWith("text:") -> {
                val text = selector.substring(5) // Remove "text:" prefix
                node.text?.toString()?.contains(text, ignoreCase = true) == true ||
                node.contentDescription?.toString()?.contains(text, ignoreCase = true) == true
            }
            selector.startsWith("id:") -> {
                val id = selector.substring(3) // Remove "id:" prefix
                node.viewIdResourceName?.contains(id) == true
            }
            selector.startsWith("class:") -> {
                val className = selector.substring(6) // Remove "class:" prefix
                node.className?.toString()?.contains(className, ignoreCase = true) == true
            }
            else -> {
                // Default to text matching for backward compatibility
                node.text?.toString()?.contains(selector, ignoreCase = true) == true ||
                node.contentDescription?.toString()?.contains(selector, ignoreCase = true) == true
            }
        }
    }

    /**
     * Click an element matching the selector
     * @param selector The selector to match
     * @return True if click was successful
     */
    fun clickElement(selector: String): Boolean {
        val root = rootInActiveWindow
        if (root == null) {
            Log.e(TAG, "Root node is null")
            return false
        }

        val node = findFirstNode(root, selector)
        if (node != null && node.isClickable) {
            // Perform the click action
            val success = node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
            node.recycle()
            return success
        }

        node?.recycle()
        return false
    }

    /**
     * Fill a form field with text
     * @param selector The selector for the form field
     * @param text The text to fill
     * @return True if fill was successful
     */
    fun fillFormField(selector: String, text: String): Boolean {
        val root = rootInActiveWindow
        if (root == null) {
            Log.e(TAG, "Root node is null")
            return false
        }

        val node = findFirstNode(root, selector)
        if (node != null && node.isEditable) {
            // Set the text
            val bundle = android.os.Bundle()
            bundle.putCharSequence(
                android.view.accessibility.AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            val success = node.performAction(
                android.view.accessibility.AccessibilityNodeInfo.ACTION_SET_TEXT,
                bundle
            )
            node.recycle()
            return success
        }

        node?.recycle()
        return false
    }

    /**
     * Find the first node matching the selector
     * @param root The root node to start searching from
     * @param selector The selector to match
     * @return The first matching node, or null if not found
     */
    private fun findFirstNode(root: AccessibilityNodeInfo, selector: String): AccessibilityNodeInfo? {
        if (matchesSelector(root, selector)) {
            return root
        }

        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            if (child != null) {
                val match = findFirstNode(child, selector)
                if (match != null) {
                    root.recycle()
                    return match
                }
                child.recycle()
            }
        }

        return null
    }

    /**
     * Switch to a different app
     * @param packageName The package name of the app to switch to
     * @return True if switch was successful
     */
    fun switchToApp(packageName: String): Boolean {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error switching to app $packageName", e)
            return false
        }
    }

    /**
     * Get the current app package name
     * @return The package name of the current app
     */
    fun getCurrentApp(): String {
        // We can't directly access currentPackage, so we'll return empty string
        return ""
    }

    /**
     * Perform a long press on an element
     * @param selector The selector to match
     * @return True if long press was successful
     */
    fun longPressElement(selector: String): Boolean {
        val root = rootInActiveWindow
        if (root == null) {
            Log.e(TAG, "Root node is null")
            return false
        }

        val node = findFirstNode(root, selector)
        if (node != null) {
            val success = node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_LONG_CLICK)
            node.recycle()
            return success
        }

        node?.recycle()
        return false
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
    fun swipe(startX: Int, startY: Int, endX: Int, endY: Int, duration: Int): Boolean {
        try {
            // Use the built-in gesture API if available (API 24+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val path = Path()
                path.moveTo(startX.toFloat(), startY.toFloat())
                path.lineTo(endX.toFloat(), endY.toFloat())
                
                val gestureStart = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, duration.toLong()))
                    .build()
                
                return dispatchGesture(gestureStart, null, null)
            } else {
                // Fallback for older versions - this would require additional implementation
                Log.w(TAG, "Swipe gesture not supported on this Android version")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error performing swipe gesture", e)
            return false
        }
    }

    /**
     * Register a callback for WebView communication
     * @param callbackId Unique ID for the callback
     * @param callback The callback function
     */
    fun registerCallback(callbackId: String, callback: (String) -> Unit) {
        webViewCallbacks[callbackId] = callback
    }

    /**
     * Execute a callback
     * @param callbackId The ID of the callback to execute
     * @param result The result to pass to the callback
     */
    fun executeCallback(callbackId: String, result: String) {
        webViewCallbacks[callbackId]?.let { callback ->
            callback(result)
            webViewCallbacks.remove(callbackId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    /**
     * Get the current instance of the service
     * @return The current instance, or null if not connected
     */
    fun getInstance(): AutomationService? {
        return instance
    }
}