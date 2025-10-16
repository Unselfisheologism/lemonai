//
// ShortcutsTest.swift
//  LemonAI
//
//  Created to test the Shortcuts generation functionality
//

import Foundation

class ShortcutsTest {
    static func testGmailAndNotionShortcuts() {
        print("Testing Gmail and Notion Shortcuts generation...")
        
        // Test Gmail workflow
        let gmailWorkflow: [[String: Any]] = [
            [
                "type": "APP_SWITCH",
                "params": [
                    "appName": "com.google.Gmail"
                ]
            ],
            [
                "type": "FORM_FILL",
                "params": [
                    "fields": [
                        "to": "test@example.com",
                        "subject": "Test Email",
                        "body": "This is a test email from LemonAI"
                    ]
                ]
            ],
            [
                "type": "CLICK",
                "params": [
                    "selector": "sendButton"
                ]
            ]
        ]
        
        // Test Notion workflow
        let notionWorkflow: [[String: Any]] = [
            [
                "type": "APP_SWITCH",
                "params": [
                    "appName": "Notion"
                ]
            ],
            [
                "type": "FORM_FILL",
                "params": [
                    "fields": [
                        "pageTitle": "Test Page from LemonAI",
                        "pageContent": "This is a test page created by LemonAI"
                    ]
                ]
            ],
            [
                "type": "CLICK",
                "params": [
                    "selector": "publishButton"
                ]
            ]
        ]
        
        // Generate shortcuts for both workflows
        let shortcutsGenerator = ShortcutsGenerator.shared
        
        if let gmailShortcut = shortcutsGenerator.generateShortcutsFromWorkflow(gmailWorkflow) {
            print("Gmail shortcut generated successfully")
            shortcutsGenerator.saveShortcutToLibrary(shortcutName: "Gmail Workflow", shortcutData: gmailShortcut)
        } else {
            print("Failed to generate Gmail shortcut")
        }
        
        if let notionShortcut = shortcutsGenerator.generateShortcutsFromWorkflow(notionWorkflow) {
            print("Notion shortcut generated successfully")
            shortcutsGenerator.saveShortcutToLibrary(shortcutName: "Notion Workflow", shortcutData: notionShortcut)
        } else {
            print("Failed to generate Notion shortcut")
        }
        
        print("Shortcuts test completed")
    }
}