//
//  ViewController.swift
//  LemonAI
//
//  Created by James Abraham on 16/10/25.
//

import UIKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        // Test Shortcuts generation
        testShortcutsGeneration()
    }
    
    private func testShortcutsGeneration() {
        // Run shortcuts test
        ShortcutsTest.testGmailAndNotionShortcuts()
    }
}