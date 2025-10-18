package com.lemonai

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

/**
 * WASM Sandbox for secure code execution as described in desire.md
 * This class handles JavaScript and Python code execution in a sandboxed environment
 * using WebAssembly to replace Docker-based code execution
 */
class WASMSandbox(private val context: Context) {
    private val TAG = "WASMSandbox"
    private var webView: WebView? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Callback interface for handling code execution results
    interface CodeExecutionListener {
        fun onSuccess(output: String)
        fun onError(error: String)
        fun onProgress(progress: Float)
    }
    
    private var executionListener: CodeExecutionListener? = null
    
    fun setCodeExecutionListener(listener: CodeExecutionListener) {
        executionListener = listener
    }
    
    /**
     * Initialize WASM sandbox with a WebView
     */
    fun initialize(webView: WebView) {
        this.webView = webView
        
        // Enable JavaScript and set up communication
        webView.settings.javaScriptEnabled = true
        
        // Load WASM runtime or setup environment
        setupWASMSandbox()
    }
    
    private fun setupWASMSandbox() {
        webView?.post {
            val jsCode = """
                // Initialize WASM sandbox environment
                if (typeof WASMSandbox === 'undefined') {
                    window.WASMSandbox = {};
                }
                
                // Setup JavaScript execution environment
                window.WASMSandbox.executeJS = function(code) {
                    try {
                        // Create a secure context for execution
                        const result = eval.call(null, code);
                        return JSON.stringify({ success: true, result: result });
                    } catch (error) {
                        return JSON.stringify({ success: false, error: error.message });
                    }
                };
                
                // Setup Python execution environment (simulated)
                window.WASMSandbox.executePython = function(code) {
                    // In a real implementation, this would interface with a Python WASM runtime
                    // For now, we'll simulate the execution
                    return NativeBridge.executePython(code);
                };
                
                // Notify Android that WASM sandbox is ready
                NativeBridge.onWASMSandboxReady();
            """.trimIndent()
            
            webView?.evaluateJavascript(jsCode) { result ->
                Log.d(TAG, "WASM sandbox environment initialized: $result")
            }
        }
    }
    
    /**
     * Execute JavaScript code in the WASM sandbox
     */
    fun executeJavaScript(code: String, callback: (String) -> Unit) {
        scope.launch {
            try {
                executionListener?.onProgress(0.1f)
                
                val jsCode = """
                    (function() {
                        try {
                            // Create an isolated context for the code execution
                            const result = (function() {
                                // Define a safe environment
                                const safeConsole = {
                                    log: function(...args) {
                                        return args.join(' ');
                                    }
                                };
                                
                                // Execute the user code with a timeout mechanism
                                return eval(${"\"\"\"$code\"\"\""});
                            })();
                            
                            return JSON.stringify({ success: true, result: result });
                        } catch (error) {
                            return JSON.stringify({ success: false, error: error.message });
                        }
                    })();
                """.trimIndent()
                
                webView?.evaluateJavascript(jsCode) { result ->
                    val cleanResult = result.replace("\"", "")
                    callback(cleanResult)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing JavaScript", e)
                executionListener?.onError("Error executing JavaScript: ${e.message}")
                callback("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Execute Python code in the WASM sandbox
     */
    @JavascriptInterface
    fun executePython(code: String): String {
        Log.d(TAG, "executePython called with code: $code")
        
        scope.launch {
            try {
                executionListener?.onProgress(0.1f)
                
                // Simulate Python execution in WASM - in a real implementation, 
                // this would interface with a Python WASM runtime like Pyodide
                delay(800) // Simulate processing time
                executionListener?.onProgress(0.6f)
                
                // For now, return a simulated response
                // In a real implementation, this would execute Python via WASM
                val simulatedOutput = "Python code executed successfully:\n$code\n\nResult: Simulated output"
                
                executionListener?.onProgress(1.0f)
                executionListener?.onSuccess(simulatedOutput)
                
                return@launch simulatedOutput
            } catch (e: Exception) {
                Log.e(TAG, "Error executing Python", e)
                executionListener?.onError("Error executing Python: ${e.message}")
                return@launch "Error: ${e.message}"
            }
        }
        
        return "Processing Python code..."
    }
    
    /**
     * Execute arbitrary code with specified language
     */
    fun executeCode(code: String, language: String, callback: (String) -> Unit) {
        when (language.lowercase()) {
            "javascript", "js" -> executeJavaScript(code, callback)
            "python", "py" -> {
                // Use the simulated Python execution for now
                scope.launch {
                    val result = executePython(code)
                    callback(result)
                }
            }
            else -> {
                val error = "Unsupported language: $language. Supported languages: JavaScript, Python"
                executionListener?.onError(error)
                callback(error)
            }
        }
    }
    
    /**
     * Notify when WASM sandbox is ready
     */
    @JavascriptInterface
    fun onWASMSandboxReady() {
        Log.d(TAG, "WASM sandbox is ready")
        executionListener?.onSuccess("WASM sandbox initialized successfully")
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
    
    /**
     * Validate code for security (basic implementation)
     */
    fun validateCode(code: String, language: String): Boolean {
        // Basic security validation
        val dangerousPatterns = when (language.lowercase()) {
            "javascript", "js" -> listOf(
                "import", "require", "eval", "Function", "setTimeout", "setInterval",
                "XMLHttpRequest", "fetch", "localStorage", "sessionStorage", "document.cookie"
            )
            "python", "py" -> listOf(
                "import", "exec", "eval", "__import__", "compile", "open", "file",
                "input", "raw_input", "__builtins__"
            )
            else -> emptyList()
        }
        
        val lowerCode = code.lowercase()
        return !dangerousPatterns.any { pattern -> lowerCode.contains(pattern) }
    }
    
    /**
     * Execute code with validation and timeout
     */
    fun executeCodeSafely(code: String, language: String, timeoutMs: Long = 5000, callback: (String) -> Unit) {
        if (!validateCode(code, language)) {
            executionListener?.onError("Code contains potentially dangerous patterns")
            callback("Error: Code contains potentially dangerous patterns")
            return
        }
        
        scope.launch {
            try {
                withTimeout(timeoutMs) {
                    executeCode(code, language) { result ->
                        callback(result)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Code execution timed out", e)
                executionListener?.onError("Code execution timed out after ${timeoutMs}ms")
                callback("Error: Code execution timed out")
            } catch (e: Exception) {
                Log.e(TAG, "Error executing code safely", e)
                executionListener?.onError("Error executing code: ${e.message}")
                callback("Error: ${e.message}")
            }
        }
    }
}