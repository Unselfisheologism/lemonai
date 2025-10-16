//
// ShortcutsGenerator.swift
//  LemonAI
//

import Foundation

class ShortcutsGenerator {
    static let shared = ShortcutsGenerator()
    
    private init() {}
    
    func generateShortcutsFromWorkflow(_ workflow: [[String: Any]]) -> [String: Any]? {
        var shortcut = [String: Any]()
        
        // Define the shortcut information
        shortcut["WFWorkflowActions"] = convertWorkflowToActions(workflow)
        shortcut["WFWorkflowName"] = "LemonAI Workflow"
        shortcut["WFWorkflowIcon"] = [
            "WFWorkflowIconStartColor": 4294967295, // White color
            "WFWorkflowIconSystemImageName": "wand.and.stars",
            "WFWorkflowIconType": "Glyph"
        ]
        
        return shortcut
    }
    
    private func convertWorkflowToActions(_ workflow: [[String: Any]]) -> [[String: Any]] {
        var actions = [[String: Any]]()
        
        for step in workflow {
            if let stepType = step["type"] as? String,
               let params = step["params"] as? [String: Any] {
                
                var action: [String: Any] = [:]
                
                switch stepType {
                case "APP_SWITCH":
                    if let appName = params["appName"] as? String {
                        action = [
                            "WFWorkflowActionIdentifier": "is.workflow.actions.openapp",
                            "WFWorkflowActionParameters": [
                                "WFAppIdentifier": appName,
                                "WFAppAction": "Open"
                            ]
                        ]
                    }
                    
                case "FORM_FILL":
                    if let fields = params["fields"] as? [String: Any] {
                        // For form filling, we'll use a combination of actions
                        // This is a simplified approach - real implementation would be more complex
                        action = [
                            "WFWorkflowActionIdentifier": "is.workflow.actions.setvariable",
                            "WFWorkflowActionParameters": [
                                "WFInput": fields,
                                "WFVariableName": "FormData"
                            ]
                        ]
                    }
                    
                case "CLICK":
                    if let selector = params["selector"] as? String {
                        // For clicking elements, we might use accessibility actions
                        action = [
                            "WFWorkflowActionIdentifier": "is.workflow.actions.conditional",
                            "WFWorkflowActionParameters": [
                                "WFCondition": [
                                    "WFConditionType": "contains",
                                    "WFConditionalActionString": selector,
                                    "WFControlFlowMode": 0
                                ]
                            ]
                        ]
                    }
                    
                default:
                    print("Unknown step type: \(stepType)")
                    continue
                }
                
                if !action.isEmpty {
                    actions.append(action)
                }
            }
        }
        
        return actions
    }
    
    func saveShortcutToLibrary(shortcutName: String, shortcutData: [String: Any]) {
        // Create a shortcut file and save it to the user's library
        guard let documentsPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first else {
            print("Error: Unable to access documents directory")
            return
        }
        
        // Create a unique filename for the shortcut
        let fileName = "\(shortcutName.replacingOccurrences(of: " ", with: "_")).shortcut"
        let fileURL = documentsPath.appendingPathComponent(fileName)
        
        do {
            // Convert shortcut data to JSON
            let jsonData = try JSONSerialization.data(withJSONObject: shortcutData, options: .prettyPrinted)
            
            // Write the JSON data to the file
            try jsonData.write(to: fileURL)
            
            print("Shortcut '\(shortcutName)' saved successfully to: \(fileURL.path)")
            
            // In a real implementation, you would use the Shortcuts framework to register the shortcut
            // For now, we'll just save it as a file that the user can manually import
            print("To use this shortcut, manually import the file from: \(fileURL.path)")
        } catch {
            print("Error saving shortcut '\(shortcutName)': \(error)")
        }
    }
    
    func isShortcutsAvailable() -> Bool {
        // Check if Shortcuts API is available
        #if targetEnvironment(simulator)
        return false
        #else
        if #available(iOS 12.0, *) {
            return true
        } else {
            return false
        }
        #endif
    }
    
    func requestShortcutsPermission(completion: @escaping (Bool) -> Void) {
        // Request permission to create shortcuts
        // In a real implementation, you would use the Shortcuts framework
        DispatchQueue.main.async {
            completion(true) // Simulate success
        }
    }
}