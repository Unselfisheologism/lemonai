package com.lemonai

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import android.view.ViewGroup
import android.util.TypedValue
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.EditText

/**
 * Handles slash commands as described in UI-description.md
 * Features:
 * - /search (for regular google search)
 * - /ask (for asking something to the ai agent)
 * - /automate (for giving a task to the ai agent)
 * - /expert (for assigning tasks to the expert agents)
 */
class SlashCommandHandler(private val context: Context) {
    
    interface SlashCommandListener {
        fun onSearchCommand(query: String)
        fun onAskCommand(query: String)
        fun onAutomateCommand(task: String)
        fun onExpertCommand(expertTask: String)
    }
    
    private var commandListener: SlashCommandListener? = null
    private var commandInput: EditText? = null
    private var commandContainer: FrameLayout? = null
    private var isCommandMode = false
    
    fun setSlashCommandListener(listener: SlashCommandListener) {
        commandListener = listener
    }
    
    fun createCommandView(): FrameLayout {
        commandContainer = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            
            // Large rectangular box with slash command options
            val commandBox = TextView(context).apply {
                text = "Select a command: /search /ask /automate /expert"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(Color.BLACK)
                setPadding(32, 32, 32, 32)
                setBackgroundResource(R.drawable.input_background)
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(32, 32, 32, 32)
                }
                
                setOnClickListener {
                    showCommandInput()
                }
            }
            
            addView(commandBox)
        }
        
        return commandContainer!!
    }
    
    private fun showCommandInput() {
        commandContainer?.removeAllViews()
        
        // Create input area with command prompt
        val inputLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 32, 32)
            }
            setBackgroundResource(R.drawable.input_background)
            setPadding(16, 16, 16, 16)
        }
        
        commandInput = EditText(context).apply {
            hint = "Enter your command (/search, /ask, /automate, /expert)..."
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        
        val buttonLayout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val sendButton = Button(context).apply {
            text = "Send"
            setBackgroundColor(Color.parseColor("#4A90E2"))
            setTextColor(Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener {
                processCommand()
            }
        }
        
        val cancelButton = Button(context).apply {
            text = "Cancel"
            setBackgroundColor(Color.GRAY)
            setTextColor(Color.WHITE)
            layoutParams = android.widget.LinearLayout.LayoutParams(
                0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(8, 0, 0, 0)
            }
            setOnClickListener {
                hideCommandInput()
            }
        }
        
        buttonLayout.addView(sendButton)
        buttonLayout.addView(cancelButton)
        
        inputLayout.addView(commandInput)
        inputLayout.addView(buttonLayout)
        
        commandContainer?.addView(inputLayout)
        commandInput?.requestFocus()
    }
    
    private fun hideCommandInput() {
        commandContainer?.removeAllViews()
        val commandBox = TextView(context).apply {
            text = "Select a command: /search /ask /automate /expert"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(Color.BLACK)
            setPadding(32, 32, 32, 32)
            setBackgroundResource(R.drawable.input_background)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(32, 32, 32, 32)
            }
            
            setOnClickListener {
                showCommandInput()
            }
        }
        
        commandContainer?.addView(commandBox)
    }
    
    private fun processCommand() {
        val inputText = commandInput?.text.toString().trim()
        if (inputText.isEmpty()) return
        
        when {
            inputText.startsWith("/search", ignoreCase = true) -> {
                val query = inputText.substring("/search".length).trim()
                commandListener?.onSearchCommand(query)
            }
            inputText.startsWith("/ask", ignoreCase = true) -> {
                val query = inputText.substring("/ask".length).trim()
                commandListener?.onAskCommand(query)
            }
            inputText.startsWith("/automate", ignoreCase = true) -> {
                val task = inputText.substring("/automate".length).trim()
                commandListener?.onAutomateCommand(task)
            }
            inputText.startsWith("/expert", ignoreCase = true) -> {
                val expertTask = inputText.substring("/expert".length).trim()
                commandListener?.onExpertCommand(expertTask)
            }
            else -> {
                // If it doesn't start with a slash command, treat as a general ask command
                commandListener?.onAskCommand(inputText)
            }
        }
        
        hideCommandInput()
    }
    
    fun executeCommand(command: String, params: String) {
        when (command.lowercase()) {
            "search" -> commandListener?.onSearchCommand(params)
            "ask" -> commandListener?.onAskCommand(params)
            "automate" -> commandListener?.onAutomateCommand(params)
            "expert" -> commandListener?.onExpertCommand(params)
        }
    }
}