# Mobile LemonAI - Updated Implementation Plan

## Overview
This plan reflects the simplified architecture that removes shell, headless browser automation, and coding capabilities to reduce complexity. The app will be completely docker-free and use n8n for workflow automation with session cookie-based external tool integrations.

## Architecture Changes from Original Plan

### Removed Components
- Docker containers (both app and sandbox)
- Shell command execution capabilities
- Headless browser automation (for web scraping/browser automation)
- General coding capabilities
- WASM sandbox for JavaScript/Python execution
- WASM-based external tool pieces

### Kept Components
- WebView-based mobile UI
- Puter.js integration for AI features
- Perplexity sonar models for web search
- n8n integration for workflows
- Android accessibility service (Blurr-based)
- Session cookie extraction for tool integrations

## Implementation Plan

### Phase 1: Architecture Cleanup
- [ ] Remove all Docker-related files and configurations
- [ ] Remove WASM sandbox functionality from `WASMSandbox.kt`
- [ ] Remove code execution capabilities (JavaScript/Python execution)
- [ ] Clean up any remaining Docker references in build files
- [ ] Remove browser automation tools and related code

### Phase 2: N8n Integration Enhancement
- [ ] Enhance `N8nIntegration.kt` to handle workflow management
- [ ] Implement hidden UI mode for n8n (AI agent only control)
- [ ] Create mobile-optimized workflow interface
- [ ] Implement workflow creation, reading, updating, deleting, scheduling, and execution

### Phase 3: Session Cookie Integration
- [ ] Enhance `CookieManagerHelper.kt` for external tool authentication
- [ ] Implement OAuth credential extraction from WebView session cookies
- [ ] Create secure credential passing to n8n integration nodes
- [ ] Implement Gmail and Notion node connections via n8n
- [ ] Test authentication flows without requiring active tabs

### Phase 4: Puter.js and AI Features
- [ ] Complete `PuterJSIntegration.kt` for all AI features
- [ ] Implement perplexity sonar models for web search
- [ ] Set up intercommunication between AI model and sonar models
- [ ] Implement text-to-image generation via Puter.js

### Phase 5: UI and User Experience
- [ ] Complete `SwipeUpPopup.kt` for chat interface
- [ ] Implement `TabSystem.kt` with browser-like experience
- [ ] Add slash commands (/search, /ask, /automate, /expert) in `SlashCommandHandler.kt`
- [ ] Implement expert agent interface
- [ ] Complete canvas feature for multimodal content in `CanvasFeature.kt`
- [ ] Add video player with editing capabilities in `VideoPlayerWithEditing.kt`

### Phase 6: Phone Automation (Blurr-based)
- [ ] Enhance `AutomationService.kt` based on Blurr repository
- [ ] Implement Android accessibility service for UI automation
- [ ] Create home-button activation mechanism
- [ ] Enable AI agent to touch UI elements and view phone screen

### Phase 7: Optional Manual Control
- [ ] Implement settings option for technical users to manually control workflows
- [ ] Ensure pre-connected tools work for both AI agent and manual modes

## Technical Implementation Details

### Android Components
- **MainActivity.kt**: Initialize WebView with proper settings
- **WebView Configuration**: Enable JavaScript, set up communication bridge
- **CookieManagerHelper.kt**: Extract and manage session cookies
- **AndroidWorkflowEngine.kt**: Handle workflow execution
- **N8nIntegration.kt**: Interface with n8n workflow engine
- **PuterJSIntegration.kt**: Handle all AI-related functionality
- **AutomationService.kt**: Android accessibility service for phone UI automation

### Security Considerations
- Secure storage of session cookies using Android Keystore
- Sanitize and validate all inputs to prevent injection attacks
- Implement proper permission handling for accessibility services
- Ensure secure communication between WebView and native components

### Performance Optimization
- Minimize app size by removing unused Docker/WASM components
- Optimize WebView performance for mobile devices
- Implement efficient session cookie management
- Optimize n8n workflow execution for mobile performance

## Testing Strategy

### Unit Tests
- Test n8n workflow creation and execution
- Test session cookie extraction and credential passing
- Test Puter.js integration functionality
- Test accessibility service automation

### Integration Tests
- End-to-end workflow automation tests
- External tool integration tests (Gmail, Notion)
- UI automation tests using Blurr approach
- Web search functionality tests

### User Experience Tests
- Mobile UI responsiveness and usability
- Session management and authentication flows
- Performance on various Android devices
- Accessibility service behavior in different scenarios

## Deployment Considerations

### App Store Compliance
- Ensure accessibility service usage is clearly documented
- Comply with privacy policies regarding session cookie access
- Follow Android app store guidelines for automation features

### Performance Requirements
- Optimize for devices with varying performance capabilities
- Minimize battery usage for accessibility service
- Efficient memory management for workflow execution

## Success Metrics

### Technical Metrics
- App size reduction (removing Docker/WASM components)
- Session cookie extraction success rate
- Workflow execution success rate
- AI response time improvements

### User Experience Metrics
- Mobile UI responsiveness
- Authentication flow completion rate
- Successful external tool connections
- User satisfaction with automation features

## Risk Assessment

### High Risk
- Accessibility service limitations on newer Android versions
- Session cookie access restrictions by websites
- n8n performance on mobile devices

### Medium Risk
- Puter.js integration stability
- Workflow complexity handling
- Multi-user session management

### Low Risk
- WebView-based UI implementation
- General Android component integration
- Basic AI feature implementation

## Timeline Estimate
- Phase 1-2: 2-3 weeks
- Phase 3-4: 3-4 weeks
- Phase 5-6: 3-4 weeks
- Phase 7: 1 week
- Testing and refinement: 2-3 weeks

Total estimated time: 11-14 weeks