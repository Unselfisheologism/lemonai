package com.lemonai

import android.content.Context
import android.util.Log
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.security.SecureRandom

/**
 * n8n Credential Manager for external tool integration as described in desire.md
 * This component handles passing credentials to n8n nodes for external tool integration
 * using session cookies as mentioned in answers.md
 */
class N8nCredentialManager(private val context: Context) {
    private val TAG = "N8nCredentialManager"
    
    // Encryption key for securing credentials
    private var encryptionKey: SecretKey? = null
    
    // Callback interface for handling credential events
    interface CredentialListener {
        fun onCredentialsStored(toolName: String)
        fun onCredentialsRetrieved(toolName: String, credentials: Map<String, String>)
        fun onCredentialsDeleted(toolName: String)
        fun onError(error: String)
    }
    
    private var credentialListener: CredentialListener? = null
    
    fun setCredentialListener(listener: CredentialListener) {
        credentialListener = listener
    }
    
    /**
     * Initialize the credential manager
     */
    fun initialize() {
        try {
            // Generate or load encryption key
            generateEncryptionKey()
            Log.d(TAG, "Credential manager initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing credential manager", e)
            credentialListener?.onError("Error initializing credential manager: ${e.message}")
        }
    }
    
    /**
     * Generate encryption key for securing credentials
     */
    private fun generateEncryptionKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(256) // 256-bit key
            encryptionKey = keyGenerator.generateKey()
            Log.d(TAG, "Encryption key generated")
        } catch (e: Exception)Now I'll create a component for passing credentials to n8n nodes for external tool integration:

<write_to_file>
<path>android/app/src/main/java/com/lemonai/N8nCredentialManager.kt</path>
<content>
package com.lemonai

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.security.SecureRandom

/**
 * Credential manager for passing credentials to n8n nodes for external tool integration
 * as described in answers.md and desire.md
 */
class N8nCredentialManager(private val context: Context) {
    private val TAG = "N8nCredentialManager"
    
    // Encryption key for securing credentials
    private var encryptionKey: SecretKey? = null
    
    // Callback interface for credential management events
    interface CredentialManagementListener {
        fun onCredentialsStored(toolName: String)
        fun onCredentialsRetrieved(toolName: String, credentials: Map<String, String>)
        fun onCredentialsDeleted(toolName: String)
        fun onError(error: String)
    }
    
    private var credentialListener: CredentialManagementListener? = null
    
    fun setCredentialManagementListener(listener: CredentialManagementListener) {
        credentialListener = listener
    }
    
    init {
        initializeEncryption()
    }
    
    /**
     * Initialize encryption for securing credentials
     */
    private fun initializeEncryption() {
        try {
            // Generate or retrieve encryption key
            encryptionKey = generateOrRetrieveKey()
            Log.d(TAG, "Encryption initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing encryption", e)
            credentialListener?.onError("Error initializing encryption: ${e.message}")
        }
    }
    
    /**
     * Generate or retrieve encryption key
     */
    private fun generateOrRetrieveKey(): SecretKey {
        // In a real implementation, you would securely store and retrieve the key
        // For this example, we'll generate a new key each time
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256) // 256-bit key
        return keyGenerator.generateKey()
    }
    
    /**
     * Store credentials for an external tool
     */
    fun storeCredentials(toolName: String, credentials: Map<String, String>): Boolean {
        return try {
            // Encrypt credentials
            val encryptedCredentials = encryptCredentials(credentials)
            
            // Store encrypted credentials (in a real implementation, you would use SharedPreferences or a secure storage)
            val prefs = context.getSharedPreferences("n8n_credentials", Context.MODE_PRIVATE)
            prefs.edit()
                .putString(toolName, encryptedCredentials)
                .apply()
            
            Log.d(TAG, "Credentials stored for tool: $toolName")
            credentialListener?.onCredentialsStored(toolName)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error storing credentials for tool: $toolName", e)
            credentialListener?.onError("Error storing credentials: ${e.message}")
            false
        }
    }
    
    /**
     * Retrieve credentials for an external tool
     */
    fun retrieveCredentials(toolName: String): Map<String, String>? {
        return try {
            // Retrieve encrypted credentials
            val prefs = context.getSharedPreferences("n8n_credentials", Context.MODE_PRIVATE)
            val encryptedCredentials = prefs.getString(toolName, null)
            
            if (encryptedCredentials.isNullOrEmpty()) {
                Log.d(TAG, "No credentials found for tool: $toolName")
                return null
            }
            
            // Decrypt credentials
            val decryptedCredentials = decryptCredentials(encryptedCredentials)
            
            Log.d(TAG, "Credentials retrieved for tool: $toolName")
            credentialListener?.onCredentialsRetrieved(toolName, decryptedCredentials)
            decryptedCredentials
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving credentials for tool: $toolName", e)
            credentialListener?.onError("Error retrieving credentials: ${e.message}")
            null
        }
    }
    
    /**
     * Delete credentials for an external tool
     */
    fun deleteCredentials(toolName: String): Boolean {
        return try {
            val prefs = context.getSharedPreferences("n8n_credentials", Context.MODE_PRIVATE)
            prefs.edit()
                .remove(toolName)
                .apply()
            
            Log.d(TAG, "Credentials deleted for tool: $toolName")
            credentialListener?.onCredentialsDeleted(toolName)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting credentials for tool: $toolName", e)
            credentialListener?.onError("Error deleting credentials: ${e.message}")
            false
        }
    }
    
    /**
     * List all tools with stored credentials
     */
    fun listToolsWithCredentials(): List<String> {
        return try {
            val prefs = context.getSharedPreferences("n8n_credentials", Context.MODE_PRIVATE)
            val tools = prefs.all.keys.toList()
            Log.d(TAG, "Found ${tools.size} tools with stored credentials")
            tools
        } catch (e: Exception) {
            Log.e(TAG, "Error listing tools with credentials", e)
            credentialListener?.onError("Error listing tools: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Encrypt credentials using AES
     */
    private fun encryptCredentials(credentials: Map<String, String>): String {
        val key = encryptionKey ?: throw IllegalStateException("Encryption key not initialized")
        
        // Convert credentials to JSON string
        val jsonCredentials = JSONObject(credentials).toString()
        
        // Encrypt the JSON string
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(jsonCredentials.toByteArray(Charsets.UTF_8))
        
        // Encode to Base64 for storage
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }
    
    /**
     * Decrypt credentials using AES
     */
    private fun decryptCredentials(encryptedCredentials: String): Map<String, String> {
        val key = encryptionKey ?: throw IllegalStateException("Encryption key not initialized")
        
        // Decode from Base64
        val encryptedBytes = Base64.decode(encryptedCredentials, Base64.DEFAULT)
        
        // Decrypt the bytes
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        // Convert JSON string back to map
        val jsonString = String(decryptedBytes, Charsets.UTF_8)
        val jsonObject = JSONObject(jsonString)
        
        val credentials = mutableMapOf<String, String>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            credentials[key] = jsonObject.getString(key)
        }
        
        return credentials
    }
    
    /**
     * Format credentials for n8n node integration
     */
    fun formatCredentialsForN8n(toolName: String, credentials: Map<String, String>): String {
        return try {
            val n8nCredentials = JSONObject().apply {
                put("toolName", toolName)
                put("timestamp", System.currentTimeMillis())
                
                val credentialsObject = JSONObject()
                credentials.forEach { (key, value) ->
                    credentialsObject.put(key, value)
                }
                put("credentials", credentialsObject)
            }
            
            n8nCredentials.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting credentials for n8n", e)
            "{}"
        }
    }
    
    /**
     * Validate credentials format
     */
    fun validateCredentials(credentials: Map<String, String>): Boolean {
        // Basic validation - check that required fields are present
        // In a real implementation, you would have tool-specific validation
        return credentials.isNotEmpty()
    }
    
    /**
     * Migrate credentials from old format to new format
     */
    fun migrateCredentials(oldFormat: String): Map<String, String> {
        return try {
            val jsonObject = JSONObject(oldFormat)
            val credentials = mutableMapOf<String, String>()
            
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                credentials[key] = jsonObject.getString(key)
            }
            
            credentials
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating credentials", e)
            emptyMap()
        }
    }
    
    /**
     * Export credentials for backup (encrypted)
     */
    fun exportCredentials(): String {
        return try {
            val prefs = context.getSharedPreferences("n8n_credentials", Context.MODE_PRIVATE)
            val allCredentials = prefs.all
            
            val exportObject = JSONObject()
            allCredentials.forEach { (toolName, encryptedCredentials) ->
                exportObject.put(toolName, encryptedCredentials)
            }
            
            exportObject.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting credentials", e)
            credentialListener?.onError("Error exporting credentials: ${e.message}")
            "{}"
        }
    }
    
    /**
     * Import credentials from backup
     */
    fun importCredentials(backupData: String): Boolean {
        return try {
            val backupObject = JSONObject(backupData)
            val prefs = context.getSharedPreferences("n8n_credentials", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            val keys = backupObject.keys()
            while (keys.hasNext()) {
                val toolName = keys.next()
                val encryptedCredentials = backupObject.getString(toolName)
                editor.putString(toolName, encryptedCredentials)
            }
            
            editor.apply()
            Log.d(TAG, "Credentials imported successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing credentials", e)
            credentialListener?.onError("Error importing credentials: ${e.message}")
            false
        }
    }
    
    /**
     * Get credential metadata (without exposing actual credentials)
     */
    fun getCredentialMetadata(toolName: String): Map<String, Any>? {
        return try {
            val credentials = retrieveCredentials(toolName)
            if (credentials == null) {
                Log.d(TAG, "No credentials found for tool: $toolName")
                return null
            }
            
            val metadata = mutableMapOf<String, Any>()
            metadata["toolName"] = toolName
            metadata["credentialCount"] = credentials.size
            metadata["lastModified"] = System.currentTimeMillis()
            metadata["fields"] = credentials.keys.toList()
            
            metadata
        } catch (e: Exception) {
            Log.e(TAG, "Error getting credential metadata for tool: $toolName", e)
            credentialListener?.onError("Error getting metadata: ${e.message}")
            null
        }
    }
}