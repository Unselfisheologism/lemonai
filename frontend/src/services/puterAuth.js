// frontend/src/services/puterAuth.js
import { puter } from '@heyputer/puter.js';

class PuterAuthService {
  constructor() {
    this.authenticated = false;
    this.user = null;
  }

  async init() {
    // Initialize Puter.js (no API keys required)
    // Puter.js is automatically initialized when imported
    return this;
  }

  async login() {
    try {
      // Login using Puter.js
      await puter.auth.signIn();
      this.authenticated = true;
      this.user = await puter.auth.getUser();
      return this.user;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

 async logout() {
    try {
      puter.auth.signOut();
      this.authenticated = false;
      this.user = null;
    } catch (error) {
      console.error('Logout failed:', error);
      throw error;
    }
  }

 async isSignedIn() {
    return puter.auth.isSignedIn();
  }

 async getCurrentUser() {
    if (!this.user) {
      this.user = await puter.auth.getUser();
    }
    return this.user;
  }

  async saveUserPreferences(preferences) {
    try {
      const user = await this.getCurrentUser();
      // Save user preferences to Puter's key-value store
      return await puter.kv.set(`users/${user.uuid}/preferences`, preferences);
    } catch (error) {
      console.error('Failed to save user preferences:', error);
      throw error;
    }
 }

  async getUserPreferences() {
    try {
      const user = await this.getCurrentUser();
      // Get user preferences from Puter's key-value store
      return await puter.kv.get(`users/${user.uuid}/preferences`);
    } catch (error) {
      console.error('Failed to get user preferences:', error);
      throw error;
    }
  }

  async saveConversation(conversation) {
    try {
      const user = await this.getCurrentUser();
      // Save conversation to Puter's key-value store
      return await puter.kv.set(`users/${user.uuid}/conversations/${conversation.id}`, conversation);
    } catch (error) {
      console.error('Failed to save conversation:', error);
      throw error;
    }
  }

  async getConversation(conversationId) {
    try {
      const user = await this.getCurrentUser();
      // Get conversation from Puter's key-value store
      return await puter.kv.get(`users/${user.uuid}/conversations/${conversationId}`);
    } catch (error) {
      console.error('Failed to get conversation:', error);
      throw error;
    }
  }

  async saveFile(filename, content) {
    try {
      // Save file to Puter's file system
      return await puter.fs.write(filename, content);
    } catch (error) {
      console.error('Failed to save file:', error);
      throw error;
    }
  }

  async readFile(filename) {
    try {
      // Read file from Puter's file system
      const file = await puter.fs.read(filename);
      return await file.text();
    } catch (error) {
      console.error('Failed to read file:', error);
      throw error;
    }
  }

  async listFiles(directory = '~/') {
    try {
      // List files in a directory
      return await puter.fs.readdir(directory);
    } catch (error) {
      console.error('Failed to list files:', error);
      throw error;
    }
  }

  async chatWithAI(prompt, options = {}) {
    try {
      // Use Puter.js AI chat functionality
      const response = await puter.ai.chat(prompt, options);
      return response;
    } catch (error) {
      console.error('AI chat failed:', error);
      throw error;
    }
  }

  async generateImage(prompt, options = {}) {
    try {
      // Use Puter.js AI image generation
      const image = await puter.ai.txt2img(prompt, options);
      return image;
    } catch (error) {
      console.error('Image generation failed:', error);
      throw error;
    }
  }

  async convertImageToText(imageUrl, options = {}) {
    try {
      // Use Puter.js AI image to text conversion
      const text = await puter.ai.img2txt(imageUrl, options);
      return text;
    } catch (error) {
      console.error('Image to text conversion failed:', error);
      throw error;
    }
  }
}

export default new PuterAuthService();