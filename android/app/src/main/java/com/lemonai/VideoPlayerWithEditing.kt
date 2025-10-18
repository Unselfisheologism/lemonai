package com.lemonai

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import java.util.*

/**
 * Video player with editing capabilities as described in UI-description.md
 * Features:
 * - Video player activated when user clicks on video from canvas
 * - Timeline at the bottom showing to edit videos (like in a video editor app)
 * - Various features to edit the videos (like in a video editor)
 */
class VideoPlayerWithEditing : CardView {
    
    private var videoView: VideoView? = null
    private var timelineSeekBar: SeekBar? = null
    private var playPauseButton: ImageButton? = null
    private var currentTimeText: TextView? = null
    private var totalTimeText: TextView? = null
    private var editControlsContainer: LinearLayout? = null
    private var mediaPlayer: MediaPlayer? = null
    
    private var isPlaying = false
    private var videoUrl: String? = null
    
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
        // Set card properties
        radius = 8f
        cardElevation = 4f
        setCardBackgroundColor(resources.getColor(android.R.color.white))
        
        val mainContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // Video view
        videoView = VideoView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                400 // Fixed height for demo
            )
        }
        
        // Player controls
        val playerControls = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(16, 8, 16, 8)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        playPauseButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_media_play)
            layoutParams = LayoutParams(
                60,
                60
            )
            setOnClickListener {
                togglePlayPause()
            }
        }
        
        currentTimeText = TextView(context).apply {
            text = "00:00"
            textSize = 14f
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 0, 8, 0)
            }
        }
        
        timelineSeekBar = SeekBar(context).apply {
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            )
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        videoView?.seekTo(progress)
                        updateTimeDisplay()
                    }
                }
                
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        
        totalTimeText = TextView(context).apply {
            text = "00:00"
            textSize = 14f
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 16, 0)
            }
        }
        
        playerControls.addView(playPauseButton)
        playerControls.addView(currentTimeText)
        playerControls.addView(timelineSeekBar)
        playerControls.addView(totalTimeText)
        
        // Edit controls container (initially hidden)
        editControlsContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE // Hidden by default
            setPadding(16, 8, 16, 8)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // Create edit controls
        createEditControls()
        
        mainContainer.addView(videoView)
        mainContainer.addView(playerControls)
        mainContainer.addView(editControlsContainer)
        
        addView(mainContainer)
        
        // Setup video view callbacks
        videoView?.setOnPreparedListener { mp ->
            timelineSeekBar?.max = mp.duration
            totalTimeText?.text = formatTime(mp.duration.toLong())
            mediaPlayer = mp
        }
        
        videoView?.setOnCompletionListener {
            isPlaying = false
            playPauseButton?.setImageResource(android.R.drawable.ic_media_play)
        }
        
        videoView?.setOnBufferingUpdateListener { _, percent ->
            timelineSeekBar?.secondaryProgress = (timelineSeekBar?.max?.times(percent)?.div(100)) ?: 0
        }
    }
    
    private fun createEditControls() {
        // Edit controls title
        val editTitle = TextView(context).apply {
            text = "Video Editing Tools"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 8)
            }
        }
        
        editControlsContainer?.addView(editTitle)
        
        // Editing features layout
        val editFeatures = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        // Trim feature
        val trimLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
        
        val trimLabel = TextView(context).apply {
            text = "Trim:"
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        val trimStartButton = Button(context).apply {
            text = "Set Start"
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 8, 0)
            }
        }
        
        val trimEndButton = Button(context).apply {
            text = "Set End"
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 0)
            }
        }
        
        trimLayout.addView(trimLabel)
        trimLayout.addView(trimStartButton)
        trimLayout.addView(trimEndButton)
        
        // Speed control
        val speedLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
        
        val speedLabel = TextView(context).apply {
            text = "Speed:"
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        val speedSpinner = Spinner(context).apply {
            val speeds = arrayOf("0.5x", "1.0x", "1.5x", "2.0x")
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, speeds).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 0, 0)
            }
        }
        
        speedLayout.addView(speedLabel)
        speedLayout.addView(speedSpinner)
        
        // Filters
        val filterLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
        
        val filterLabel = TextView(context).apply {
            text = "Filter:"
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        val filterSpinner = Spinner(context).apply {
            val filters = arrayOf("Normal", "Grayscale", "Sepia", "Invert")
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, filters).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 0, 0)
            }
        }
        
        filterLayout.addView(filterLabel)
        filterLayout.addView(filterSpinner)
        
        // Apply button
        val applyButton = Button(context).apply {
            text = "Apply Changes"
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
        }
        
        editFeatures.addView(trimLayout)
        editFeatures.addView(speedLayout)
        editFeatures.addView(filterLayout)
        editFeatures.addView(applyButton)
        
        editControlsContainer?.addView(editFeatures)
    }
    
    private fun togglePlayPause() {
        if (videoUrl == null) return
        
        if (isPlaying) {
            videoView?.pause()
            playPauseButton?.setImageResource(android.R.drawable.ic_media_play)
            isPlaying = false
        } else {
            videoView?.start()
            playPauseButton?.setImageResource(android.R.drawable.ic_media_pause)
            isPlaying = true
            
            // Start updating the time display
            updateTimer()
        }
    }
    
    private fun updateTimer() {
        if (isPlaying && videoView?.isPlaying == true) {
            timelineSeekBar?.progress = videoView?.currentPosition ?: 0
            updateTimeDisplay()
            postDelayed(::updateTimer, 1000) // Update every second
        }
    }
    
    private fun updateTimeDisplay() {
        val currentPosition = videoView?.currentPosition ?: 0
        currentTimeText?.text = formatTime(currentPosition.toLong())
    }
    
    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60)) % 24
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    fun loadVideo(videoUrl: String) {
        this.videoUrl = videoUrl
        videoView?.setVideoPath(videoUrl)
    }
    
    fun showEditControls(show: Boolean) {
        editControlsContainer?.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    fun isEditControlsVisible(): Boolean {
        return editControlsContainer?.visibility == View.VISIBLE
    }
    
    fun toggleEditControls() {
        showEditControls(!isEditControlsVisible())
    }
    
    fun getCurrentPosition(): Int {
        return videoView?.currentPosition ?: 0
    }
    
    fun getDuration(): Int {
        return videoView?.duration ?: 0
    }
}