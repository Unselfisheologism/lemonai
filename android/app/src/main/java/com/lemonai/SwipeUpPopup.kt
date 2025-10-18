package com.lemonai

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlin.math.abs

/**
 * A swipe-up popup interface that overlays the main UI as described in UI-description.md
 */
class SwipeUpPopup : FrameLayout {
    
    private var popupContainer: View? = null
    private var chatInterface: ChatInterface? = null
    private var isPopupVisible = false
    private var startY = 0f
    private var currentY = 0f
    
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
        // Create the popup container
        popupContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                (resources.getDimension(R.dimen.popup_height) * 0.75).toInt() // Three-fourth of screen as per UI-description
            ).apply {
                gravity = android.view.Gravity.BOTTOM
            }
            
            // Make it look like a popup overlay
            setBackgroundResource(R.drawable.popup_background)
            elevation = 16f
        }
        
        // Create the chat interface
        chatInterface = ChatInterface(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }
        
        popupContainer?.addView(chatInterface)
        
        // Add the popup container to this view
        addView(popupContainer)
        
        // Initially hide the popup
        popupContainer?.visibility = View.GONE
        
        // Add touch listener for swipe gestures
        setOnTouchListener { _, event ->
            handleSwipe(event)
            true
        }
    }
    
    private fun handleSwipe(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startY = event.rawY
                currentY = startY
            }
            MotionEvent.ACTION_MOVE -> {
                currentY = event.rawY
                val diffY = startY - currentY
                
                // Only respond to upward swipes
                if (diffY > 0) {
                    // Move the popup up based on swipe distance
                    val translationY = if (isPopupVisible) {
                        // If popup is visible, swipe down to hide
                        (popupContainer?.height ?: 0) - diffY
                    } else {
                        // If popup is hidden, swipe up to show
                        -diffY
                    }
                    
                    // Limit the translation to the popup height
                    val maxTranslation = if (isPopupVisible) 0f else -(popupContainer?.height?.toFloat() ?: 0f)
                    val finalTranslation = translationY.coerceIn(maxTranslation, 0f)
                    
                    popupContainer?.translationY = finalTranslation
                    
                    // Show popup if we've swiped a significant distance
                    if (!isPopupVisible && abs(diffY) > 100) {
                        showPopup()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                // Animate to final position based on swipe distance
                val diffY = startY - currentY
                if (abs(diffY) > 10) { // Significant swipe
                    if (isPopupVisible) {
                        if (diffY > 0) {
                            // Swipe down to hide
                            hidePopup()
                        } else {
                            // Swipe up to stay visible
                            showPopup()
                        }
                    } else {
                        if (diffY < -10) {
                            // Strong upward swipe to show
                            showPopup()
                        } else {
                            // Weak upward swipe, return to hidden
                            popupContainer?.translationY = 0f
                        }
                    }
                } else {
                    // Not a significant swipe, animate back to original position
                    if (isPopupVisible) {
                        showPopup()
                    } else {
                        popupContainer?.translationY = 0f
                    }
                }
            }
        }
        return true
    }
    
    fun showPopup() {
        isPopupVisible = true
        popupContainer?.visibility = View.VISIBLE
        popupContainer?.translationY = 0f
    }
    
    fun hidePopup() {
        isPopupVisible = false
        popupContainer?.translationY = (popupContainer?.height?.toFloat() ?: 0f)
        // Delay the visibility change to allow for animation
        post {
            popupContainer?.visibility = View.GONE
        }
    }
    
    fun isPopupVisible(): Boolean {
        return isPopupVisible
    }
    
    // Function to update the user message in the chat interface
    fun updateUserMessage(message: String) {
        chatInterface?.updateUserMessage(message)
    }
    
    // Function to update AI response titles
    fun updateAIResponseTitles(title1: String, title2: String) {
        chatInterface?.updateAIResponseTitles(title1, title2)
    }
    
    // Function to update AI response content
    fun updateAIResponseContent(content: String) {
        chatInterface?.updateAIResponseContent(content)
    }
    
    // Function to add content to the response area (for multimodal content)
    fun addContentToResponse(contentView: android.view.View) {
        chatInterface?.addContentToResponse(contentView)
    }
}