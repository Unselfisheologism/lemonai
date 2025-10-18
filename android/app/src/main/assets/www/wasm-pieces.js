/**
 * WasmPieces.js - WASM-based MCP Pieces Manager
 * 
 * This file manages pre-compiled WASM modules for MCP pieces (Gmail and Notion)
 * that run in the browser environment for mobile workflow automation.
 */

class WasmPieces {
  constructor() {
    this.pieces = new Map();
    this.initialized = false;
  }

  /**
   * Initialize the WASM pieces manager
   */
  async initialize() {
    if (this.initialized) {
      return;
    }

    try {
      // Load Gmail piece WASM module
      const gmailPiece = await this.loadPiece('gmail');
      if (gmailPiece) {
        this.pieces.set('gmail', gmailPiece);
      }

      // Load Notion piece WASM module
      const notionPiece = await this.loadPiece('notion');
      if (notionPiece) {
        this.pieces.set('notion', notionPiece);
      }

      this.initialized = true;
      console.log('WASM Pieces initialized successfully');
    } catch (error) {
      console.error('Failed to initialize WASM Pieces:', error);
      throw error;
    }
  }

  /**
   * Load a specific piece by name
   * @param {string} pieceName - Name of the piece to load (e.g., 'gmail', 'notion')
   * @returns {Promise<Object>} The loaded piece module
   */
  async loadPiece(pieceName) {
    try {
      // Load the WASM module for the specified piece
      const wasmModule = await this.loadWasmModule(pieceName);
      
      if (!wasmModule) {
        throw new Error(`WASM module for piece '${pieceName}' not found`);
      }

      // Create a piece wrapper that provides the same interface as MCP pieces
      const piece = {
        name: pieceName,
        module: wasmModule,
        
        // Execute a specific action in the piece
        async executeAction(actionName, auth, props) {
          if (!wasmModule[actionName]) {
            throw new Error(`Action '${actionName}' not found in piece '${pieceName}'`);
          }
          
          try {
            // Call the WASM function with authentication and properties
            const result = await wasmModule[actionName](auth, props);
            return result;
          } catch (error) {
            console.error(`Error executing action '${actionName}' in piece '${pieceName}':`, error);
            throw error;
          }
        },
        
        // Get available actions in the piece
        getActions() {
          const actions = [];
          for (const key in wasmModule) {
            if (typeof wasmModule[key] === 'function') {
              actions.push(key);
            }
          }
          return actions;
        }
      };

      return piece;
    } catch (error) {
      console.error(`Failed to load piece '${pieceName}':`, error);
      return null;
    }
 }

  /**
   * Load the WASM module for a specific piece
   * @param {string} pieceName - Name of the piece
   * @returns {Promise<Object>} The WASM module
   */
  async loadWasmModule(pieceName) {
    // Construct the path to the WASM file
    const wasmPath = `/wasm/${pieceName}_piece.wasm`;
    
    try {
      // Check if the WASM file exists before attempting to load
      const response = await fetch(wasmPath);
      if (!response.ok) {
        throw new Error(`WASM file not found at ${wasmPath}`);
      }

      // Load and instantiate the WASM module
      const wasmBytes = await response.arrayBuffer();
      const wasmModule = await WebAssembly.instantiate(wasmBytes);
      
      return wasmModule.instance.exports;
    } catch (error) {
      console.error(`Failed to load WASM module for '${pieceName}':`, error);
      throw error;
    }
  }

  /**
   * Execute an action in a specific piece
   * @param {string} pieceName - Name of the piece
   * @param {string} actionName - Name of the action to execute
   * @param {Object} auth - Authentication data
   * @param {Object} props - Properties for the action
   * @returns {Promise<any>} The result of the action
   */
  async executeAction(pieceName, actionName, auth, props) {
    if (!this.initialized) {
      await this.initialize();
    }

    const piece = this.pieces.get(pieceName);
    if (!piece) {
      throw new Error(`Piece '${pieceName}' not found or not loaded`);
    }

    return piece.executeAction(actionName, auth, props);
  }

  /**
   * Get all available pieces
   * @returns {Array<string>} List of available piece names
   */
  getAvailablePieces() {
    if (!this.initialized) {
      return [];
    }
    return Array.from(this.pieces.keys());
  }

  /**
   * Get available actions for a specific piece
   * @param {string} pieceName - Name of the piece
   * @returns {Array<string>} List of available actions
   */
  getPieceActions(pieceName) {
    if (!this.initialized) {
      return [];
    }

    const piece = this.pieces.get(pieceName);
    if (!piece) {
      return [];
    }

    return piece.getActions();
  }

  /**
   * Check if a piece is available
   * @param {string} pieceName - Name of the piece
   * @returns {boolean} True if the piece is available
   */
  hasPiece(pieceName) {
    if (!this.initialized) {
      return false;
    }
    return this.pieces.has(pieceName);
  }
}

// Create a singleton instance
const wasmPieces = new WasmPieces();

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
  module.exports = wasmPieces;
} else if (typeof window !== 'undefined') {
  window.wasmPieces = wasmPieces;
}
