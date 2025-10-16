package com.lemonai

import android.content.Context
import android.content.Intent
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class AndroidWorkflowEngine(private val context: Context) {
    private var currentApp = "browser"
    
    fun switchApp(appName: String): Boolean {
        return try {
            // Switch app via direct intent
            val success = switchAppViaIntent(appName)
            if (success) {
                currentApp = appName
            }
            success
        } catch (e: Exception) {
            Log.e("AndroidWorkflowEngine", "Error switching app: ${e.message}")
            false
        }
    }

    private fun switchAppViaIntent(appName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(appName)
            launchIntent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
                true
            } ?: false
        } catch (e: Exception) {
            Log.e("AndroidWorkflowEngine", "Error switching app via intent: ${e.message}")
            false
        }
    }

    fun fillForm(fields: Map<String, String>): Boolean {
        // In a real implementation, this would use accessibility service to fill form fields
        // For now, we'll just log the fields
        Log.d("AndroidWorkflowEngine", "Filling form with fields: $fields")
        return true
    }

    fun clickElement(selector: String): Boolean {
        // In a real implementation, this would use accessibility service to click element
        // For now, we'll just log the selector
        Log.d("AndroidWorkflowEngine", "Clicking element with selector: $selector")
        return true
    }

    fun executeWorkflow(steps: List<WorkflowStep>): JSONObject {
        val result = JSONObject()
        result.put("success", true)
        result.put("stepsExecuted", 0)
        
        try {
            for ((index, step) in steps.withIndex()) {
                val stepResult = JSONObject()
                stepResult.put("stepIndex", index)
                stepResult.put("type", step.type)
                
                when (step.type) {
                    "APP_SWITCH" -> {
                        val appName = step.params["appName"] as? String ?: continue
                        val success = switchApp(appName)
                        stepResult.put("success", success)
                        stepResult.put("appName", appName)
                        if (!success) {
                            Log.e("AndroidWorkflowEngine", "Failed to switch to app: $appName")
                            result.put("success", false)
                            result.put("error", "Failed to switch to app: $appName")
                            break
                        }
                    }
                    "FORM_FILL" -> {
                        @Suppress("UNCHECKED_CAST")
                        val fields = step.params["fields"] as? Map<String, String> ?: continue
                        val success = fillForm(fields)
                        stepResult.put("success", success)
                        stepResult.put("fieldCount", fields.size)
                        if (!success) {
                            Log.e("AndroidWorkflowEngine", "Failed to fill form")
                            result.put("success", false)
                            result.put("error", "Failed to fill form")
                            break
                        }
                    }
                    "CLICK" -> {
                        val selector = step.params["selector"] as? String ?: continue
                        val success = clickElement(selector)
                        stepResult.put("success", success)
                        stepResult.put("selector", selector)
                        if (!success) {
                            Log.e("AndroidWorkflowEngine", "Failed to click element: $selector")
                            result.put("success", false)
                            result.put("error", "Failed to click element: $selector")
                            break
                        }
                    }
                    "MCP_EXECUTE" -> {
                        val pieceName = step.params["pieceName"] as? String ?: continue
                        val action = step.params["action"] as? String ?: continue
                        val params = step.params["params"] as? String ?: continue
                        val executionResult = executeMcpPiece(pieceName, action, params)
                        val success = executionResult.contains("\"result\"") // Check if execution was successful
                        stepResult.put("success", success)
                        stepResult.put("pieceName", pieceName)
                        stepResult.put("action", action)
                        stepResult.put("result", executionResult)
                        if (!success) {
                            Log.e("AndroidWorkflowEngine", "Failed to execute MCP piece: $pieceName")
                            // Don't break the workflow if node.js integration is not available, just mark step as failed
                            if (executionResult == "Node.js integration not available") {
                                Log.w("AndroidWorkflowEngine", "Node.js integration not available, continuing workflow")
                            } else {
                                result.put("success", false)
                                result.put("error", "Failed to execute MCP piece: $pieceName - ${executionResult}")
                                break
                            }
                        }
                    }
                    else -> {
                        Log.w("AndroidWorkflowEngine", "Unknown step type: ${step.type}")
                        stepResult.put("success", false)
                        stepResult.put("error", "Unknown step type: ${step.type}")
                    }
                }
                
                result.put("stepsExecuted", index + 1)
            }
        } catch (e: Exception) {
            Log.e("AndroidWorkflowEngine", "Error executing workflow: ${e.message}")
            result.put("success", false)
            result.put("error", "Error executing workflow: ${e.message}")
        }
        
        return result
    }

    // Helper function to parse workflow JSON
    fun parseWorkflowJson(jsonString: String): List<WorkflowStep> {
        val steps = mutableListOf<WorkflowStep>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val stepObj = jsonArray.getJSONObject(i)
                val step = WorkflowStep(
                    type = stepObj.getString("type"),
                    params = convertJsonToMap(stepObj.getJSONObject("params"))
                )
                steps.add(step)
            }
        } catch (e: Exception) {
            Log.e("AndroidWorkflowEngine", "Error parsing workflow JSON: ${e.message}")
        }
        return steps
    }

    private fun convertJsonToMap(jsonObject: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = jsonObject.get(key)
        }
        return map
    }
    
    private fun executeMcpPiece(pieceName: String, action: String, params: String): String {
        // Call the NodeServiceHelper to execute the MCP piece
        return NodeServiceHelper.executeMcpPieceSync(pieceName, action, params)
    }
}

// Data class for workflow steps
data class WorkflowStep(
    val type: String,
    val params: Map<String, Any>
)