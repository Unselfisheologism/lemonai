// WorkflowTest.kt
package com.lemonai

import android.content.Context
import android.util.Log
import org.json.JSONObject

class WorkflowTest {
    companion object {
        private const val TAG = "WorkflowTest"
        
        fun testGmailAndNotionWorkflow(context: Context) {
            Log.d(TAG, "Testing Gmail and Notion workflow execution...")
            
            // Initialize the workflow engine
            val workflowEngine = AndroidWorkflowEngine(context)
            
            // Test Gmail workflow
            val gmailWorkflow = listOf(
                WorkflowStep(
                    type = "APP_SWITCH",
                    params = mapOf("appName" to "com.google.android.gm")
                ),
                WorkflowStep(
                    type = "FORM_FILL",
                    params = mapOf("fields" to mapOf(
                        "to" to "test@example.com",
                        "subject" to "Test Email",
                        "body" to "This is a test email from LemonAI"
                    ))
                ),
                WorkflowStep(
                    type = "CLICK",
                    params = mapOf("selector" to "sendButton")
                )
            )
            
            // Test Notion workflow
            val notionWorkflow = listOf(
                WorkflowStep(
                    type = "APP_SWITCH",
                    params = mapOf("appName" to "notion.id")
                ),
                WorkflowStep(
                    type = "FORM_FILL",
                    params = mapOf("fields" to mapOf(
                        "pageTitle" to "Test Page from LemonAI",
                        "pageContent" to "This is a test page created by LemonAI"
                    ))
                ),
                WorkflowStep(
                    type = "CLICK",
                    params = mapOf("selector" to "publishButton")
                )
            )
            
            // Test Gmail workflow execution
            val gmailResult = workflowEngine.executeWorkflow(gmailWorkflow)
            Log.d(TAG, "Gmail workflow result: $gmailResult")
            
            // Test Notion workflow execution
            val notionResult = workflowEngine.executeWorkflow(notionWorkflow)
            Log.d(TAG, "Notion workflow result: $notionResult")
            
            Log.d(TAG, "Workflow test completed")
        }
    }
}