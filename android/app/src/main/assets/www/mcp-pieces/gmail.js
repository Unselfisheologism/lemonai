// Gmail MCP Piece
// This is a simplified version for mobile implementation

const gmailPiece = {
  name: 'gmail',
  actions: {
    listEmails: {
      description: 'List emails from Gmail',
      run: async ({ auth, params }) => {
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
        } = params || {};
        
        // Build Gmail API URL
        let apiUrl = `https://gmail.googleapis.com/gmail/v1/users/me/messages?labelIds=${mailbox}&maxResults=${limit}`;
        
        // Add query parameter if provided
        if (query) {
          apiUrl += `&q=${encodeURIComponent(query)}`;
        }
        
        try {
          // Make API request to Gmail
          const response = await fetch(apiUrl, {
            headers: {
              'Authorization': `Bearer ${auth.accessToken}`,
              'Content-Type': 'application/json'
            }
          });
          
          if (!response.ok) {
            throw new Error(`Gmail API error: ${response.statusText}`);
          }
          
          const data = await response.json();
          const messages = data.messages || [];
          
          // Get detailed information for each message
          const emails = [];
          for (const message of messages) {
            const messageResponse = await fetch(
              `https://gmail.googleapis.com/gmail/v1/users/me/messages/${message.id}`,
              {
                headers: {
                  'Authorization': `Bearer ${auth.accessToken}`,
                  'Content-Type': 'application/json'
                }
              }
            );
            
            if (messageResponse.ok) {
              const messageData = await messageResponse.json();
              
              // Extract relevant information
              const email = {
                id: messageData.id,
                threadId: messageData.threadId,
                snippet: messageData.snippet,
                subject: '',
                from: '',
                to: '',
                date: messageData.internalDate ? new Date(parseInt(messageData.internalDate)).toISOString() : '',
                size: messageData.sizeEstimate || 0,
                labels: messageData.labelIds || []
              };
              
              // Parse headers
              if (messageData.payload && messageData.payload.headers) {
                for (const header of messageData.payload.headers) {
                  if (header.name === 'Subject') {
                    email.subject = header.value || '';
                  } else if (header.name === 'From') {
                    email.from = header.value || '';
                  } else if (header.name === 'To') {
                    email.to = header.value || '';
                  }
                }
              }
              
              emails.push(email);
            }
          }
          
          // Apply unread filter if requested
          let result = emails;
          if (unread) {
            result = result.filter(email => email.labels.includes('UNREAD'));
          }
          
          // Apply limit
          result = result.slice(0, limit);
          
          return {
            success: true,
            data: result
          };
        } catch (error) {
          console.error('Error listing emails:', error);
          throw error;
        }
      }
    },
    sendEmail: {
      description: 'Send an email via Gmail',
      run: async ({ auth, params }) => {
        // In a real implementation, this would send an email via Gmail API
        console.log('Sending email with params:', params);
        return {
          success: true,
          messageId: 'test-message-id',
          message: 'Email sent successfully'
        };
      }
    },
    searchEmails: {
      description: 'Search emails in Gmail',
      run: async ({ auth, params }) => {
        // In a real implementation, this would search emails via Gmail API
        console.log('Searching emails with params:', params);
        return {
          success: true,
          data: [
            { id: 1, subject: 'Search Result 1', from: 'result@example.com' }
          ]
        };
      }
    }
  },
  triggers: {
    newEmail: {
      description: 'Trigger when a new email arrives',
      run: async ({ auth, params }) => {
        // In a real implementation, this would set up a Gmail push notification
        console.log('Setting up new email trigger with params:', params);
        return {
          success: true,
          message: 'New email trigger set up'
        };
      }
    }
  }
};

// Function to execute specific actions
function executeGmailAction(action, params) {
  if (gmailPiece.actions[action]) {
    return gmailPiece.actions[action].run({ auth: null, params });
  } else {
    throw new Error(`Action ${action} not found in Gmail piece`);
  }
}

// Export the piece and execution function
module.exports = exports = {
  ...gmailPiece,
  executeGmailAction
};