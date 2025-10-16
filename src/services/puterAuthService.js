// src/services/puterAuthService.js
const { init } = require("@heyputer/puter.js/src/init.cjs"); // NODE JS ONLY

class PuterAuthService {
  constructor() {
    this.authenticated = false;
    this.user = null;
    this.puter = null;
  }

  async init() {
    // Initialize Puter.js - handles authentication automatically
    // Initialize Puter.js with token from environment, or undefined if not set
    // Puter.js handles authentication automatically when no token is provided in browser environments
    const token = typeof process !== 'undefined' && process.env ? process.env.PUTER_AUTH_TOKEN : undefined;
    this.puter = init(token);
    return this;
  }

  async login() {
    try {
      // Login using Puter.js
      await this.puter.auth.signIn();
      this.authenticated = true;
      this.user = await this.puter.auth.getUser();
      return this.user;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

  async logout() {
    try {
      this.puter.auth.signOut();
      this.authenticated = false;
      this.user = null;
    } catch (error) {
      console.error('Logout failed:', error);
      throw error;
    }
  }

  async isSignedIn() {
    return this.puter.auth.isSignedIn();
  }

  async getCurrentUser() {
    if (!this.user) {
      this.user = await this.puter.auth.getUser();
    }
    return this.user;
  }

 async saveUserPreferences(preferences) {
    try {
      const user = await this.getCurrentUser();
      // Save user preferences to Puter's key-value store
      return await this.puter.kv.set(`users/${user.uuid}/preferences`, preferences);
    } catch (error) {
      console.error('Failed to save user preferences:', error);
      throw error;
    }
  }

  async getUserPreferences() {
    try {
      const user = await this.getCurrentUser();
      // Get user preferences from Puter's key-value store
      return await this.puter.kv.get(`users/${user.uuid}/preferences`);
    } catch (error) {
      console.error('Failed to get user preferences:', error);
      throw error;
    }
  }

  async saveConversation(conversation) {
    try {
      const user = await this.getCurrentUser();
      // Save conversation to Puter's key-value store
      return await this.puter.kv.set(`users/${user.uuid}/conversations/${conversation.id}`, conversation);
    } catch (error) {
      console.error('Failed to save conversation:', error);
      throw error;
    }
  }

  async getConversation(conversationId) {
    try {
      const user = await this.getCurrentUser();
      // Get conversation from Puter's key-value store
      return await this.puter.kv.get(`users/${user.uuid}/conversations/${conversationId}`);
    } catch (error) {
      console.error('Failed to get conversation:', error);
      throw error;
    }
  }

  async saveFile(filename, content) {
    try {
      // Save file to Puter's file system
      return await this.puter.fs.write(filename, content);
    } catch (error) {
      console.error('Failed to save file:', error);
      throw error;
    }
  }

  async readFile(filename) {
    try {
      // Read file from Puter's file system
      const file = await this.puter.fs.read(filename);
      return await file.text();
    } catch (error) {
      console.error('Failed to read file:', error);
      throw error;
    }
  }

  async listFiles(directory = '~/') {
    try {
      // List files in a directory
      return await this.puter.fs.readdir(directory);
    } catch (error) {
      console.error('Failed to list files:', error);
      throw error;
    }
  }
}

module.exports = new PuterAuthService();