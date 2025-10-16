package com.lemonai

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class SimplifiedWorkflowEngine(private val context: Context) {
    
    fun isAutomationAvailable(): Boolean {
        return isAccessibilityServiceEnabled()
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${context.packageName}/.LemonAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service) == true
    }
    
    fun requestAutomationPermission(): Boolean {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.e("SimplifiedWorkflowEngine", "Error requesting automation permission", e)
            return false
        }
    }
    
    fun executeWorkflow(stepsJson: String): String {
        return try {
            val steps = parseWorkflowSteps(stepsJson)
            val results = mutableListOf<JSONObject>()
            
            for (step in steps) {
                val result = executeStep(step)
                results.add(result)
            }
            
            val response = JSONObject()
            response.put("success", true)
            response.put("results", JSONArray(results))
            response.toString()
        } catch (e: Exception) {
            val response = JSONObject()
            response.put("success", false)
            response.put("error", e.message)
            response.toString()
        }
    }
    
    private fun parseWorkflowSteps(stepsJson: String): List<JSONObject> {
        val steps = mutableListOf<JSONObject>()
        try {
            val jsonArray = JSONArray(stepsJson)
            for (i in 0 until jsonArray.length()) {
                steps.add(jsonArray.getJSONObject(i))
            }
        } catch (e: Exception) {
            Log.e("SimplifiedWorkflowEngine", "Error parsing workflow steps", e)
        }
        return steps
    }
    
    private fun executeStep(step: JSONObject): JSONObject {
        val result = JSONObject()
        
        return try {
            val type = step.getString("type")
            val params = step.getJSONObject("params")
            
            when (type) {
                "APP_SWITCH" -> {
                    val appName = params.getString("appName")
                    val success = switchToApp(appName)
                    result.put("type", type)
                    result.put("success", success)
                    if (success) {
                        result.put("message", "Switched to app: $appName")
                    } else {
                        result.put("error", "Failed to switch to app: $appName")
                    }
                }
                "FORM_FILL" -> {
                    val fields = params.getJSONObject("fields")
                    val success = fillForm(fields)
                    result.put("type", type)
                    result.put("success", success)
                    if (success) {
                        result.put("message", "Form filled successfully")
                    } else {
                        result.put("error", "Failed to fill form")
                    }
                }
                "CLICK" -> {
                    val selector = params.getString("selector")
                    val success = clickElement(selector)
                    result.put("type", type)
                    result.put("success", success)
                    if (success) {
                        result.put("message", "Clicked element: $selector")
                    } else {
                        result.put("error", "Failed to click element: $selector")
                    }
                }
                else -> {
                    result.put("type", type)
                    result.put("success", false)
                    result.put("error", "Unknown step type: $type")
                }
            }
            
            result
        } catch (e: Exception) {
            result.put("success", false)
            result.put("error", e.message)
            result
        }
    }
    
    private fun switchToApp(appName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(appName)
            launchIntent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("SimplifiedWorkflowEngine", "Error switching to app: $appName", e)
            false
        }
    }
    
    private fun fillForm(fields: JSONObject): Boolean {
        // In a real implementation, this would use the accessibility service
        // to find and fill form fields
        Log.d("SimplifiedWorkflowEngine", "Filling form with fields: $fields")
        return true // Simulate success
    }
    
    private fun clickElement(selector: String): Boolean {
        // In a real implementation, this would use the accessibility service
        // to find and click an element
        Log.d("SimplifiedWorkflowEngine", "Clicking element with selector: $selector")
        return true // Simulate success
    }
}