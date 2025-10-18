package com.lemonai

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * n8n Integration for workflow automation as described in desire.md
 * This class handles integration with n8n as an npm package for workflow automation
 * with hidden UI where AI agent controls workflows
 */
class N8nIntegration(private val context: Context) {
    private val TAG = "N8nIntegration"
    private var webView: WebView? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Callback interface for handling workflow operations
    interface WorkflowListener {
        fun onWorkflowCreated(workflowId: String)
        fun onWorkflowExecuted(workflowId: String, result: String)
        fun onError(error: String)
        fun onProgress(progress: Float)
    }
    
    private var workflowListener: WorkflowListener? = null
    
    fun setWorkflowListener(listener: WorkflowListener) {
        workflowListener = listener
    }
    
    /**
     * Initialize n8n integration with a WebView
     */
    fun initialize(webView: WebView) {
        this.webView = webView
        
        // Enable JavaScript and set up communication
        webView.settings.javaScriptEnabled = true
        
        // Load n8n or setup environment
        setupN8nEnvironment()
    }
    
    private fun setupN8nEnvironment() {
        webView?.post {
            val jsCode = """
                // Initialize n8n environment
                if (typeof n8n === 'undefined') {
                    window.n8n = {};
                }
                
                // Setup basic n8n functionality
                window.n8n.workflows = {
                    create: function(workflowData) {
                        return new Promise((resolve, reject) => {
                            try {
                                const workflowId = 'workflow_' + Date.now();
                                // Store workflow in a simple in-memory store
                                if (!window.n8n.workflowStore) {
                                    window.n8n.workflowStore = {};
                                }
                                window.n8n.workflowStore[workflowId] = {
                                    id: workflowId,
                                    data: workflowData,
                                    createdAt: new Date().toISOString()
                                };
                                
                                // Notify Android
                                NativeBridge.onWorkflowCreated(workflowId);
                                resolve(workflowId);
                            } catch (error) {
                                reject(error.message);
                            }
                        });
                    },
                    
                    execute: function(workflowId, inputData) {
                        return new Promise((resolve, reject) => {
                            try {
                                const workflow = window.n8n.workflowStore[workflowId];
                                if (!workflow) {
                                    throw new Error('Workflow not found: ' + workflowId);
                                }
                                
                                // Simulate workflow execution
                                // In a real implementation, this would execute the actual n8n workflow
                                const result = {
                                    workflowId: workflowId,
                                    inputData: inputData,
                                    output: "Simulated execution result for workflow: " + workflowId,
                                    executedAt: new Date().toISOString(),
                                    status: "success"
                                };
                                
                                // Notify Android of execution
                                NativeBridge.onWorkflowExecuted(workflowId, JSON.stringify(result));
                                resolve(result);
                            } catch (error) {
                                reject(error.message);
                            }
                        });
                    },
                    
                    update: function(workflowId, workflowData) {
                        return new Promise((resolve, reject) => {
                            try {
                                if (!window.n8n.workflowStore[workflowId]) {
                                    throw new Error('Workflow not found: ' + workflowId);
                                }
                                
                                window.n8n.workflowStore[workflowId].data = workflowData;
                                window.n8n.workflowStore[workflowId].updatedAt = new Date().toISOString();
                                
                                resolve(workflowId);
                            } catch (error) {
                                reject(error.message);
                            }
                        });
                    },
                    
                    delete: function(workflowId) {
                        return new Promise((resolve, reject) => {
                            try {
                                if (!window.n8n.workflowStore[workflowId]) {
                                    throw new Error('Workflow not found: ' + workflowId);
                                }
                                
                                delete window.n8n.workflowStore[workflowId];
                                resolve(workflowId);
                            } catch (error) {
                                reject(error.message);
                            }
                        });
                    },
                    
                    get: function(workflowId) {
                        return new Promise((resolve, reject) => {
                            try {
                                const workflow = window.n8n.workflowStore[workflowId];
                                if (!workflow) {
                                    throw new Error('Workflow not found: ' + workflowId);
                                }
                                resolve(workflow);
                            } catch (error) {
                                reject(error.message);
                            }
                        });
                    },
                    
                    list: function() {
                        return new Promise((resolve, reject) => {
                            try {
                                const workflows = Object.values(window.n8n.workflowStore || {});
                                resolve(workflows);
                            } catch (error) {
                                reject(error.message);
                            }
                        });
                    }
                };
                
                // Notify Android that n8n is ready
                NativeBridge.onN8nReady();
            """.trimIndent()
            
            webView?.evaluateJavascript(jsCode) { result ->
                Log.d(TAG, "n8n environment initialized: $result")
            }
        }
    }
    
    /**
     * Create a new workflow
     */
    fun createWorkflow(workflowData: String, callback: (String) -> Unit) {
        scope.launch {
            try {
                workflowListener?.onProgress(0.1f)
                
                val jsCode = """
                    n8n.workflows.create($workflowData)
                    .then(id => id)
                    .catch(error => 'ERROR: ' + error.message);
                """.trimIndent()
                
                webView?.evaluateJavascript(jsCode) { result ->
                    val cleanResult = result.replace("\"", "")
                    if (cleanResult.startsWith("ERROR:")) {
                        workflowListener?.onError(cleanResult)
                        callback(cleanResult)
                    } else {
                        workflowListener?.onWorkflowCreated(cleanResult)
                        callback(cleanResult)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating workflow", e)
                workflowListener?.onError("Error creating workflow: ${e.message}")
                callback("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Execute a workflow
     */
    fun executeWorkflow(workflowId: String, inputData: String, callback: (String) -> Unit) {
        scope.launch {
            try {
                workflowListener?.onProgress(0.3f)
                
                val jsCode = """
                    n8n.workflows.execute('$workflowId', $inputData)
                    .then(result => JSON.stringify(result))
                    .catch(error => 'ERROR: ' + error.message);
                """.trimIndent()
                
                webView?.evaluateJavascript(jsCode) { result ->
                    val cleanResult = result.replace("\"", "")
                    if (cleanResult.startsWith("ERROR:")) {
                        workflowListener?.onError(cleanResult)
                        callback(cleanResult)
                    } else {
                        workflowListener?.onWorkflowExecuted(workflowId, cleanResult)
                        callback(cleanResult)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing workflow", e)
                workflowListener?.onError("Error executing workflow: ${e.message}")
                callback("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Update an existing workflow
     */
    fun updateWorkflow(workflowId: String, workflowData: String, callback: (String) -> Unit) {
        scope.launch {
            try {
                val jsCode = """
                    n8n.workflows.update('$workflowId', $workflowData)
                    .then(id => id)
                    .catch(error => 'ERROR: ' + error.message);
                """.trimIndent()
                
                webView?.evaluateJavascript(jsCode) { result ->
                    val cleanResult = result.replace("\"", "")
                    if (cleanResult.startsWith("ERROR:")) {
                        workflowListener?.onError(cleanResult)
                        callback(cleanResult)
                    } else {
                        callback(cleanResult)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating workflow", e)
                workflowListener?.onError("Error updating workflow: ${e.message}")
                callback("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Delete a workflow
     */
    fun deleteWorkflow(workflowId: String, callback: (String) -> Unit) {
        scope.launch {
            try {
                val jsCode = """
                    n8n.workflows.delete('$workflowId')
                    .then(id => id)
                    .catch(error => 'ERROR: ' + error.message);
                """.trimIndent()
                
                webView?.evaluateJavascript(jsCode) { result ->
                    val cleanResult = result.replace("\"", "")
                    if (cleanResult.startsWith("ERROR:")) {
                        workflowListener?.onError(cleanResult)
                        callback(cleanResult)
                    } else {
                        callback(cleanResult)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting workflow", e)
                workflowListener?.onError("Error deleting workflow: ${e.message}")
                callback("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Get a workflow by ID
     */
    fun getWorkflow(workflowId: String, callback: (String) -> Unit) {
        scope.launch {
            try {
                val jsCode = """
                    n8n.workflows.get('$workflowId')
                    .then(workflow => JSON.stringify(workflow))
                    .catch(error => 'ERROR: ' + error.message);
                """.trimIndent()
                
                webView?.evaluateJavascript(jsCode) { result ->
                    val cleanResult = result.replace("\"", "")
                    if (cleanResult.startsWith("ERROR:")) {
                        workflowListener?.onError(cleanResult)
                        callback(cleanResult)
                    } else {
                        callback(cleanResult)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting workflow", e)
                workflowListener?.onError("Error getting workflow: ${e.message}")
                callback("Error: ${e.message}")
            }
        }
    }
    
    /**
     * List all workflows
     */
    fun listWorkflows(callback: (String) -> Unit) {
        scope.launch {
            try {
                val jsCode = """
                    n8n.workflows.list()
                    .then(workflows => JSON.stringify(workflows))
                    .catch(error => 'ERROR: ' + error.message);
                """.trimIndent()
                
                webView?.evaluateJavascript(jsCode) { result ->
                    val cleanResult = result.replace("\"", "")
                    if (cleanResult.startsWith("ERROR:")) {
                        workflowListener?.onError(cleanResult)
                        callback(cleanResult)
                    } else {
                        callback(cleanResult)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error listing workflows", e)
                workflowListener?.onError("Error listing workflows: ${e.message}")
                callback("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Notify when n8n is ready
     */
    @JavascriptInterface
    fun onN8nReady() {
        Log.d(TAG, "n8n is ready")
        workflowListener?.onProgress(1.0f)
        workflowListener?.onSuccess("n8n integration initialized successfully")
    }
    
    /**
     * Notify when workflow is created
     */
    @JavascriptInterface
    fun onWorkflowCreated(workflowId: String) {
        Log.d(TAG, "Workflow created: $workflowId")
        workflowListener?.onWorkflowCreated(workflowId)
    }
    
    /**
     * Notify when workflow is executed
     */
    @JavascriptInterface
    fun onWorkflowExecuted(workflowId: String, result: String) {
        Log.d(TAG, "Workflow executed: $workflowId, result: $result")
        workflowListener?.onWorkflowExecuted(workflowId, result)
    }
    
    /**
     * Get the WebView instance (for debugging or direct access)
     */
    fun getWebView(): WebView? = webView
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
    }
}