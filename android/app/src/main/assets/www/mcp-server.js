// MCP Server for nodejs-mobile
const http = require('http');
const fs = require('fs');
const path = require('path');

// Load local pieces
const gmailPiece = require('./mcp-pieces/gmail.js');
const notionPiece = require('./mcp-pieces/notion.js');

// Store pieces in a map for easy access
const pieces = {
  'gmail': gmailPiece,
  'notion': notionPiece
};

// Create a simple HTTP server to handle requests from the native bridge
const httpServer = http.createServer((req, res) => {
  if (req.method === 'POST' && req.url === '/execute-piece') {
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
    });
    req.on('end', () => {
      try {
        const { pieceName, action, params } = JSON.parse(body);
        
        // Execute the piece based on the request
        const result = executeMcpPiece(pieceName, action, params);
        
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, result }));
      } catch (error) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, error: error.message }));
      }
    });
  } else {
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ success: false, error: 'Not found' }));
  }
});

function executeMcpPiece(pieceName, action, params) {
  // Execute an MCP piece based on its name and action
  try {
    if (!pieces[pieceName]) {
      throw new Error(`Unknown piece: ${pieceName}`);
    }
    
    const piece = pieces[pieceName];
    
    // Check if the piece has the requested action
    if (piece.actions && piece.actions[action]) {
      // Execute the action
      return piece.actions[action].run({ auth: null, params: params || {} });
    } else if (piece[action] && typeof piece[action] === 'function') {
      // For backward compatibility with older piece structure
      return piece[action](params || {});
    } else {
      throw new Error(`Action ${action} not found in piece ${pieceName}`);
    }
  } catch (error) {
    console.error('Error executing MCP piece:', error);
    throw error;
  }
}

// Start the HTTP server
httpServer.listen(3001, () => {
  console.log('MCP HTTP server running on port 3001');
  console.log('Loaded pieces:', Object.keys(pieces));
});

// Export the execution function for direct use
module.exports = { executeMcpPiece };