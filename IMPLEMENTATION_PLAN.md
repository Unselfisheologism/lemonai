# Mobile LemonAI Implementation Plan - Aligned with Revised Requirements

## Issues Identified with Current Implementation

1. **WASM Pieces**: Created JavaScript mock implementations instead of actual WASM modules
2. **Android Code**: Still contains Node.js references that violate the "no Node.js runtime on mobile" constraint
3. **Missing Components**: Didn't create the specific JavaScript bridge files mentioned in the plan
4. **Architecture Violations**: Still references Node.js services in AndroidManifest.xml and MainActivity.kt

## Corrected Implementation Plan

### Phase 1: Remove Node.js Dependencies
- [ ] Remove all Node.js service references from AndroidManifest.xml
- [ ] Remove Node.js initialization from MainActivity.kt
- [ ] Remove NodeServiceHelper and related components
- [ ] Clean up AndroidManifest.xml to only include AccessibilityService

### Phase 2: Create Proper WASM Pieces
- [ ] Create actual compilation pipeline for @activepieces/piece-gmail to WASM
- [ ] Create actual compilation pipeline for @activepieces/piece-notion to WASM
- [ ] Replace JavaScript mock implementations with proper WASM modules
- [ ] Update WasmPieces.js to load actual WASM binaries

### Phase 3: Implement JavaScript Bridge Files
- [ ] Create AndroidAutomation.js for native communication (already partially done)
- [ ] Create WasmPieces.js for WASM piece management (already partially done)
- [ ] Create AuthFlow.js for OAuth implementation (already partially done)

### Phase 4: Update Android Components
- [ ] Verify AutomationService.kt implements Blurr repository approach correctly
- [ ] Verify AndroidAutomationInterface.kt provides proper JavaScript interface
- [ ] Ensure all Android components work without Node.js

### Phase 5: Create Simple UI
- [ ] Create basic HTML interface for connecting services
- [ ] Implement workflow execution UI
- [ ] Add status and error reporting

### Phase 6: Testing and Validation
- [ ] Test WASM piece loading and execution
- [ ] Test Android Accessibility Service integration
- [ ] Test OAuth flow for service connections
- [ ] Verify no Node.js dependencies exist

## Detailed Task Breakdown

### Task 1: Clean Android Manifest and Main Activity
The AndroidManifest.xml and MainActivity.kt still contain references to Node.js services that must be removed:

1. Remove NodeService and NodeServiceHelper declarations from AndroidManifest.xml
2. Remove NodeServiceHelper.init() call from MainActivity.kt
3. Remove import org.nodejs.v8.android.NodeJSPackageActivity from MainActivity.kt
4. Keep only the AccessibilityService registration

### Task 2: Create Actual WASM Compilation Pipeline
Instead of JavaScript mocks, we need to create actual WASM modules:

1. Set up toolchain to compile @activepieces/piece-gmail to WASM
2. Set up toolchain to compile @activepieces/piece-notion to WASM
3. Create build scripts to generate WASM binaries
4. Store compiled WASM modules in public/wasm/ directory
5. Update WasmPieces.js to load actual WASM modules instead of JavaScript mocks

### Task 3: Verify Android Accessibility Service Implementation
Ensure the AutomationService.kt properly implements the Blurr repository approach:

1. Verify accessibility service configuration matches Blurr implementation
2. Confirm all UI automation methods are properly implemented
3. Test with actual Android UI elements

### Task 4: Create Proper JavaScript Bridge
Implement the specific JavaScript bridge files mentioned in the plan:

1. AndroidAutomation.js - Already partially implemented, needs refinement
2. WasmPieces.js - Already partially implemented, needs actual WASM loading
3. AuthFlow.js - Already partially implemented, needs testing

### Task 5: Create Simple WebView UI
Develop a basic HTML interface:

1. Simple interface for connecting Gmail and Notion services
2. Workflow creation and execution UI
3. Status and error reporting components
4. Ensure responsive design for mobile devices

## Constraints to Maintain
- DO NOT modify existing Gradle files or Android build configurations
- DO NOT implement Node.js runtime on mobile
- DO NOT use Docker or complex containerization
- Focus on simplicity for non-technical users
- Only implement Gmail and Notion pieces initially

## Expected Deliverables
1. Android native layer with Accessibility Service implementation
2. JavaScript bridge for Android automation
3. WASM pieces implementation for Gmail and Notion
4. Simple OAuth flow for service connections
5. Basic UI for connecting services
6. Documentation for building and testing