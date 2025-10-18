package com.lemonai

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import java.util.*

/**
 * Canvas feature for multimodal content as described in UI-description.md
 * This includes: images, video player, line chart, infographic, spreadsheet, audio player
 */
class CanvasFeature : ScrollView {
    
    private var contentContainer: LinearLayout? = null
    private val contentItems = mutableListOf<ContentItem>()
    
    data class ContentItem(
        val id: String = UUID.randomUUID().toString(),
        val type: ContentType,
        val title: String,
        val content: Any? = null
    )
    
    enum class ContentType {
        IMAGE, VIDEO, CHART, INFOGRAPHIC, SPREADSHEET, AUDIO, TEXT
    }
    
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
        contentContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        addView(contentContainer)
    }
    
    fun addImage(title: String, imageUrl: String) {
        val cardView = createContentCard(title, ContentType.IMAGE)
        val imageView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            // In a real implementation, you would load the image from imageUrl
            // For now, we'll use a placeholder
            setImageResource(android.R.drawable.ic_menu_gallery)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                300 // Fixed height for demo
            )
        }
        
        cardView.addView(imageView)
        contentContainer?.addView(cardView)
        
        contentItems.add(ContentItem(type = ContentType.IMAGE, title = title, content = imageUrl))
    }
    
    fun addVideo(title: String, videoUrl: String) {
        val cardView = createContentCard(title, ContentType.VIDEO)
        val videoContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                300 // Fixed height for demo
            )
        }
        
        // Video player placeholder
        val videoPlaceholder = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(android.R.drawable.ic_media_play)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }
        
        // Play button overlay
        val playButton = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_media_play)
            layoutParams = FrameLayout.LayoutParams(
                100,
                100,
                android.view.Gravity.CENTER
            )
        }
        
        videoContainer.addView(videoPlaceholder)
        videoContainer.addView(playButton)
        
        cardView.addView(videoContainer)
        contentContainer?.addView(cardView)
        
        contentItems.add(ContentItem(type = ContentType.VIDEO, title = title, content = videoUrl))
    }
    
    fun addChart(title: String, chartData: Any) {
        val cardView = createContentCard(title, ContentType.CHART)
        val chartContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                300 // Fixed height for demo
            )
        }
        
        // Simple visualization of chart data
        val chartTitle = TextView(context).apply {
            text = "Line Chart: $title"
            textSize = 18f
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 8)
            }
        }
        
        // Simple bar visualization
        val chartView = View(context).apply {
            // This would be a proper chart view in a real implementation
            setBackgroundResource(android.R.color.holo_blue_light)
            layoutParams = LayoutParams(
                300,
                200
            ).apply {
                setMargins(16, 8, 16, 16)
            }
        }
        
        chartContainer.addView(chartTitle)
        chartContainer.addView(chartView)
        
        cardView.addView(chartContainer)
        contentContainer?.addView(cardView)
        
        contentItems.add(ContentItem(type = ContentType.CHART, title = title, content = chartData))
    }
    
    fun addInfographic(title: String, data: Any) {
        val cardView = createContentCard(title, ContentType.INFOGRAPHIC)
        val infographicContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                300 // Fixed height for demo
            )
        }
        
        val infoTitle = TextView(context).apply {
            text = "Infographic: $title"
            textSize = 18f
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 8)
            }
        }
        
        // Simple infographic representation
        val infoView = View(context).apply {
            setBackgroundResource(android.R.color.holo_green_light)
            layoutParams = LayoutParams(
                300,
                200
            ).apply {
                setMargins(16, 8, 16, 16)
            }
        }
        
        infographicContainer.addView(infoTitle)
        infographicContainer.addView(infoView)
        
        cardView.addView(infographicContainer)
        contentContainer?.addView(cardView)
        
        contentItems.add(ContentItem(type = ContentType.INFOGRAPHIC, title = title, content = data))
    }
    
    fun addSpreadsheet(title: String, data: Any) {
        val cardView = createContentCard(title, ContentType.SPREADSHEET)
        val spreadsheetContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                400 // Fixed height for demo
            )
        }
        
        val sheetTitle = TextView(context).apply {
            text = "Spreadsheet: $title"
            textSize = 18f
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 8)
            }
        }
        
        // Simple table representation
        val tableLayout = TableLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 16)
            }
            
            // Add header row
            val headerRow = TableRow(context)
            for (i in 0..4) {
                val headerCell = TextView(context).apply {
                    text = "Col $i"
                    setPadding(16, 8, 16, 8)
                    setBackgroundResource(R.drawable.input_background)
                    textSize = 14f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                headerRow.addView(headerCell)
            }
            addView(headerRow)
            
            // Add data rows
            for (row in 0..3) {
                val dataRow = TableRow(context)
                for (col in 0..4) {
                    val dataCell = TextView(context).apply {
                        text = "R${row}C${col}"
                        setPadding(16, 8, 16, 8)
                        setBackgroundResource(R.drawable.input_background)
                        textSize = 12f
                    }
                    dataRow.addView(dataCell)
                }
                addView(dataRow)
            }
        }
        
        spreadsheetContainer.addView(sheetTitle)
        spreadsheetContainer.addView(tableLayout)
        
        cardView.addView(spreadsheetContainer)
        contentContainer?.addView(cardView)
        
        contentItems.add(ContentItem(type = ContentType.SPREADSHEET, title = title, content = data))
    }
    
    fun addAudio(title: String, audioUrl: String) {
        val cardView = createContentCard(title, ContentType.AUDIO)
        val audioContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                150 // Fixed height for demo
            )
        }
        
        val audioTitle = TextView(context).apply {
            text = "Audio: $title"
            textSize = 18f
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 8)
            }
        }
        
        val audioControls = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 16)
            }
        }
        
        val playButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_media_play)
            layoutParams = LayoutParams(
                80,
                80
            )
        }
        
        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LayoutParams(
                0,
                50,
                1f
            ).apply {
                setMargins(16, 0, 16, 0)
            }
        }
        
        audioControls.addView(playButton)
        audioControls.addView(progressBar)
        
        audioContainer.addView(audioTitle)
        audioContainer.addView(audioControls)
        
        cardView.addView(audioContainer)
        contentContainer?.addView(cardView)
        
        contentItems.add(ContentItem(type = ContentType.AUDIO, title = title, content = audioUrl))
    }
    
    private fun createContentCard(title: String, type: ContentType): CardView {
        return CardView(context).apply {
            val cardParams = LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
            layoutParams = cardParams
            
            radius = 8f
            cardElevation = 4f
            setCardBackgroundColor(resources.getColor(android.R.color.white))
        }
    }
    
    fun clearAllContent() {
        contentContainer?.removeAllViews()
        contentItems.clear()
    }
    
    fun getContentItems(): List<ContentItem> = contentItems.toList()
}