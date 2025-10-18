package com.lemonai

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.app.AlertDialog
import android.view.accessibility.AccessibilityEvent

/**
 * Google-assistant-like home-button activation as described in UI-description.md
 * This feature creates a floating button that can be activated like Google Assistant
 * to trigger the AI agent
 */
class HomeButtonActivation(private val context: Context) {
    private val TAG = "HomeButtonActivation"
    
    private var floatingButton: ImageButton? = null
    private var windowManager: WindowManager? = null
    private var isButtonAdded = false
    
    // Callback interface for handling activation events
    interface ActivationListener {
        fun onActivated()
        fun onDeactivated()
        fun onError(error: String)
    }
    
    private var activationListener: ActivationListener? = null
    
    fun setActivationListener(listener: ActivationListener) {
        activationListener = listener
    }
    
    /**
     * Check if we have the required permissions to create a floating button
     */
    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    /**
     * Request the required permissions to create a floating button
     */
    fun requestPermissions(activity: android.app.Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${activity.packageName}")
                )
                activity.startActivityForResult(intent, requestCode)
            }
        }
    }
    
    /**
     * Create and show the floating home button
     */
    fun createFloatingButton() {
        if (!hasRequiredPermissions()) {
            Log.e(TAG, "Missing required permissions to create floating button")
            activationListener?.onError("Missing required permissions to create floating button")
            return
        }
        
        if (isButtonAdded) {
            Log.w(TAG, "Floating button already added")
            return
        }
        
        // Initialize window manager
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Create floating button
        floatingButton = ImageButton(context).apply {
            // Set a simple circle as the button background
            setBackgroundResource(R.drawable.button_background)
            setImageResource(R.drawable.ic_settings) // Using settings icon as default
            contentDescription = "AI Assistant Button"
            
            // Set button size
            layoutParams = WindowManager.LayoutParams(
                100, // Width
                100, // Height
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            
            // Position the button at the bottom right corner
            (layoutParams as WindowManager.LayoutParams).gravity = Gravity.BOTTOM or Gravity.END
            (layoutParams as WindowManager.LayoutParams).x = 32
            (layoutParams as WindowManager.LayoutParams).y = 32
            
            // Set click listener
            setOnClickListener {
                activateAssistant()
            }
            
            // Set long click listener for settings
            setOnLongClickListener {
                showSettingsDialog()
                true
            }
        }
        
        // Add button to window
        try {
            windowManager?.addView(floatingButton, floatingButton?.layoutParams)
            isButtonAdded = true
            Log.d(TAG, "Floating button added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding floating button", e)
            activationListener?.onError("Error adding floating button: ${e.message}")
        }
    }
    
    /**
     * Remove the floating button
     */
    fun removeFloatingButton() {
        if (isButtonAdded && floatingButton != null) {
            try {
                windowManager?.removeView(floatingButton)
                isButtonAdded = false
                Log.d(TAG, "Floating button removed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing floating button", e)
            }
        }
    }
    
    /**
     * Activate the assistant (trigger the AI agent)
     */
    private fun activateAssistant() {
        Log.d(TAG, "Assistant activated")
        activationListener?.onActivated()
        
        // Show a toast to indicate activation
        Toast.makeText(context, "AI Assistant Activated", Toast.LENGTH_SHORT).show()
        
        // In a real implementation, this would trigger the AI agent
        // For now, we'll just send a broadcast or start an activity
        val intent = Intent("com.lemonai.ASSISTANT_ACTIVATED")
        intent.putExtra("timestamp", System.currentTimeMillis())
        context.sendBroadcast(intent)
    }
    
    /**
     * Show settings dialog for the floating button
     */
    private fun showSettingsDialog(): Boolean {
        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32)
        }
        
        val title = TextView(context).apply {
            text = "AI Assistant Settings"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 32)
            }
        }
        
        val positionLabel = TextView(context).apply {
            text = "Button Position:"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 8)
            }
        }
        
        val positionGroup = RadioGroup(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val bottomRightOption = RadioButton(context).apply {
            text = "Bottom Right"
            isChecked = true
        }
        
        val topLeftOption = RadioButton(context).apply {
            text = "Top Left"
        }
        
        val bottomLeftOption = RadioButton(context).apply {
            text = "Bottom Left"
        }
        
        val topRightOption = RadioButton(context).apply {
            text = "Top Right"
        }
        
        positionGroup.addView(bottomRightOption)
        positionGroup.addView(topLeftOption)
        positionGroup.addView(bottomLeftOption)
        positionGroup.addView(topRightOption)
        
        val sizeLabel = TextView(context).apply {
            text = "Button Size:"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 8)
            }
        }
        
        val sizeSeekbar = SeekBar(context).apply {
            max = 100
            progress = 50
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val sizeValue = TextView(context).apply {
            text = "Medium"
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 16)
            }
        }
        
        sizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sizeValue.text = when {
                    progress < 33 -> "Small"
                    progress < 66 -> "Medium"
                    else -> "Large"
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        val okButton = Button(context).apply {
            text = "OK"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 32, 0, 0)
            }
        }
        
        dialogView.addView(title)
        dialogView.addView(positionLabel)
        dialogView.addView(positionGroup)
        dialogView.addView(sizeLabel)
        dialogView.addView(sizeSeekbar)
        dialogView.addView(sizeValue)
        dialogView.addView(okButton)
        
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
        
        okButton.setOnClickListener {
            dialog.dismiss()
            // Apply settings would go here
        }
        
        dialog.show()
        return true
    }
    
    /**
     * Move the floating button to a specific position
     */
    fun moveButtonToPosition(x: Int, y: Int) {
        if (!isButtonAdded) return
        
        val params = floatingButton?.layoutParams as? WindowManager.LayoutParams
        params?.x = x
        params?.y = y
        windowManager?.updateViewLayout(floatingButton, params)
    }
    
    /**
     * Change the floating button size
     */
    fun resizeButton(width: Int, height: Int) {
        if (!isButtonAdded) return
        
        val params = floatingButton?.layoutParams as? WindowManager.LayoutParams
        params?.width = width
        params?.height = height
        windowManager?.updateViewLayout(floatingButton, params)
    }
    
    /**
     * Show or hide the floating button
     */
    fun setButtonVisibility(visible: Boolean) {
        if (!isButtonAdded) return
        
        floatingButton?.visibility = if (visible) View.VISIBLE else View.GONE
    }
    
    /**
     * Check if the floating button is visible
     */
    fun isButtonVisible(): Boolean {
        return if (isButtonAdded) {
            floatingButton?.visibility == View.VISIBLE
        } else {
            false
        }
    }
    
    /**
     * Update the button icon
     */
    fun updateButtonIcon(iconResId: Int) {
        if (!isButtonAdded) return
        
        floatingButton?.setImageResource(iconResId)
    }
    
    /**
     * Animate the button (pulse effect)
     */
    fun pulseButton() {
        if (!isButtonAdded) return
        
        // Simple scaling animation
        floatingButton?.animate()
            ?.scaleX(1.2f)
            ?.scaleY(1.2f)
            ?.setDuration(200)
            ?.withEndAction {
                floatingButton?.animate()
                    ?.scaleX(1.0f)
                    ?.scaleY(1.0f)
                    ?.setDuration(200)
                    ?.start()
            }
            ?.start()
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        removeFloatingButton()
        windowManager = null
        floatingButton = null
        activationListener = null
    }
    
    /**
     * Check if the assistant is currently active
     */
    fun isAssistantActive(): Boolean {
        // In a real implementation, this would check if the AI agent is currently processing
        return false
    }
    
    /**
     * Simulate assistant speaking (for demonstration)
     */
    fun simulateAssistantSpeaking(text: String) {
        // In a real implementation, this would use TTS to speak the text
        Toast.makeText(context, "Assistant: $text", Toast.LENGTH_LONG).show()
        
        // Send an accessibility event for screen readers
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        event.text.add(text)
        event.className = this::class.java.name
        // In a real implementation, you would send this to the accessibility service
    }
}