package com.lemonai

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * Mobile-optimized workflow builder interface as described in desire.md
 * Features:
 * - Simplified workflow interface optimized for mobile
 * - Vertical node arrangement for mobile UX
 * - Ability for AI agent to create, read, update, delete, schedule, and execute workflows
 */
class MobileWorkflowBuilder : LinearLayout {
    
    private var workflowRecyclerView: RecyclerView? = null
    private var workflowAdapter: WorkflowAdapter? = null
    private var workflowNodes = mutableListOf<WorkflowNode>()
    private var addButton: FloatingActionButton? = null
    private var executeButton: Button? = null
    private var scheduleButton: Button? = null
    
    // Callback interface for handling workflow events
    interface WorkflowListener {
        fun onNodeAdded(node: WorkflowNode)
        fun onNodeUpdated(node: WorkflowNode)
        fun onNodeDeleted(nodeId: String)
        fun onWorkflowExecuted()
        fun onWorkflowScheduled(schedule: ScheduleInfo)
        fun onError(error: String)
    }
    
    private var workflowListener: WorkflowListener? = null
    
    fun setWorkflowListener(listener: WorkflowListener) {
        workflowListener = listener
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
        orientation = VERTICAL
        setPadding(16, 16)
        
        // Create title
        val title = TextView(context).apply {
            text = "Workflow Builder"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
        }
        
        // Create RecyclerView for workflow nodes
        workflowRecyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        
        // Initialize adapter
        workflowAdapter = WorkflowAdapter(workflowNodes) { node, action ->
            when (action) {
                NodeAction.EDIT -> editNode(node)
                NodeAction.DELETE -> deleteNode(node.id)
                NodeAction.MOVE_UP -> moveNodeUp(node.id)
                NodeAction.MOVE_DOWN -> moveNodeDown(node.id)
            }
        }
        
        workflowRecyclerView?.adapter = workflowAdapter
        
        // Create action buttons
        val buttonContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
        }
        
        // Add node button
        addButton = FloatingActionButton(context).apply {
            setImageResource(android.R.drawable.ic_input_add)
            layoutParams = LayoutParams(
                80,
                80
            ).apply {
                setMargins(0, 0, 16, 0)
            }
            setOnClickListener {
                addNewNode()
            }
        }
        
        // Execute workflow button
        executeButton = Button(context).apply {
            text = "Execute"
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, 8, 0)
            }
            setOnClickListener {
                executeWorkflow()
            }
        }
        
        // Schedule workflow button
        scheduleButton = Button(context).apply {
            text = "Schedule"
            layoutParams = LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(8, 0, 0, 0)
            }
            setOnClickListener {
                scheduleWorkflow()
            }
        }
        
        buttonContainer.addView(addButton)
        buttonContainer.addView(executeButton)
        buttonContainer.addView(scheduleButton)
        
        // Add views to main layout
        addView(title)
        addView(workflowRecyclerView)
        addView(buttonContainer)
    }
    
    /**
     * Add a new node to the workflow
     */
    private fun addNewNode() {
        val newNode = WorkflowNode(
            id = UUID.randomUUID().toString(),
            type = NodeType.TASK,
            title = "New Task",
            description = "Click to configure this task",
            position = workflowNodes.size
        )
        
        workflowNodes.add(newNode)
        workflowAdapter?.notifyItemInserted(workflowNodes.size - 1)
        workflowRecyclerView?.scrollToPosition(workflowNodes.size - 1)
        
        workflowListener?.onNodeAdded(newNode)
    }
    
    /**
     * Edit an existing node
     */
    private fun editNode(node: WorkflowNode) {
        // Show edit dialog
        showEditNodeDialog(node)
    }
    
    /**
     * Delete a node from the workflow
     */
    private fun deleteNode(nodeId: String) {
        val index = workflowNodes.indexOfFirst { it.id == nodeId }
        if (index != -1) {
            workflowNodes.removeAt(index)
            workflowAdapter?.notifyItemRemoved(index)
            workflowListener?.onNodeDeleted(nodeId)
            
            // Update positions of remaining nodes
            updateNodePositions()
        }
    }
    
    /**
     * Move a node up in the workflow
     */
    private fun moveNodeUp(nodeId: String) {
        val index = workflowNodes.indexOfFirst { it.id == nodeId }
        if (index > 0) {
            Collections.swap(workflowNodes, index, index - 1)
            workflowAdapter?.notifyItemMoved(index, index - 1)
            updateNodePositions()
        }
    }
    
    /**
     * Move a node down in the workflow
     */
    private fun moveNodeDown(nodeId: String) {
        val index = workflowNodes.indexOfFirst { it.id == nodeId }
        if (index < workflowNodes.size - 1) {
            Collections.swap(workflowNodes, index, index + 1)
            workflowAdapter?.notifyItemMoved(index, index + 1)
            updateNodePositions()
        }
    }
    
    /**
     * Update positions of all nodes
     */
    private fun updateNodePositions() {
        for (i in workflowNodes.indices) {
            workflowNodes[i] = workflowNodes[i].copy(position = i)
        }
        workflowAdapter?.notifyDataSetChanged()
    }
    
    /**
     * Show dialog to edit a node
     */
    private fun showEditNodeDialog(node: WorkflowNode) {
        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32)
        }
        
        val titleEditText = EditText(context).apply {
            setText(node.title)
            hint = "Task Title"
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        
        val descriptionEditText = EditText(context).apply {
            setText(node.description)
            hint = "Task Description"
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        
        val typeSpinner = Spinner(context).apply {
            val types = arrayOf("Task", "Condition", "Loop", "API Call", "Notification")
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, types).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(getTypeIndex(node.type))
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        
        val saveButton = Button(context).apply {
            text = "Save"
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        dialogView.addView(titleEditText)
        dialogView.addView(descriptionEditText)
        dialogView.addView(typeSpinner)
        dialogView.addView(saveButton)
        
        val dialog = AlertDialog.Builder(context)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .show()
        
        saveButton.setOnClickListener {
            val updatedNode = node.copy(
                title = titleEditText.text.toString(),
                description = descriptionEditText.text.toString(),
                type = getTypeFromIndex(typeSpinner.selectedItemPosition)
            )
            
            val index = workflowNodes.indexOfFirst { it.id == updatedNode.id }
            if (index != -1) {
                workflowNodes[index] = updatedNode
                workflowAdapter?.notifyItemChanged(index)
                workflowListener?.onNodeUpdated(updatedNode)
            }
            
            dialog.dismiss()
        }
    }
    
    private fun getTypeIndex(type: NodeType): Int {
        return when (type) {
            NodeType.TASK -> 0
            NodeType.CONDITION -> 1
            NodeType.LOOP -> 2
            NodeType.API_CALL -> 3
            NodeType.NOTIFICATION -> 4
        }
    }
    
    private fun getTypeFromIndex(index: Int): NodeType {
        return when (index) {
            1 -> NodeType.CONDITION
            2 -> NodeType.LOOP
            3 -> NodeType.API_CALL
            4 -> NodeType.NOTIFICATION
            else -> NodeType.TASK
        }
    }
    
    /**
     * Execute the current workflow
     */
    private fun executeWorkflow() {
        if (workflowNodes.isEmpty()) {
            Toast.makeText(context, "Cannot execute empty workflow", Toast.LENGTH_SHORT).show()
            return
        }
        
        workflowListener?.onWorkflowExecuted()
        Toast.makeText(context, "Executing workflow...", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Schedule the current workflow
     */
    private fun scheduleWorkflow() {
        if (workflowNodes.isEmpty()) {
            Toast.makeText(context, "Cannot schedule empty workflow", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show schedule dialog
        showScheduleDialog()
    }
    
    /**
     * Show dialog to schedule workflow
     */
    private fun showScheduleDialog() {
        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32)
        }
        
        val datePicker = DatePicker(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        
        val timePicker = TimePicker(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        
        val scheduleButton = Button(context).apply {
            text = "Schedule"
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
        }
        
        dialogView.addView(datePicker)
        dialogView.addView(timePicker)
        dialogView.addView(scheduleButton)
        
        val dialog = AlertDialog.Builder(context)
            .setTitle("Schedule Workflow")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .show()
        
        scheduleButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.hour
                } else {
                    @Suppress("DEPRECATION")
                    timePicker.currentHour
                },
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    timePicker.minute
                } else {
                    @Suppress("DEPRECATION")
                    timePicker.currentMinute
                }
            )
            
            val scheduleInfo = ScheduleInfo(
                workflowId = UUID.randomUUID().toString(),
                scheduledTime = calendar.timeInMillis,
                repeat = false
            )
            
            workflowListener?.onWorkflowScheduled(scheduleInfo)
            Toast.makeText(context, "Workflow scheduled!", Toast.LENGTH_SHORT).show()
            
            dialog.dismiss()
        }
    }
    
    /**
     * Load a workflow
     */
    fun loadWorkflow(nodes: List<WorkflowNode>) {
        workflowNodes.clear()
        workflowNodes.addAll(nodes)
        workflowAdapter?.notifyDataSetChanged()
    }
    
    /**
     * Get current workflow
     */
    fun getCurrentWorkflow(): List<WorkflowNode> = workflowNodes.toList()
    
    /**
     * Clear current workflow
     */
    fun clearWorkflow() {
        workflowNodes.clear()
        workflowAdapter?.notifyDataSetChanged()
    }
    
    /**
     * RecyclerView adapter for workflow nodes
     */
    inner class WorkflowAdapter(
        private val nodes: List<WorkflowNode>,
        private val onNodeAction: (WorkflowNode, NodeAction) -> Unit
    ) : RecyclerView.Adapter<WorkflowAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nodeCard: CardView = view.findViewById(R.id.node_card)
            val titleText: TextView = view.findViewById(R.id.node_title)
            val descriptionText: TextView = view.findViewById(R.id.node_description)
            val editButton: ImageButton = view.findViewById(R.id.edit_button)
            val deleteButton: ImageButton = view.findViewById(R.id.delete_button)
            val moveUpButton: ImageButton = view.findViewById(R.id.move_up_button)
            val moveDownButton: ImageButton = view.findViewById(R.id.move_down_button)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.workflow_node_item, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val node = nodes[position]
            
            holder.titleText.text = node.title
            holder.descriptionText.text = node.description
            
            holder.editButton.setOnClickListener {
                onNodeAction(node, NodeAction.EDIT)
            }
            
            holder.deleteButton.setOnClickListener {
                onNodeAction(node, NodeAction.DELETE)
            }
            
            holder.moveUpButton.setOnClickListener {
                onNodeAction(node, NodeAction.MOVE_UP)
            }
            
            holder.moveDownButton.setOnClickListener {
                onNodeAction(node, NodeAction.MOVE_DOWN)
            }
            
            // Disable move up/down buttons at edges
            holder.moveUpButton.isEnabled = position > 0
            holder.moveDownButton.isEnabled = position < nodes.size - 1
        }
        
        override fun getItemCount(): Int = nodes.size
    }
    
    /**
     * Data class for workflow node
     */
    data class WorkflowNode(
        val id: String,
        val type: NodeType,
        val title: String,
        val description: String,
        val position: Int,
        val config: Map<String, Any> = emptyMap()
    )
    
    /**
     * Enum for node types
     */
    enum class NodeType {
        TASK, CONDITION, LOOP, API_CALL, NOTIFICATION
    }
    
    /**
     * Enum for node actions
     */
    enum class NodeAction {
        EDIT, DELETE, MOVE_UP, MOVE_DOWN
    }
    
    /**
     * Data class for schedule information
     */
    data class ScheduleInfo(
        val workflowId: String,
        val scheduledTime: Long,
        val repeat: Boolean,
        val repeatInterval: Long = 0 // in milliseconds
    )
}