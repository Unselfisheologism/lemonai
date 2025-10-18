package com.lemonai

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var topBar: LinearLayout
    private lateinit var swipeUpPopup: SwipeUpPopup
    private lateinit var tabSystem: TabSystem
    private lateinit var slashCommandHandler: SlashCommandHandler
    private lateinit var puterJSIntegration: PuterJSIntegration
    private lateinit var wasmSandbox: WASMSandbox
    private lateinit var n8nIntegration: N8nIntegration
    private lateinit var componentConnector: ComponentConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Lock orientation to portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        // Create main layout
        val mainLayout = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Create the WebView layout
        val webViewLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Create top bar with URL input
        topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                resources.getDimension(R.dimen.top_bar_height).toInt()
            )
        }

        urlInput = EditText(this).apply {
            hint = "Enter URL"
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        }

        val goButton = Button(this).apply {
            text = "Go"
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimension(R.dimen.button_width).toInt(),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setOnClickListener {
                val url = if (urlInput.text.toString().startsWith("http")) {
                    urlInput.text.toString()
                } else {
                    "https://" + urlInput.text.toString()
                }
                webView.loadUrl(url)
                
                // Add to tab system
                tabSystem.addTab(url, url, isUsedByAI = false)
            }
        }

        topBar.addView(urlInput)
        topBar.addView(goButton)
        webViewLayout.addView(topBar)

        // Create tab system
        tabSystem = TabSystem(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnTabSelectedListener { tabInfo ->
                webView.loadUrl(tabInfo.url)
            }
        }
        
        webViewLayout.addView(tabSystem)

        // Create WebView
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Log cookies when page loads
                    url?.let {
                        CookieManagerHelper.logAllCookies(it)
                    }
                    
                    // Update current tab title if needed
                    url?.let { currentUrl ->
                        val currentTab = tabSystem.getActiveTab()
                        if (currentTab != null && currentTab.url == currentUrl) {
                            // Update title based on page content
                            view?.evaluateJavascript("document.title") { title ->
                                // Remove quotes added by evaluateJavascript
                                val cleanTitle = title.replace("\"", "")
                                // Update tab title in tab system
                            }
                        } else {
                            // Add new tab if none exists for this URL
                            tabSystem.addTab("New Tab", currentUrl, isUsedByAI = false)
                        }
                    }
                }
            }
            webChromeClient = WebChromeClient()
            
            // Enable JavaScript and add JavaScript interface for communication
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.setAppCacheEnabled(true)
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            
            // Add JavaScript interface for communication bridge
            addJavascriptInterface(NativeCommunicationBridge(this@MainActivity), "NativeBridge")
        }

        webViewLayout.addView(webView)

        // Add WebView layout to main layout
        mainLayout.addView(webViewLayout)

        // Create slash command handler
        slashCommandHandler = SlashCommandHandler(this)
        slashCommandHandler.setSlashCommandListener(object : SlashCommandHandler.SlashCommandListener {
            override fun onSearchCommand(query: String) {
                val searchUrl = "https://www.google.com/search?q=${query.replace(" ", "+")}"
                webView.loadUrl(searchUrl)
                tabSystem.addTab("Search: $query", searchUrl, isUsedByAI = true)
            }

            override fun onAskCommand(query: String) {
                // Show popup and send query to AI
                swipeUpPopup.showPopup()
                swipeUpPopup.updateUserMessage(query)
                swipeUpPopup.updateAIResponseTitles("Thinking...", "Processing your request")
                swipeUpPopup.updateAIResponseContent("I'm processing your query: $query")
            }

            override fun onAutomateCommand(task: String) {
                // Show popup and start automation task
                swipeUpPopup.showPopup()
                swipeUpPopup.updateUserMessage("Automate: $task")
                swipeUpPopup.updateAIResponseTitles("Automation Task", "Setting up workflow")
                swipeUpPopup.updateAIResponseContent("I'll automate this task for you: $task")
            }

            override fun onExpertCommand(expertTask: String) {
                // Show popup and assign to expert
                swipeUpPopup.showPopup()
                swipeUpPopup.updateUserMessage("Expert Task: $expertTask")
                swipeUpPopup.updateAIResponseTitles("Expert Agent", "Assigning to specialist")
                swipeUpPopup.updateAIResponseContent("I'll assign this to the appropriate expert agent: $expertTask")
            }
        })

        // Create and add the slash command view
        val slashCommandView = slashCommandHandler.createCommandView()
        slashCommandView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 100) // Position above the popup area
        }
        mainLayout.addView(slashCommandView)

        // Create and add the swipe-up popup
        swipeUpPopup = SwipeUpPopup(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        mainLayout.addView(swipeUpPopup)

        // Initialize Puter.js integration
        puterJSIntegration = PuterJSIntegration(this)
        puterJSIntegration.setAIResponseListener(object : PuterJSIntegration.AIResponseListener {
            override fun onSuccess(result: String) {
                runOnUiThread {
                    swipeUpPopup.updateAIResponseContent("Puter.js initialized: $result")
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Log.e("PuterJS", "Error: $error")
                }
            }

            override fun onProgress(progress: Float) {
                // Handle progress updates
            }
        })
        puterJSIntegration.initialize(webView)

        // Initialize WASM sandbox
        wasmSandbox = WASMSandbox(this)
        wasmSandbox.setCodeExecutionListener(object : WASMSandbox.CodeExecutionListener {
            override fun onSuccess(output: String) {
                runOnUiThread {
                    swipeUpPopup.updateAIResponseContent("Code executed: $output")
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Log.e("WASMSandbox", "Error: $error")
                }
            }

            override fun onProgress(progress: Float) {
                // Handle progress updates
            }
        })
        wasmSandbox.initialize(webView)

        // Initialize n8n integration
        n8nIntegration = N8nIntegration(this)
        n8nIntegration.setWorkflowListener(object : N8nIntegration.WorkflowListener {
            override fun onWorkflowCreated(workflowId: String) {
                runOnUiThread {
                    Log.d("N8nIntegration", "Workflow created: $workflowId")
                }
            }

            override fun onWorkflowExecuted(workflowId: String, result: String) {
                runOnUiThread {
                    Log.d("N8nIntegration", "Workflow executed: $workflowId, result: $result")
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Log.e("N8nIntegration", "Error: $error")
                }
            }

            override fun onProgress(progress: Float) {
                // Handle progress updates
            }
        })
        n8nIntegration.initialize(webView)

        // Initialize component connector
        componentConnector = ComponentConnector(this)
        componentConnector.setConnectionListener(object : ComponentConnector.ConnectionListener {
            override fun onComponentsConnected() {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "All components connected successfully", Toast.LENGTH_SHORT).show()
                    Log.d("ComponentConnector", "All components connected successfully")
                }
            }

            override fun onConnectionError(component: String, error: String) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Connection error in $component: $error", Toast.LENGTH_LONG).show()
                    Log.e("ComponentConnector", "Connection error in $component: $error")
                }
            }

            override fun onProgress(progress: Float) {
                // Handle connection progress updates
                Log.d("ComponentConnector", "Connection progress: ${(progress * 100).toInt()}%")
            }

            override fun onComponentReady(component: String) {
                runOnUiThread {
                    Log.d("ComponentConnector", "Component ready: $component")
                }
            }
        })
        
        // Connect all components
        componentConnector.connectAllComponents(webView) { success ->
            if (success) {
                Log.d("MainActivity", "Components connected successfully")
            } else {
                Log.e("MainActivity", "Failed to connect components")
            }
        }

        // Load initial page
        webView.loadUrl("https://www.google.com")

        setContentView(mainLayout)
    }

    override fun onBackPressed() {
        if (swipeUpPopup.isPopupVisible()) {
            swipeUpPopup.hidePopup()
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up component connector
        componentConnector.cleanup()
    }
    
    // Communication bridge class for JavaScript to Android communication
    inner class NativeCommunicationBridge(private val context: MainActivity) {
        @JavascriptInterface
        fun getCookiesForDomain(domain: String): String {
            val cookies = CookieManagerHelper.getCookiesForDomain(domain)
            val cookieStringBuilder = StringBuilder()
            for ((key, value) in cookies) {
                cookieStringBuilder.append("$key=$value;")
            }
            return cookieStringBuilder.toString()
        }
        
        @JavascriptInterface
        fun getCookieValue(domain: String, cookieName: String): String {
            return CookieManagerHelper.getCookieValue(domain, cookieName) ?: ""
        }
        
        @JavascriptInterface
        fun setCookie(domain: String, cookieString: String) {
            CookieManagerHelper.setCookie(domain, cookieString)
        }
        
        @JavascriptInterface
        fun logMessage(message: String) {
            Log.d("WebView", message)
        }
        
        @JavascriptInterface
        fun loadUrl(url: String) {
            context.runOnUiThread {
                webView.loadUrl(url)
                
                // Add to tab system
                tabSystem.addTab(url, url, isUsedByAI = true) // AI-initiated navigation
            }
        }
        
        @JavascriptInterface
        fun showPopup() {
            context.runOnUiThread {
                swipeUpPopup.showPopup()
            }
        }
        
        @JavascriptInterface
        fun hidePopup() {
            context.runOnUiThread {
                swipeUpPopup.hidePopup()
            }
        }
        
        @JavascriptInterface
        fun updateUserMessage(message: String) {
            context.runOnUiThread {
                swipeUpPopup.updateUserMessage(message)
            }
        }
        
        @JavascriptInterface
        fun updateAIResponseTitles(title1: String, title2: String) {
            context.runOnUiThread {
                swipeUpPopup.updateAIResponseTitles(title1, title2)
            }
        }
        
        @JavascriptInterface
        fun updateAIResponseContent(content: String) {
            context.runOnUiThread {
                swipeUpPopup.updateAIResponseContent(content)
            }
        }
        
        @JavascriptInterface
        fun addContentToResponse(content: String) {
            context.runOnUiThread {
                // For now, just add the content as text - in a real implementation,
                // this would handle different content types
                val textView = TextView(context)
                textView.text = content
                textView.textSize = 16f
                textView.setPadding(0, 8, 0, 8)
                swipeUpPopup.addContentToResponse(textView)
            }
        }
        
        @JavascriptInterface
        fun executeCode(code: String, language: String): String {
            var result = ""
            wasmSandbox.executeCodeSafely(code, language) { output ->
                result = output
                // Update UI with the result
                context.runOnUiThread {
                    swipeUpPopup.updateAIResponseContent("Code execution result: $output")
                }
            }
            return result
        }
        
        @JavascriptInterface
        fun createWorkflow(workflowData: String): String {
            var result = ""
            n8nIntegration.createWorkflow(workflowData) { workflowId ->
                result = workflowId
            }
            return result
        }
        
        @JavascriptInterface
        fun executeWorkflow(workflowId: String, inputData: String): String {
            var result = ""
            n8nIntegration.executeWorkflow(workflowId, inputData) { executionResult ->
                result = executionResult
            }
            return result
        }
        
        @JavascriptInterface
        fun generateText(prompt: String): String {
            var result = ""
            puterJSIntegration.performAICompletion(prompt) { output ->
                result = output
                // Update UI with the AI response
                context.runOnUiThread {
                    swipeUpPopup.updateAIResponseContent(output)
                }
            }
            return result
        }
        
        @JavascriptInterface
        fun getComponentStatus(): String {
            val status = componentConnector.getComponentStatus()
            val statusString = status.entries.joinToString(", ") { "${it.key}: ${it.value}" }
            return "{ $statusString }"
        }
        
        @JavascriptInterface
        fun connectAllComponents(): String {
            componentConnector.connectAllComponents(webView) { success ->
                if (success) {
                    Log.d("NativeBridge", "Components reconnected successfully")
                } else {
                    Log.e("NativeBridge", "Failed to reconnect components")
                }
            }
            return "Connecting components..."
        }
    }
}