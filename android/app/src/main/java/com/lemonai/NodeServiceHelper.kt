package com.lemonai

import android.content.Context
import im.delight.android.node.NodeJs

class NodeServiceHelper {
    companion object {
        private var isInitialized = false
        
        fun init(context: Context) {
            if (!isInitialized) {
                try {
                    NodeJs.init(context)
                    isInitialized = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        fun startMcpServer(): Boolean {
            return try {
                // Start MCP server using nodejs-mobile
                NodeJs.instance()?.runScript("mcp-server.js")
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
        
        fun executeMcpPiece(pieceName: String, action: String, params: String): String {
            return try {
                // Execute an MCP piece using nodejs-mobile
                val script = """
                    const result = executeMcpPiece('${pieceName}', '${action}', $params);
                    JSON.stringify(result);
                """
                NodeJs.instance()?.runScript(script) ?: "Error executing piece"
            } catch (e: Exception) {
                e.printStackTrace()
                "Error: ${e.message}"
            }
        }
        
        // Alternative method to execute MCP pieces
        fun executeMcpPieceSync(pieceName: String, action: String, params: String): String {
            return try {
                // Direct execution of the piece function
                val script = """
                    const result = executeMcpPiece('${pieceName}', '${action}', $params);
                    JSON.stringify(result);
                """
                NodeJs.instance()?.runScript(script) ?: "Error executing piece"
            } catch (e: Exception) {
                e.printStackTrace()
                "Error: ${e.message}"
            }
        }
    }
}