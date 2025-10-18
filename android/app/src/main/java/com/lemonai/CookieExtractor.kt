package com.lemonai

import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebView
import org.json.JSONObject
import java.net.HttpCookie
import java.net.URI
import java.util.*

/**
 * Cookie extractor for session cookies from WebView as described in answers.md
 * This component handles extraction of session cookies for external tool integration
 * with n8n nodes as mentioned in desire.md
 */
class CookieExtractor(private val context: Context) {
    private val TAG = "CookieExtractor"
    
    // Callback interface for handling cookie extraction events
    interface CookieExtractionListener {
        fun onCookiesExtracted(cookies: List<ExtractedCookie>)
        fun onExtractionError(error: String)
        fun onProgress(progress: Float)
    }
    
    private var extractionListener: CookieExtractionListener? = null
    
    fun setCookieExtractionListener(listener: CookieExtractionListener) {
        extractionListener = listener
    }
    
    /**
     * Extract all cookies for a specific domain
     */
    fun extractCookiesForDomain(domain: String, callback: (List<ExtractedCookie>) -> Unit) {
        try {
            extractionListener?.onProgress(0.1f)
            
            val cookieManager = CookieManager.getInstance()
            val cookiesString = cookieManager.getCookie(domain)
            
            if (cookiesString.isNullOrEmpty()) {
                Log.d(TAG, "No cookies found for domain: $domain")
                callback(emptyList())
                extractionListener?.onCookiesExtracted(emptyList())
                return
            }
            
            extractionListener?.onProgress(0.5f)
            
            val extractedCookies = parseCookiesString(cookiesString, domain)
            
            extractionListener?.onProgress(1.0f)
            extractionListener?.onCookiesExtracted(extractedCookies)
            callback(extractedCookies)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting cookies for domain: $domain", e)
            extractionListener?.onExtractionError("Error extracting cookies: ${e.message}")
            callback(emptyList())
        }
    }
    
    /**
     * Extract session cookies for a specific domain
     */
    fun extractSessionCookiesForDomain(domain: String, callback: (List<ExtractedCookie>) -> Unit) {
        extractCookiesForDomain(domain) { allCookies ->
            val sessionCookies = allCookies.filter { cookie ->
                // Filter for session cookies (no expiration or expires in the future)
                cookie.expiresAt == null || cookie.expiresAt > System.currentTimeMillis()
            }
            
            callback(sessionCookies)
            extractionListener?.onCookiesExtracted(sessionCookies)
        }
    }
    
    /**
     * Extract all cookies from WebView
     */
    fun extractAllCookies(callback: (List<ExtractedCookie>) -> Unit) {
        try {
            extractionListener?.onProgress(0.1f)
            
            val cookieManager = CookieManager.getInstance()
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.getCookie("", object : ValueCallback<String> {
                    override fun onReceiveValue(value: String?) {
                        if (value.isNullOrEmpty()) {
                            Log.d(TAG, "No cookies found")
                            callback(emptyList())
                            extractionListener?.onCookiesExtracted(emptyList())
                            return
                        }
                        
                        extractionListener?.onProgress(0.5f)
                        
                        val extractedCookies = parseCookiesString(value, "")
                        
                        extractionListener?.onProgress(1.0f)
                        extractionListener?.onCookiesExtracted(extractedCookies)
                        callback(extractedCookies)
                    }
                })
            } else {
                @Suppress("DEPRECATION")
                val cookiesString = cookieManager.getCookie("")
                if (cookiesString.isNullOrEmpty()) {
                    Log.d(TAG, "No cookies found")
                    callback(emptyList())
                    extractionListener?.onCookiesExtracted(emptyList())
                    return
                }
                
                extractionListener?.onProgress(0.5f)
                
                val extractedCookies = parseCookiesString(cookiesString, "")
                
                extractionListener?.onProgress(1.0f)
                extractionListener?.onCookiesExtracted(extractedCookies)
                callback(extractedCookies)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting all cookies", e)
            extractionListener?.onExtractionError("Error extracting cookies: ${e.message}")
            callback(emptyList())
        }
    }
    
    /**
     * Parse cookies string into ExtractedCookie objects
     */
    private fun parseCookiesString(cookiesString: String, domain: String): List<ExtractedCookie> {
        val cookies = mutableListOf<ExtractedCookie>()
        
        try {
            // Split by semicolon to get individual cookies
            val cookieParts = cookiesString.split(";")
            
            for (cookiePart in cookieParts) {
                val trimmedCookie = cookiePart.trim()
                if (trimmedCookie.isEmpty()) continue
                
                // Find the first equals sign to separate name and value
                val equalsIndex = trimmedCookie.indexOf('=')
                if (equalsIndex <= 0) continue // Invalid cookie format
                
                val name = trimmedCookie.substring(0, equalsIndex)
                val value = trimmedCookie.substring(equalsIndex + 1)
                
                // Create a basic cookie object
                val cookie = ExtractedCookie(
                    name = name,
                    value = value,
                    domain = domain.ifEmpty { extractDomainFromCookie(trimmedCookie) },
                    path = "/",
                    isSecure = false,
                    isHttpOnly = false,
                    expiresAt = null
                )
                
                cookies.add(cookie)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing cookies string", e)
        }
        
        return cookies
    }
    
    /**
     * Extract domain from cookie string (simple implementation)
     */
    private fun extractDomainFromCookie(cookieString: String): String {
        // Look for Domain attribute in cookie string
        val domainRegex = Regex("(?i)Domain=([^;]*)")
        val match = domainRegex.find(cookieString)
        return match?.groupValues?.get(1)?.trim() ?: ""
    }
    
    /**
     * Get specific cookie value by name for a domain
     */
    fun getCookieValue(domain: String, cookieName: String): String? {
        try {
            val cookieManager = CookieManager.getInstance()
            val cookiesString = cookieManager.getCookie(domain)
            
            if (cookiesString.isNullOrEmpty()) {
                return null
            }
            
            // Parse cookies to find the specific one
            val cookies = parseCookiesString(cookiesString, domain)
            val cookie = cookies.find { it.name.equals(cookieName, ignoreCase = true) }
            
            return cookie?.value
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cookie value", e)
            return null
        }
    }
    
    /**
     * Set a cookie for a domain
     */
    fun setCookie(domain: String, cookie: ExtractedCookie) {
        try {
            val cookieManager = CookieManager.getInstance()
            
            // Build cookie string
            val cookieString = buildCookieString(cookie)
            
            cookieManager.setCookie(domain, cookieString)
            
            // Flush cookies to persist
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.flush()
            }
            
            Log.d(TAG, "Cookie set for domain: $domain")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting cookie", e)
            extractionListener?.onExtractionError("Error setting cookie: ${e.message}")
        }
    }
    
    /**
     * Build cookie string from ExtractedCookie object
     */
    private fun buildCookieString(cookie: ExtractedCookie): String {
        val sb = StringBuilder()
        sb.append("${cookie.name}=${cookie.value}")
        
        if (cookie.domain.isNotEmpty()) {
            sb.append("; Domain=${cookie.domain}")
        }
        
        if (cookie.path.isNotEmpty()) {
            sb.append("; Path=${cookie.path}")
        }
        
        if (cookie.isSecure) {
            sb.append("; Secure")
        }
        
        if (cookie.isHttpOnly) {
            sb.append("; HttpOnly")
        }
        
        cookie.expiresAt?.let { expires ->
            val date = java.util.Date(expires)
            sb.append("; Expires=${date}")
        }
        
        return sb.toString()
    }
    
    /**
     * Clear all cookies
     */
    fun clearAllCookies() {
        try {
            val cookieManager = CookieManager.getInstance()
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.removeAllCookies(null)
            } else {
                @Suppress("DEPRECATION")
                cookieManager.removeAllCookie()
            }
            
            Log.d(TAG, "All cookies cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cookies", e)
            extractionListener?.onExtractionError("Error clearing cookies: ${e.message}")
        }
    }
    
    /**
     * Clear cookies for a specific domain
     */
    fun clearCookiesForDomain(domain: String) {
        try {
            val cookieManager = CookieManager.getInstance()
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setCookie(domain, "expires=Thu, 01 Jan 1970 00:00:00 GMT")
                cookieManager.flush()
            } else {
                @Suppress("DEPRECATION")
                cookieManager.setCookie(domain, "expires=Thu, 01 Jan 1970 00:00:00 GMT")
                cookieManager.removeSessionCookie()
            }
            
            Log.d(TAG, "Cookies cleared for domain: $domain")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cookies for domain: $domain", e)
            extractionListener?.onExtractionError("Error clearing cookies: ${e.message}")
        }
    }
    
    /**
     * Convert cookies to JSON format for n8n integration
     */
    fun cookiesToJson(cookies: List<ExtractedCookie>): String {
        return try {
            val jsonArray = org.json.JSONArray()
            
            for (cookie in cookies) {
                val jsonObject = org.json.JSONObject().apply {
                    put("name", cookie.name)
                    put("value", cookie.value)
                    put("domain", cookie.domain)
                    put("path", cookie.path)
                    put("secure", cookie.isSecure)
                    put("httpOnly", cookie.isHttpOnly)
                    cookie.expiresAt?.let { put("expires", it) }
                }
                jsonArray.put(jsonObject)
            }
            
            jsonArray.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting cookies to JSON", e)
            "[]"
        }
    }
    
    /**
     * Data class for extracted cookie
     */
    data class ExtractedCookie(
        val name: String,
        val value: String,
        val domain: String,
        val path: String,
        val isSecure: Boolean,
        val isHttpOnly: Boolean,
        val expiresAt: Long? // Unix timestamp in milliseconds
    )
}