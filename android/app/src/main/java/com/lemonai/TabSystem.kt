package com.lemonai

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.cardview.widget.CardView

/**
 * Browser-like tab system with previews as described in UI-description.md
 * Features:
 * - Tab previews in squares
 * - Indicators for user vs AI usage
 * - Status indicators (active/dormant)
 * - Vertical arrangement for mobile UX
 */
class TabSystem : LinearLayout {
    
    private var tabContainer: LinearLayout? = null
    private var tabs: MutableList<TabInfo> = mutableListOf()
    private var onTabSelectedListener: ((TabInfo) -> Unit)? = null
    private var maxTabs = 6 // As mentioned in UI-description.md
    
    data class TabInfo(
        val id: String,
        val title: String,
        val url: String,
        val isUsedByAI: Boolean = false,
        var isActive: Boolean = false
    )
    
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
        
        // Create title for the tab system
        val titleText = TextView(context).apply {
            text = "Tabs"
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(16, 16, 16, 8)
        }
        
        // Create the container for tabs
        tabContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            // Set horizontal scroll to allow more tabs than fit on screen
            isHorizontalScrollBarEnabled = false
        }
        
        val tabScrollView = HorizontalScrollView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                resources.getDimension(R.dimen.tab_height).toInt() * 2 // Allow for two rows of tabs
            )
            addView(tabContainer)
        }
        
        addView(titleText)
        addView(tabScrollView)
    }
    
    fun addTab(title: String, url: String, isUsedByAI: Boolean = false) {
        if (tabs.size >= maxTabs) {
            // Remove the oldest tab if we've reached the limit
            tabs.removeAt(0)
            tabContainer?.removeViewAt(0)
        }
        
        val tabId = "tab_${tabs.size}"
        val tabInfo = TabInfo(tabId, title, url, isUsedByAI, isActive = true)
        tabs.add(tabInfo)
        
        val tabView = createTabView(tabInfo)
        tabContainer?.addView(tabView)
        
        // Update the WebView to the new tab if it's the first one
        if (tabs.size == 1) {
            onTabSelectedListener?.invoke(tabInfo)
        }
    }
    
    private fun createTabView(tabInfo: TabInfo): View {
        val cardView = CardView(context).apply {
            val cardParams = LinearLayout.LayoutParams(
                resources.getDimension(R.dimen.tab_width).toInt(),
                resources.getDimension(R.dimen.tab_height).toInt()
            ).apply {
                setMargins(8, 8, 8, 8)
            }
            layoutParams = cardParams
            
            radius = 8f
            cardElevation = 4f
            setCardBackgroundColor(resources.getColor(android.R.color.white))
            setOnClickListener {
                // Update active status for all tabs
                tabs.forEach { it.isActive = (it.id == tabInfo.id) }
                onTabSelectedListener?.invoke(tabInfo)
                updateTabIndicators()
            }
        }
        
        val tabLayout = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            setPadding(8, 8, 8, 8)
        }
        
        // Top row: User/AI indicator and status indicator
        val topRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // User/AI indicator
        val userIndicator = ImageView(context).apply {
            val indicatorRes = if (tabInfo.isUsedByAI) {
                R.drawable.ic_settings // AI indicator (using settings icon)
            } else {
                R.drawable.ic_settings // User indicator (using account icon)
            }
            setImageResource(indicatorRes)
            layoutParams = LayoutParams(
                resources.getDimension(R.dimen.indicator_size).toInt(),
                resources.getDimension(R.dimen.indicator_size).toInt()
            )
        }
        
        // Status indicator (green for active, yellow for dormant)
        val statusIndicator = View(context).apply {
            val statusColor = if (tabInfo.isActive) {
                resources.getColor(android.R.color.holo_green_dark)
            } else {
                resources.getColor(android.R.color.holo_orange_light)
            }
            setBackgroundColor(statusColor)
            layoutParams = LayoutParams(
                resources.getDimension(R.dimen.indicator_size).toInt(),
                resources.getDimension(R.dimen.indicator_size).toInt()
            ).apply {
                gravity = android.view.Gravity.END
            }
        }
        
        topRow.addView(userIndicator)
        topRow.addView(statusIndicator)
        
        // Tab title
        val titleText = TextView(context).apply {
            text = if (tabInfo.title.length > 20) "${tabInfo.title.substring(0, 17)}..." else tabInfo.title
            textSize = 12f
            setTextColor(resources.getColor(android.R.color.black))
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
        }
        
        tabLayout.addView(topRow)
        tabLayout.addView(titleText)
        
        cardView.addView(tabLayout)
        return cardView
    }
    
    private fun updateTabIndicators() {
        // Update all tab views to reflect current active state
        for (i in 0 until tabContainer?.childCount ?: 0) {
            val cardView = tabContainer?.getChildAt(i) as? CardView
            val tabLayout = cardView?.getChildAt(0) as? LinearLayout
            val topRow = tabLayout?.getChildAt(0) as? LinearLayout
            val statusIndicator = topRow?.getChildAt(1)
            
            if (i < tabs.size) {
                val tabInfo = tabs[i]
                val statusColor = if (tabInfo.isActive) {
                    resources.getColor(android.R.color.holo_green_dark)
                } else {
                    resources.getColor(android.R.color.holo_orange_light)
                }
                statusIndicator?.setBackgroundColor(statusColor)
            }
        }
    }
    
    fun setOnTabSelectedListener(listener: (TabInfo) -> Unit) {
        this.onTabSelectedListener = listener
    }
    
    fun getActiveTab(): TabInfo? {
        return tabs.find { it.isActive }
    }
    
    fun closeTab(tabId: String) {
        val tabIndex = tabs.indexOfFirst { it.id == tabId }
        if (tabIndex != -1) {
            tabs.removeAt(tabIndex)
            tabContainer?.removeViewAt(tabIndex)
            
            // Select the next available tab or the previous one
            if (tabs.isNotEmpty()) {
                val newTabIndex = if (tabIndex < tabs.size) tabIndex else tabIndex - 1
                if (newTabIndex >= 0 && newTabIndex < tabs.size) {
                    tabs[newTabIndex].isActive = true
                    onTabSelectedListener?.invoke(tabs[newTabIndex])
                    updateTabIndicators()
                }
            }
        }
    }
    
    fun updateTabStatus(tabId: String, isActive: Boolean) {
        val tab = tabs.find { it.id == tabId }
        tab?.isActive = isActive
        updateTabIndicators()
    }
}