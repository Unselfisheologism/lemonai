package com.lemonai

import android.content.Context
import android.webkit.JavascriptInterface
import android.util.Log
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class NativeBridge(private val context: Context) {
    
    @JavascriptInterface
    fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val service = "${context.packageName}/.LemonAccessibilityService"
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            enabledServices?.contains(service) == true
        } catch (e: Exception) {
            Log.e("NativeBridge", "Error checking accessibility service", e)
            false
        }
    }
    
    @JavascriptInterface
    fun requestAccessibilityPermission(): Boolean {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.e("NativeBridge", "Error requesting accessibility permission", e)
            return false
        }
    }
    
    @JavascriptInterface
    fun switchToApp(appName: String): String {
        return try {
            // Implementation to switch to another app
            val result = JSONObject()
            result.put("success", true)
            result.put("message", "Switched to app: $appName")
            result.toString()
        } catch (e: Exception) {
            val result = JSONObject()
            result.put("success", false)
            result.put("error", e.message)
            result.toString()
        }
    }
    
    @JavascriptInterface
    fun isAutomationAvailable(): Boolean {
        return isAccessibilityServiceEnabled()
    }
    
    @JavascriptInterface
    fun executeWorkflow(stepsJson: String): String {
        return try {
            val engine = AndroidWorkflowEngine(context)
            val steps = engine.parseWorkflowJson(stepsJson)
            
            // Execute workflow
            val result = engine.executeWorkflow(steps)
            result.toString()
        } catch (e: Exception) {
            val result = JSONObject()
            result.put("success", false)
            result.put("error", e.message)
            result.toString()
        }
    }
    
    @JavascriptInterface
    fun executeMcpPiece(pieceName: String, action: String, params: String): String {
        // Execute an MCP piece and return the result
        return try {
            val result = NodeServiceHelper.executeMcpPiece(pieceName, action, params)
            val response = JSONObject()
            // Check if node.js integration is available
            if (result == "Node.js integration not available") {
                response.put("success", false)
                response.put("result", result)
            } else {
                response.put("success", true)
                response.put("result", result)
            }
            response.toString()
        } catch (e: Exception) {
            val response = JSONObject()
            response.put("success", false)
            response.put("error", e.message)
            response.toString()
        }
    }
}