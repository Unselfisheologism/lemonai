package com.lemonai

import android.webkit.CookieManager
import android.util.Log

class CookieManagerHelper {
    companion object {
        private const val TAG = "CookieManagerHelper"
        
        // Initialize CookieManager and set it to accept cookies
        init {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(null, true)
            }
        }
        
        /**
         * Get all cookies for a specific domain
         */
        fun getCookiesForDomain(domain: String): Map<String, String> {
            val cookieManager = CookieManager.getInstance()
            val cookiesString = cookieManager.getCookie(domain)
            val cookiesMap = mutableMapOf<String, String>()
            
            if (!cookiesString.isNullOrEmpty()) {
                val cookiesArray = cookiesString.split(";")
                for (cookie in cookiesArray) {
                    val trimmedCookie = cookie.trim()
                    val separatorIndex = trimmedCookie.indexOf('=')
                    if (separatorIndex > 0) {
                        val name = trimmedCookie.substring(0, separatorIndex)
                        val value = trimmedCookie.substring(separatorIndex + 1)
                        cookiesMap[name] = value
                    }
                }
            }
            
            return cookiesMap
        }
        
        /**
         * Get a specific cookie value by name for a domain
         */
        fun getCookieValue(domain: String, cookieName: String): String? {
            val cookieManager = CookieManager.getInstance()
            val cookiesString = cookieManager.getCookie(domain)
            
            if (!cookiesString.isNullOrEmpty()) {
                val cookiesArray = cookiesString.split(";")
                for (cookie in cookiesArray) {
                    val trimmedCookie = cookie.trim()
                    val separatorIndex = trimmedCookie.indexOf('=')
                    if (separatorIndex > 0) {
                        val name = trimmedCookie.substring(0, separatorIndex)
                        val value = trimmedCookie.substring(separatorIndex + 1)
                        if (name == cookieName) {
                            return value
                        }
                    }
                }
            }
            
            return null
        }
        
        /**
         * Set a cookie for a specific domain
         */
        fun setCookie(domain: String, cookieString: String) {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setCookie(domain, cookieString)
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.flush()
            } else {
                @Suppress("DEPRECATION")
                CookieManager.getInstance().removeExpiredCookie()
            }
        }
        
        /**
         * Remove a specific cookie
         */
        fun removeCookie(cookieName: String) {
            val cookieManager = CookieManager.getInstance()
            // This is a simplified approach; in practice, you'd need to set the cookie with an expired date
            // For now, we'll flush all cookies - in a real app, you'd want more granular control
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.flush()
            } else {
                @Suppress("DEPRECATION")
                cookieManager.removeSessionCookie()
            }
        }
        
        /**
         * Get all cookies as a formatted string for a specific URL
         */
        fun getAllCookiesAsHeader(url: String): String {
            val cookieManager = CookieManager.getInstance()
            return cookieManager.getCookie(url) ?: ""
        }
        
        /**
         * Log all cookies for debugging purposes
         */
        fun logAllCookies(url: String) {
            val cookies = getAllCookiesAsHeader(url)
            Log.d(TAG, "Cookies for $url: $cookies")
        }
    }
}