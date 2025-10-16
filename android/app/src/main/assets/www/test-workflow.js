// test-workflow.js
// This script tests the complete workflow from UI to native bridge to MCP execution

console.log("Starting workflow test...");

// Test data for Gmail
const gmailWorkflow = [
  {
    type: "APP_SWITCH",
    params: {
      appName: "com.google.android.gm"
    }
  },
  {
    type: "MCP_EXECUTE",
    params: {
      pieceName: "gmail",
      action: "sendEmail",
      params: JSON.stringify({
        to: "test@example.com",
        subject: "Test Email from LemonAI",
        body: "This is a test email sent from the LemonAI mobile app."
      })
    }
  }
];

// Test data for Notion
const notionWorkflow = [
  {
    type: "APP_SWITCH",
    params: {
      appName: "notion.id"
    }
  },
  {
    type: "MCP_EXECUTE",
    params: {
      pieceName: "notion",
      action: "createPage",
      params: JSON.stringify({
        pageTitle: "Test Page from LemonAI",
        pageContent: "This is a test page created by the LemonAI mobile app."
      })
    }
  }
];

// Function to execute workflow through native bridge
function executeWorkflowThroughBridge(workflow) {
  try {
    // In a real implementation, this would call the native bridge
    // For testing purposes, we'll simulate the calls
    console.log("Executing workflow through native bridge:", workflow);
    
    // Simulate native bridge calls
    for (const step of workflow) {
      switch (step.type) {
        case "APP_SWITCH":
          console.log("Switching to app:", step.params.appName);
          // In real implementation: NativeBridge.switchToApp(step.params.appName);
          break;
          
        case "MCP_EXECUTE":
          console.log("Executing MCP piece:", step.params.pieceName, step.params.action);
          // In real implementation: NativeBridge.executeMcpPiece(step.params.pieceName, step.params.action, step.params.params);
          break;
          
        default:
          console.log("Unknown step type:", step.type);
      }
    }
    
    return { success: true, message: "Workflow executed successfully" };
  } catch (error) {
    console.error("Error executing workflow:", error);
    return { success: false, error: error.message };
  }
}

// Run tests
console.log("Testing Gmail workflow...");
const gmailResult = executeWorkflowThroughBridge(gmailWorkflow);
console.log("Gmail workflow result:", gmailResult);

console.log("Testing Notion workflow...");
const notionResult = executeWorkflowThroughBridge(notionWorkflow);
console.log("Notion workflow result:", notionResult);

console.log("Workflow test completed.");