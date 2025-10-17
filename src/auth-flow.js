/**
 * AuthFlow.js - WebView-based OAuth Flow for Service Connections
 * 
 * This file handles the OAuth flow for connecting services like Gmail and Notion
 * through a WebView interface, storing tokens securely on Android.
 */

class AuthFlow {
  constructor() {
    this.authProviders = {
      gmail: {
        name: 'Gmail',
        authUrl: 'https://accounts.google.com/o/oauth2/auth',
        tokenUrl: 'https://oauth2.googleapis.com/token',
        scopes: [
          'https://www.googleapis.com/auth/gmail.readonly',
          'https://www.googleapis.com/auth/gmail.send',
          'https://www.googleapis.com/auth/gmail.modify'
        ],
        clientId: null, // Will be set via config
        redirectUri: 'com.lemonai:/oauth/callback'
      },
      notion: {
        name: 'Notion',
        authUrl: 'https://api.notion.com/v1/oauth/authorize',
        tokenUrl: 'https://api.notion.com/v1/oauth/token',
        scopes: [],
        clientId: null, // Will be set via config
        redirectUri: 'com.lemonai:/oauth/callback'
      }
    };
    this.isAndroid = this.detectAndroid();
  }

 /**
   * Detect if running on Android
   * @returns {boolean} True if running on Android
   */
  detectAndroid() {
    return typeof window !== 'undefined' && 
           window.AndroidInterface !== undefined;
  }

  /**
   * Initiate OAuth flow for a specific service
   * @param {string} service - Service name (e.g., 'gmail', 'notion')
   * @param {Object} config - Configuration including client ID
   * @returns {Promise<Object>} Authentication result
   */
  async initiateAuthFlow(service, config = {}) {
    const provider = this.authProviders[service];
    if (!provider) {
      throw new Error(`Unsupported service: ${service}`);
    }

    // Set the client ID from config
    provider.clientId = config.clientId;
    if (!provider.clientId) {
      throw new Error(`Client ID required for ${service} authentication`);
    }

    // Generate a state parameter for security
    const state = this.generateState();
    
    // Store state in sessionStorage to verify callback
    sessionStorage.setItem('oauth_state', state);
    sessionStorage.setItem('oauth_service', service);

    // Build authorization URL
    let authUrl;
    if (service === 'gmail') {
      // Google OAuth URL
      const params = new URLSearchParams({
        client_id: provider.clientId,
        redirect_uri: provider.redirectUri,
        scope: provider.scopes.join(' '),
        response_type: 'code',
        access_type: 'offline',
        prompt: 'consent',
        state: state
      });
      authUrl = `${provider.authUrl}?${params.toString()}`;
    } else if (service === 'notion') {
      // Notion OAuth URL
      const params = new URLSearchParams({
        owner: 'user',
        redirect_uri: provider.redirectUri,
        response_type: 'code',
        client_id: provider.clientId,
        state: state
      });
      authUrl = `${provider.authUrl}?${params.toString()}`;
    }

    // Store the auth URL for the native layer to open
    sessionStorage.setItem('oauth_auth_url', authUrl);

    if (this.isAndroid) {
      // On Android, use the native interface to open the auth URL
      try {
        const success = await window.AndroidInterface.openUrl(authUrl);
        if (!success) {
          throw new Error(`Failed to open authentication URL for ${service}`);
        }
      } catch (error) {
        console.error(`Error opening auth URL for ${service}:`, error);
        throw error;
      }
    } else {
      // For testing, open the URL in the same window
      console.log(`Opening auth URL for ${service}: ${authUrl}`);
      window.location.href = authUrl;
    }

    // Wait for the callback to complete the flow
    return this.waitForAuthCallback(service);
  }

  /**
   * Handle the OAuth callback from the redirect
   * @param {string} service - Service name
   * @param {Object} callbackParams - Parameters from the OAuth callback
   * @returns {Promise<Object>} Authentication result
   */
  async handleAuthCallback(service, callbackParams) {
    const provider = this.authProviders[service];
    if (!provider) {
      throw new Error(`Unsupported service: ${service}`);
    }

    // Verify the state parameter to prevent CSRF
    const storedState = sessionStorage.getItem('oauth_state');
    const callbackState = callbackParams.state;
    
    if (!storedState || storedState !== callbackState) {
      throw new Error('Invalid state parameter - possible CSRF attack');
    }

    // Check for error in callback
    if (callbackParams.error) {
      throw new Error(`OAuth error: ${callbackParams.error}`);
    }

    // Get the authorization code
    const code = callbackParams.code;
    if (!code) {
      throw new Error('No authorization code received');
    }

    // Exchange the authorization code for an access token
    const tokenData = await this.exchangeCodeForToken(service, code, provider);

    // Store the authentication data securely
    await this.storeAuthData(service, tokenData);

    // Clean up session storage
    sessionStorage.removeItem('oauth_state');
    sessionStorage.removeItem('oauth_service');
    sessionStorage.removeItem('oauth_auth_url');

    return {
      success: true,
      service: service,
      tokenData: tokenData
    };
  }

  /**
   * Exchange authorization code for access token
   * @param {string} service - Service name
   * @param {string} code - Authorization code
   * @param {Object} provider - Provider configuration
   * @returns {Promise<Object>} Token data
   */
  async exchangeCodeForToken(service, code, provider) {
    const tokenUrl = provider.tokenUrl;
    
    // Prepare the request body
    const body = new URLSearchParams({
      client_id: provider.clientId,
      client_secret: provider.clientSecret || '', // In a real implementation, this would come from secure storage
      code: code,
      grant_type: 'authorization_code',
      redirect_uri: provider.redirectUri
    });

    // Make the token exchange request
    const response = await fetch(tokenUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: body.toString()
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(`Token exchange failed: ${errorData.error || response.statusText}`);
    }

    const tokenData = await response.json();
    
    // Add service and timestamp info
    tokenData.service = service;
    tokenData.grantedAt = new Date().toISOString();
    
    return tokenData;
  }

 /**
   * Store authentication data securely
   * @param {string} service - Service name
   * @param {Object} tokenData - Token data to store
   * @returns {Promise<boolean>} True if storage was successful
   */
 async storeAuthData(service, tokenData) {
    if (this.isAndroid) {
      // On Android, use the native interface to store tokens securely
      try {
        // Prepare the data to store
        const authData = {
          service: service,
          tokenData: tokenData,
          storedAt: new Date().toISOString()
        };

        // Store the authentication data using the native interface
        const success = await window.AndroidInterface.storeSecureData(
          `auth_${service}`, 
          JSON.stringify(authData)
        );

        return success;
      } catch (error) {
        console.error(`Error storing auth data for ${service}:`, error);
        return false;
      }
    } else {
      // For testing, store in localStorage
      console.log(`Storing auth data for ${service} (test mode)`);
      localStorage.setItem(`auth_${service}`, JSON.stringify(tokenData));
      return true;
    }
  }

  /**
   * Retrieve stored authentication data
   * @param {string} service - Service name
   * @returns {Promise<Object|null>} Token data if found, null otherwise
   */
  async getStoredAuthData(service) {
    if (this.isAndroid) {
      // On Android, use the native interface to retrieve tokens securely
      try {
        const storedData = await window.AndroidInterface.getSecureData(`auth_${service}`);
        
        if (storedData) {
          const authData = JSON.parse(storedData);
          return authData.tokenData || authData;
        }
        
        return null;
      } catch (error) {
        console.error(`Error retrieving auth data for ${service}:`, error);
        return null;
      }
    } else {
      // For testing, retrieve from localStorage
      const storedData = localStorage.getItem(`auth_${service}`);
      return storedData ? JSON.parse(storedData) : null;
    }
  }

  /**
   * Check if a service is connected (has valid auth data)
   * @param {string} service - Service name
   * @returns {Promise<boolean>} True if service is connected
   */
  async isServiceConnected(service) {
    const authData = await this.getStoredAuthData(service);
    if (!authData) {
      return false;
    }

    // Check if token is expired (considering a 1-hour buffer for safety)
    if (authData.expires_at) {
      const expiresAt = new Date(authData.expires_at);
      const now = new Date();
      const buffer = 60 * 60 * 1000; // 1 hour in milliseconds
      
      if (now.getTime() + buffer > expiresAt.getTime()) {
        return false; // Token will expire soon
      }
    }

    return true;
  }

  /**
   * Refresh an access token using the refresh token
   * @param {string} service - Service name
   * @returns {Promise<Object|null>} New token data if successful, null otherwise
   */
  async refreshToken(service) {
    const authData = await this.getStoredAuthData(service);
    if (!authData || !authData.refresh_token) {
      console.warn(`No refresh token available for ${service}`);
      return null;
    }

    const provider = this.authProviders[service];
    if (!provider) {
      throw new Error(`Unsupported service: ${service}`);
    }

    // Prepare the request body for refresh
    const body = new URLSearchParams({
      client_id: provider.clientId,
      client_secret: provider.clientSecret || '', // In a real implementation, this would come from secure storage
      refresh_token: authData.refresh_token,
      grant_type: 'refresh_token'
    });

    try {
      // Make the refresh request
      const response = await fetch(provider.tokenUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: body.toString()
      });

      if (!response.ok) {
        console.error(`Token refresh failed for ${service}:`, response.statusText);
        return null;
      }

      const newTokenData = await response.json();
      
      // Update the stored auth data with new tokens
      const updatedAuthData = {
        ...authData,
        ...newTokenData,
        refreshedAt: new Date().toISOString()
      };

      await this.storeAuthData(service, updatedAuthData);
      
      return updatedAuthData;
    } catch (error) {
      console.error(`Error refreshing token for ${service}:`, error);
      return null;
    }
  }

 /**
   * Wait for the OAuth callback to complete the flow
   * @param {string} service - Service name
   * @returns {Promise<Object>} Authentication result
   */
  waitForAuthCallback(service) {
    return new Promise((resolve, reject) => {
      // Set a timeout for the auth flow (5 minutes)
      const timeoutId = setTimeout(() => {
        reject(new Error(`Authentication timeout for ${service}`));
      }, 5 * 60 * 100); // 5 minutes

      // Listen for a custom event that would be triggered by the callback
      const handleAuthComplete = (event) => {
        clearTimeout(timeoutId);
        
        if (event.detail.service === service) {
          window.removeEventListener('authComplete', handleAuthComplete);
          resolve(event.detail.result);
        }
      };

      window.addEventListener('authComplete', handleAuthComplete);
    });
 }

  /**
   * Generate a random state parameter for OAuth security
   * @returns {string} Random state string
   */
  generateState() {
    // Generate a random string for state parameter
    const array = new Uint8Array(32);
    crypto.getRandomValues(array);
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
  }

 /**
   * Disconnect a service by removing stored auth data
   * @param {string} service - Service name
   * @returns {Promise<boolean>} True if disconnection was successful
   */
  async disconnectService(service) {
    if (this.isAndroid) {
      try {
        const success = await window.AndroidInterface.removeSecureData(`auth_${service}`);
        return success;
      } catch (error) {
        console.error(`Error disconnecting service ${service}:`, error);
        return false;
      }
    } else {
      // For testing, remove from localStorage
      localStorage.removeItem(`auth_${service}`);
      return true;
    }
  }
}

// Create a singleton instance
const authFlow = new AuthFlow();

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
 module.exports = authFlow;
} else if (typeof window !== 'undefined') {
  window.authFlow = authFlow;
}