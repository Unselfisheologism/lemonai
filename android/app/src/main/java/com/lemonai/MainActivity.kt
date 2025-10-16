// MainActivity.kt
package com.lemonai

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize nodejs-mobile for MCP background service
        NodeServiceHelper.init(this)
        NodeServiceHelper.startMcpServer()
        
        // Initialize WebView
        webView = findViewById(R.id.webview)
        setupWebView()
        
        // Load LemonAI UI from assets
        webView.loadUrl("file:///android_asset/www/index.html")
        
        // Initialize Accessibility Service for UI automation
        setupAccessibilityService()
        
        // Test workflow execution
        testWorkflowExecution()
    }
    
    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.setAppCacheEnabled(true)
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.allowFileAccessFromFileURLs = true
        
        // Set custom WebViewClient to handle navigation
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Handle navigation within the app
                return false
            }
        }
        
        // Add JavaScript interface for native communication
        webView.addJavascriptInterface(NativeBridge(this), "NativeBridge")
    }
    
    private fun setupAccessibilityService() {
        // Check if accessibility service is enabled
        if (!isAccessibilityServiceEnabled()) {
            // Request accessibility permission
            requestAccessibilityPermission()
        }
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "$packageName/.LemonAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service) == true
    }
    
    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
    
    private fun testWorkflowExecution() {
        // Run workflow test in background thread
        Thread {
            try {
                WorkflowTest.testGmailAndNotionWorkflow(this)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error in workflow test", e)
            }
        }.start()
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}