# Project Context

## Purpose
Lemon AI is the first full-stack, open-source, agentic AI framework offering a fully local alternative to platforms like Manus & Genspark AI. It features an integrated Code Interpreter VM sandbox for safe execution. Lemon AI empowers deep research, web browsing, viable coding, and data analysis – running entirely on your local hardware. It supports planning, action, reflection, and memory functionalities using local LLMs (like DeepSeek, Qwen, Llama, Gemma) via Ollama, ensuring complete privacy and zero cloud dependency. The project leverages Puter.js to provide unified access to various AI models with automatic authentication and user-pays model for cost efficiency.

## Tech Stack
- Node.js, Koa.js
- TypeScript, JavaScript
- Docker (for runtime sandboxing)
- Puter.js (for unified AI model access)
- SQLite/MySQL (for data persistence)
- Electron (for desktop application)
- Vite (for build tooling)
- Various AI models (Qwen, Claude, GPT, Gemini, DeepSeek, etc.)

## Project Conventions

### Code Style
- Use JavaScript with CommonJS modules
- Use TypeScript declarations for type safety
- Follow consistent naming conventions with camelCase for functions and variables
- Use meaningful variable names and consistent formatting
- Use module aliases (e.g., @src for src directory)
- Use async/await for asynchronous operations
- Implement proper error handling and logging

### Architecture Patterns
- Model-View-Controller (MVC) pattern for application structure
- Plugin-based architecture for tools and extensions
- Service layer pattern for business logic (e.g., PuterAuthService)
- Router-based API structure using Koa Router
- Agent-based architecture for AI interactions
- Runtime abstraction (DockerRuntime, LocalRuntime) for execution environments
- Proxy pattern for AI model access through Puter.js

### Testing Strategy
- Unit tests using Mocha framework
- Integration tests for API endpoints
- End-to-end tests using Playwright
- Test files located in test/ directory
- Follow behavior-driven development (BDD) patterns

### Git Workflow
- Feature branch workflow
- Use semantic commit messages
- Branch naming: feature/..., bugfix/..., hotfix/...
- Pull requests require review before merging
- Follow conventional commits specification

## Domain Context
This project is an AI agent framework that integrates with Puter.js for AI model access. It provides a complete environment for AI agents to interact with the system, execute code safely, and maintain conversation history. The system includes a proxy layer that translates OpenAI-compatible API calls to Puter.js calls, allowing standard AI client libraries to work with Puter's unified AI interface. The architecture supports both local and Docker-based runtime environments for secure code execution.

## Important Constraints
- All AI interactions must go through Puter.js for unified access
- Runtime execution must be sandboxed for security
- User data privacy must be maintained
- Local execution preference over cloud services
- Support for multiple AI model providers through Puter.js abstraction
- VM sandboxing for code execution safety

## External Dependencies
- Puter.js SDK for AI model access
- Docker for runtime sandboxing
- Various AI model providers (OpenAI, Anthropic, Google, etc.) through Puter.js
- Database systems (SQLite/MySQL) for data persistence
- Browser automation tools for web interactions
- File system access for workspace management
