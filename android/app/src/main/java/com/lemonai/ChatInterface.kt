package com.lemonai

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.*

/**
 * Mobile-optimized chat interface as described in UI-description.md
 * Features:
 * - User message identification at the top
 * - Two lines below user's message (likely indicating title of AI response)
 * - Large rectangular content area for AI agent's actual response
 * - Input area with buttons at the bottom
 */
class ChatInterface : LinearLayout {
    
    private var userMessageText: TextView? = null
    private var aiResponseTitle1: TextView? = null
    private var aiResponseTitle2: TextView? = null
    private var aiResponseContent: ScrollView? = null
    private var aiResponseContentContainer: LinearLayout? = null
    private var plusButton: ImageButton? = null
    private var settingsButton: ImageButton? = null
    private var chatInput: EditText? = null
    
    constructor(context: Context) : super(context) {
        init()
    }
    
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    
    private fun init() {
        orientation = VERTICAL
        
        // Create the top area with user message and AI response titles
        val topArea = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                0.4f // 40% of the chat area for the top section
            )
            setPadding(16, 16, 8)
        }
        
        // User message identification
        userMessageText = TextView(context).apply {
            text = "USER <- user's message to the AI"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // Two lines below user's message (AI response titles)
        aiResponseTitle1 = TextView(context).apply {
            text = "Processing your request..."
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black))
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 4)
            }
        }
        
        aiResponseTitle2 = TextView(context).apply {
            text = "Analyzing options"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        }
        
        topArea.addView(userMessageText)
        topArea.addView(aiResponseTitle1)
        topArea.addView(aiResponseTitle2)
        
        // AI response content area (large rectangular content area)
        aiResponseContentContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        aiResponseContent = ScrollView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                0.6f // 60% of the chat area for response content
            )
            addView(aiResponseContentContainer)
        }
        
        // Input area with buttons at the bottom
        val inputArea = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                resources.getDimension(R.dimen.input_height).toInt()
            )
            setPadding(16, 8, 16, 16)
        }
        
        // Plus button
        plusButton = ImageButton(context).apply {
            setImageResource(R.drawable.ic_plus)
            layoutParams = LayoutParams(
                resources.getDimension(R.dimen.button_size).toInt(),
                resources.getDimension(R.dimen.button_size).toInt()
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundResource(R.drawable.button_background)
        }
        
        // Settings button
        settingsButton = ImageButton(context).apply {
            setImageResource(R.drawable.ic_settings)
            layoutParams = LayoutParams(
                resources.getDimension(R.dimen.button_size).toInt(),
                resources.getDimension(R.dimen.button_size).toInt()
            ).apply {
                setMargins(8, 0, 0)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundResource(R.drawable.button_background)
        }
        
        // Chat input
        chatInput = EditText(context).apply {
            hint = "Type followup..."
            layoutParams = LayoutParams(
                0,
                LayoutParams.MATCH_PARENT,
                1f
            ).apply {
                setMargins(8, 0, 8, 0)
            }
            setBackgroundResource(R.drawable.input_background)
            setPadding(16, 8, 16, 8)
        }
        
        inputArea.addView(plusButton)
        inputArea.addView(settingsButton)
        inputArea.addView(chatInput)
        
        // Add all components to the main layout
        addView(topArea)
        addView(aiResponseContent)
        addView(inputArea)
    }
    
    // Function to update the user message
    fun updateUserMessage(message: String) {
        userMessageText?.text = "USER <- $message"
    }
    
    // Function to update AI response titles
    fun updateAIResponseTitles(title1: String, title2: String) {
        aiResponseTitle1?.text = title1
        aiResponseTitle2?.text = title2
    }
    
    // Function to update AI response content
    fun updateAIResponseContent(content: String) {
        aiResponseContentContainer?.removeAllViews()
        
        val contentText = TextView(context).apply {
            text = content
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(0, 8, 0, 8)
        }
        
        aiResponseContentContainer?.addView(contentText)
    }
    
    // Function to add content to the response area (for multimodal content)
    fun addContentToResponse(contentView: android.view.View) {
        aiResponseContentContainer?.addView(contentView)
    }
}