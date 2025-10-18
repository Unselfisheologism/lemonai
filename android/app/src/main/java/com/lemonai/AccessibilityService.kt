package com.lemonai

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityWindowInfo
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import java.util.*

/**
 * Android accessibility service for UI automation based on Blurr
 * This service allows the AI agent to touch UI elements and view phone screen
 * as described in desire.md and UI-description.md
 */
class AccessibilityService : AccessibilityService() {
    private val TAG = "LemonAI_Accessibility"
    
    // Callback interface for handling automation events
    interface AutomationListener {
        fun onScreenChanged(packageName: String, activityName: String)
        fun onElementFound(element: AccessibilityNodeInfo)
        fun onActionCompleted(action: String, success: Boolean)
        fun onError(error: String)
    }
    
    private var automationListener: AutomationListener? = null
    
    fun setAutomationListener(listener: AutomationListener) {
        automationListener = listener
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
        
        // Configure the service
        val config = serviceInfo
        config.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                AccessibilityEvent.TYPE_VIEW_FOCUSED or
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        config.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        config.notificationTimeout = 100
        
        serviceInfo = config
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { e ->
            when (e.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    val packageName = e.packageName?.toString() ?: ""
                    val className = e.className?.toString() ?: ""
                    Log.d(TAG, "Screen changed: $packageName / $className")
                    
                    automationListener?.onScreenChanged(packageName, className)
                }
                
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    Log.d(TAG, "View clicked: ${e.text}")
                }
                
                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    Log.d(TAG, "View focused: ${e.text}")
                }
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }
    
    /**
     * Find UI elements by text or content description
     */
    fun findElementByText(text: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        
        // Search for elements containing the specified text
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return if (nodes.isNotEmpty()) nodes[0] else null
    }
    
    /**
     * Find UI elements by view ID
     */
    fun findElementById(viewId: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        
        // Search for elements with the specified ID
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        return if (nodes.isNotEmpty()) nodes[0] else null
    }
    
    /**
     * Find UI elements by class name
     */
    fun findElementByClass(className: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        
        // Search for elements with the specified class name
        return findNodeByClass(root, className)
    }
    
    private fun findNodeByClass(node: AccessibilityNodeInfo?, className: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.className?.toString() == className) return node
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findNodeByClass(child, className)
            if (result != null) {
                return result
            }
        }
        
        return null
    }
    
    /**
     * Click on a UI element
     */
    fun clickElement(element: AccessibilityNodeInfo): Boolean {
        if (element.isClickable) {
            val success = element.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "Click action performed: $success")
            automationListener?.onActionCompleted("click", success)
            return success
        } else {
            // Try to find a clickable parent
            var parent = element.parent
            while (parent != null) {
                if (parent.isClickable) {
                    val success = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d(TAG, "Click action performed on parent: $success")
                    automationListener?.onActionCompleted("click", success)
                    return success
                }
                parent = parent.parent
            }
        }
        
        // If the element itself is not clickable, try to perform a click action directly
        val success = element.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Log.d(TAG, "Click action performed directly: $success")
        automationListener?.onActionCompleted("click", success)
        return success
    }
    
    /**
     * Long click on a UI element
     */
    fun longClickElement(element: AccessibilityNodeInfo): Boolean {
        val success = element.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        Log.d(TAG, "Long click action performed: $success")
        automationListener?.onActionCompleted("long_click", success)
        return success
    }
    
    /**
     * Focus on a UI element
     */
    fun focusElement(element: AccessibilityNodeInfo): Boolean {
        val success = element.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        Log.d(TAG, "Focus action performed: $success")
        automationListener?.onActionCompleted("focus", success)
        return success
    }
    
    /**
     * Set text in an editable element
     */
    fun setText(element: AccessibilityNodeInfo, text: String): Boolean {
        if (element.isEditable) {
            val bundle = android.os.Bundle()
            bundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            val success = element.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
            Log.d(TAG, "Set text action performed: $success, text: $text")
            automationListener?.onActionCompleted("set_text", success)
            return success
        }
        
        Log.d(TAG, "Element is not editable")
        automationListener?.onActionCompleted("set_text", false)
        return false
    }
    
    /**
     * Scroll in a scrollable element
     */
    fun scrollElement(element: AccessibilityNodeInfo, direction: Int): Boolean {
        val action = when (direction) {
            1 -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            -1 -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            else -> return false
        }
        
        val success = element.performAction(action)
        Log.d(TAG, "Scroll action performed: $success, direction: $direction")
        automationListener?.onActionCompleted("scroll", success)
        return success
    }
    
    /**
     * Get text from an element
     */
    fun getTextFromElement(element: AccessibilityNodeInfo): String {
        return element.text?.toString() ?: ""
    }
    
    /**
     * Get bounds of an element
     */
    fun getElementBounds(element: AccessibilityNodeInfo): Rect {
        val bounds = Rect()
        element.getBoundsInScreen(bounds)
        return bounds
    }
    
    /**
     * Get current screen information
     */
    fun getCurrentScreenInfo(): ScreenInfo {
        val root = rootInActiveWindow
        val windows = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindows()
        } else {
            emptyList()
        }
        
        val packageName = root?.packageName?.toString() ?: ""
        val className = root?.className?.toString() ?: ""
        
        return ScreenInfo(
            packageName = packageName,
            activityName = className,
            elementCount = countElements(root),
            windowsCount = windows.size
        )
    }
    
    private fun countElements(node: AccessibilityNodeInfo?): Int {
        if (node == null) return 0
        
        var count = 1 // Count the current node
        for (i in 0 until node.childCount) {
            count += countElements(node.getChild(i))
        }
        
        return count
    }
    
    /**
     * Perform a series of automation actions
     */
    fun performAutomationSequence(actions: List<AutomationAction>): Boolean {
        var allSuccessful = true
        
        for (action in actions) {
            val element = when {
                action.text.isNotEmpty() -> findElementByText(action.text)
                action.id.isNotEmpty() -> findElementById(action.id)
                action.className.isNotEmpty() -> findElementByClass(action.className)
                else -> null
            }
            
            if (element != null) {
                val success = when (action.type) {
                    ActionType.CLICK -> clickElement(element)
                    ActionType.LONG_CLICK -> longClickElement(element)
                    ActionType.SET_TEXT -> setText(element, action.text)
                    ActionType.FOCUS -> focusElement(element)
                    ActionType.SCROLL_FORWARD -> scrollElement(element, 1)
                    ActionType.SCROLL_BACKWARD -> scrollElement(element, -1)
                }
                
                if (!success) {
                    allSuccessful = false
                    automationListener?.onError("Action ${action.type} failed on element")
                    break
                }
            } else {
                allSuccessful = false
                automationListener?.onError("Element not found for action: ${action.type}")
                break
            }
        }
        
        return allSuccessful
    }
    
    /**
     * Take a screenshot of the current screen (simulated)
     * In a real implementation, this would require additional permissions
     * and potentially a foreground service
     */
    fun takeScreenshot(): String {
        // This is a simulated implementation
        // In a real app, you would need to implement actual screenshot functionality
        // which typically requires additional permissions and approaches
        return "screenshot_${System.currentTimeMillis()}.jpg"
    }
    
    /**
     * Data class for screen information
     */
    data class ScreenInfo(
        val packageName: String,
        val activityName: String,
        val elementCount: Int,
        val windowsCount: Int
    )
    
    /**
     * Data class for automation action
     */
    data class AutomationAction(
        val type: ActionType,
        val text: String = "",
        val id: String = "",
        val className: String = ""
    )
    
    /**
     * Enum for action types
     */
    enum class ActionType {
        CLICK, LONG_CLICK, SET_TEXT, FOCUS, SCROLL_FORWARD, SCROLL_BACKWARD
    }
    
    /**
     * Clean up resources when service is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        automationListener = null
    }
}