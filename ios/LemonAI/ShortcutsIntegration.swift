//
// ShortcutsIntegration.swift
//  LemonAI
//

import Foundation
import Intents

class ShortcutsIntegration {
    
    static let shared = ShortcutsIntegration()
    
    private init() {}
    
    func isShortcutsAvailable() -> Bool {
        // Check if Shortcuts API is available
        return INVoiceShortcutCenter.supportsCreatingShortcuts
    }
    
    func requestShortcutsPermission(completion: @escaping (Bool) -> Void) {
        // Request permission to create shortcuts
        INPreferences.requestSiriAuthorization { status in
            DispatchQueue.main.async {
                let authorized = status == .authorized
                completion(authorized)
            }
        }
    }
    
    func generateShortcutsWorkflow(steps: [[String: Any]]) -> [String: Any]? {
        // Convert workflow steps to Shortcuts format
        var shortcutActions: [[String: Any]] = []
        
        for step in steps {
            if let stepType = step["type"] as? String {
                switch stepType {
                case "APP_SWITCH":
                    if let appName = step["appName"] as? String {
                        let action = createAppSwitchAction(appName: appName)
                        shortcutActions.append(action)
                    }
                case "FORM_FILL":
                    if let fields = step["fields"] as? [String: String] {
                        let action = createFormFillAction(fields: fields)
                        shortcutActions.append(action)
                    }
                case "CLICK":
                    if let selector = step["selector"] as? String {
                        let action = createClickAction(selector: selector)
                        shortcutActions.append(action)
                    }
                default:
                    print("Unknown step type: \(stepType)")
                }
            }
        }
        
        return [
            "WFWorkflowActions": shortcutActions
        ]
    }
    
    private func createAppSwitchAction(appName: String) -> [String: Any] {
        return [
            "WFWorkflowActionIdentifier": "is.workflow.actions.openapp",
            "WFWorkflowActionParameters": [
                "WFApp": appName
            ]
        ]
    }
    
    private func createFormFillAction(fields: [String: String]) -> [String: Any] {
        // This is a simplified representation - actual implementation would be more complex
        return [
            "WFWorkflowActionIdentifier": "is.workflow.actions.setvalue",
            "WFWorkflowActionParameters": [
                "WFInput": fields
            ]
        ]
    }
    
    private func createClickAction(selector: String) -> [String: Any] {
        // This is a simplified representation - actual implementation would be more complex
        return [
            "WFWorkflowActionIdentifier": "is.workflow.actions.conditional",
            "WFWorkflowActionParameters": [
                "WFCondition": selector
            ]
        ]
    }
    
    func executeWorkflowInShortcuts(shortcutData: [String: Any]) -> Bool {
        // This would handle execution of the workflow in the Shortcuts app
        print("Executing workflow in Shortcuts: \(shortcutData)")
        return true
    }
    
    // Save the shortcut to the user's Shortcuts
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
}