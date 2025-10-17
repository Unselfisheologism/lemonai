/**
 * Mock Gmail Piece Implementation for Mobile LemonAI
 * 
 * This is a JavaScript mock implementation of what would be a compiled WASM module.
 * In a real implementation, this would be actual WASM code compiled from @activepieces/piece-gmail.
 */

// This would be replaced by actual WASM exports in a real implementation
const gmailPiece = {
  /**
   * List emails in a Gmail account
   * @param {Object} auth - Authentication data (access token)
   * @param {Object} props - Properties for the action
   * @returns {Promise<Array>} List of emails
   */
  async listEmails(auth, props) {
    // In a real implementation, this would call actual WASM functions
    // For now, we'll simulate the functionality
    
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 500));
    
    // Validate authentication
    if (!auth || !auth.accessToken) {
      throw new Error('Gmail authentication required');
    }
    
    // Extract parameters
    const {
      mailbox = 'INBOX',
      limit = 10,
      unread = false,
      query = ''
    } = props || {};
    
    // Mock email data
    const mockEmails = [
      {
        id: '1',
        threadId: 'thread1',
        snippet: 'This is a sample email snippet',
        subject: 'Sample Email Subject',
        from: 'sender@example.com',
        to: 'recipient@example.com',
        date: new Date().toISOString(),
        size: 1024,
        labels: unread ? ['UNREAD', 'INBOX'] : ['INBOX']
      },
      {
        id: '2',
        threadId: 'thread2',
        snippet: 'Another sample email',
        subject: 'Another Email',
        from: 'another@example.com',
        to: 'recipient@example.com',
        date: new Date(Date.now() - 3600000).toISOString(), // 1 hour ago
        size: 2048,
        labels: unread ? ['UNREAD', 'INBOX'] : ['INBOX']
      }
    ];
    
    // Apply filters based on parameters
    let result = mockEmails;
    
    if (unread) {
      result = result.filter(email => email.labels.includes('UNREAD'));
    }
    
    if (query) {
      result = result.filter(email => 
        email.subject.toLowerCase().includes(query.toLowerCase()) ||
        email.snippet.toLowerCase().includes(query.toLowerCase())
      );
    }
    
    // Apply limit
    result = result.slice(0, limit);
    
    return result;
  },

 /**
   * Get details of a specific email
   * @param {Object} auth - Authentication data (access token)
   * @param {Object} props - Properties for the action (includes email ID)
   * @returns {Promise<Object>} Email details
   */
  async getEmailDetails(auth, props) {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 300));
    
    // Validate authentication
    if (!auth || !auth.accessToken) {
      throw new Error('Gmail authentication required');
    }
    
    // Extract parameters
    const { emailId } = props || {};
    
    if (!emailId) {
      throw new Error('Email ID is required');
    }
    
    // Mock email details
    return {
      id: emailId,
      threadId: `thread_${emailId}`,
      subject: 'Detailed Email Subject',
      from: 'sender@example.com',
      to: 'recipient@example.com',
      cc: [],
      bcc: [],
      date: new Date().toISOString(),
      body: {
        text: 'This is the plain text body of the email.',
        html: '<p>This is the <b>HTML</b> body of the email.</p>'
      },
      attachments: [],
      labels: ['INBOX'],
      sizeEstimate: 1536
    };
 },

  /**
   * Send an email
   * @param {Object} auth - Authentication data (access token)
   * @param {Object} props - Properties for the action (email content)
   * @returns {Promise<Object>} Result of the send operation
   */
  async sendEmail(auth, props) {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 800));
    
    // Validate authentication
    if (!auth || !auth.accessToken) {
      throw new Error('Gmail authentication required');
    }
    
    // Extract parameters
    const { to, subject, body, cc, bcc } = props || {};
    
    if (!to || !subject || !body) {
      throw new Error('Recipient, subject, and body are required to send an email');
    }
    
    // Mock send result
    return {
      success: true,
      messageId: 'mock_message_id_123',
      sentTime: new Date().toISOString(),
      recipients: {
        to: Array.isArray(to) ? to : [to],
        cc: cc ? (Array.isArray(cc) ? cc : [cc]) : [],
        bcc: bcc ? (Array.isArray(bcc) ? bcc : [bcc]) : []
      }
    };
 },

  /**
   * Create a draft email
   * @param {Object} auth - Authentication data (access token)
   * @param {Object} props - Properties for the action (email content)
   * @returns {Promise<Object>} Result of the draft creation
   */
  async createDraft(auth, props) {
    // Simulate network delay
    await new Promise(resolve => setTimeout(resolve, 400));
    
    // Validate authentication
    if (!auth || !auth.accessToken) {
      throw new Error('Gmail authentication required');
    }
    
    // Extract parameters
    const { to, subject, body, cc, bcc } = props || {};
    
    // Mock draft result
    return {
      success: true,
      draftId: 'mock_draft_id_456',
      draft: {
        to: Array.isArray(to) ? to : [to],
        subject: subject || '',
        body: body || '',
        cc: cc ? (Array.isArray(cc) ? cc : [cc]) : [],
        bcc: bcc ? (Array.isArray(bcc) ? bcc : [bcc]) : [],
        creationTime: new Date().toISOString()
      }
    };
  }
};

// Export for use in the WASM pieces manager
if (typeof module !== 'undefined' && module.exports) {
  module.exports = gmailPiece;
} else if (typeof window !== 'undefined') {
  window.gmailPiece = gmailPiece;
}