# WASM Pieces Directory

This directory contains pre-compiled WASM modules for MCP pieces (Gmail and Notion).

## Compiling Pieces to WASM

To compile the ActivePieces to WASM, follow these steps:

### Prerequisites
1. Install Emscripten SDK (https://emscripten.org/docs/getting_started/downloads.html)
2. Install Node.js and npm
3. Clone the ActivePieces repository

### Compilation Process

#### For Gmail Piece
```bash
# Navigate to the piece-gmail directory
cd path/to/activepieces/packages/piece-gmail

# Install dependencies
npm install

# Compile to WASM using Emscripten
emcc -O3 \
  -s WASM=1 \
  -s EXPORTED_FUNCTIONS='["_malloc", "_free"]' \
  -s EXPORTED_RUNTIME_METHODS='["ccall", "cwrap"]' \
  -s MODULARIZE=1 \
  -s EXPORT_NAME="GmailPiece" \
  --bind \
  src/*.cpp \
  -o ../public/wasm/gmail_piece.js
```

#### For Notion Piece
```bash
# Navigate to the piece-notion directory
cd path/to/activepieces/packages/piece-notion

# Install dependencies
npm install

# Compile to WASM using Emscripten
emcc -O3 \
  -s WASM=1 \
  -s EXPORTED_FUNCTIONS='["_malloc", "_free"]' \
  -s EXPORTED_RUNTIME_METHODS='["ccall", "cwrap"]' \
  -s MODULARIZE=1 \
  -s EXPORT_NAME="NotionPiece" \
  --bind \
  src/*.cpp \
  -o ../public/wasm/notion_piece.js
```

### WASM Module Structure
Each compiled piece will consist of:
- `piece_name.js` - JavaScript glue code
- `piece_name.wasm` - Binary WASM module
- `piece_name.worker.js` - Web Worker for background processing (if needed)

### Loading WASM Modules
The WASM pieces are loaded by `src/wasm-pieces.js` which:
1. Fetches the WASM binary from `/wasm/piece_name.wasm`
2. Instantiates the WASM module using `WebAssembly.instantiate()`
3. Exposes the exported functions through a JavaScript wrapper

### Exported Functions
Each piece exports functions that correspond to its actions:
- Gmail piece: `listEmails`, `getEmailDetails`, `sendEmail`, `createDraft`
- Notion piece: `searchPages`, `getPageDetails`, `createPage`, `updatePage`, `searchDatabases`, `queryDatabase`

### Authentication
Authentication is handled by passing token data to each function:
```javascript
// Example of calling a WASM function with authentication
const result = gmailPiece.listEmails({
  accessToken: 'user_access_token'
}, {
  mailbox: 'INBOX',
  limit: 10
});
```

### Error Handling
WASM functions throw JavaScript exceptions for error conditions:
```javascript
try {
  const result = await gmailPiece.listEmails(auth, props);
} catch (error) {
  console.error('Error listing emails:', error.message);
}