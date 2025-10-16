const BaseLLM = require('./llm.base');
const { Readable } = require('stream');

// Import TYPE_ENUM from BaseLLM
const TYPE_ENUM = {
  SSE: 'SSE',
  STREAM: 'STREAM'
};

class PuterLLM extends BaseLLM {
  constructor(onTokenStream, model, llm_config) {
    super(onTokenStream);
    this.model = model || 'gpt-5-nano';
    this.llm_config = llm_config || {};
    this.splitter = '\n\n';
    
    // Extract model_info from llm_config if available
    if (llm_config && llm_config.model_info) {
      this.model_info = llm_config.model_info;
      // Map the requested model to a puter-compatible model
      this.model = this.mapModelToPuter(llm_config.model_info.model_name || this.model);
    } else {
      // If no model_info provided, use the model parameter directly
      this.model = this.mapModelToPuter(model);
    }
    
    // For frontend applications, Puter.js handles authentication automatically
    // We'll initialize with the global puter object that should be available
    // when the SDK is loaded in the frontend
    if (typeof window !== 'undefined' && window.puter) {
      this.puter = window.puter;
    } else if (typeof global !== 'undefined' && global.puter) {
      // For Node.js environments where puter is available globally
      this.puter = global.puter;
    } else {
      // For Node.js environments, we'll try to dynamically import the puter package
      try {
        const puterModule = require('@heyputer/puter.js');
        this.puter = puterModule;
      } catch (e) {
        // If puter is not available in Node.js, throw an error
        throw new Error("Puter.js SDK not available. Please install @heyputer/puter.js package or ensure puter.js is loaded before using PuterLLM.");
      }
    }
  }

  // Map various model names to puter-compatible models
 mapModelToPuter(modelName) {
    // Map common model names to puter-compatible equivalents from putermodels.md
    const modelMap = {
      // OpenAI models
      'gpt-3.5-turbo': 'gpt-4o-mini',
      'gpt-4': 'gpt-4o',
      'gpt-4-turbo': 'gpt-4o',
      'gpt-4o': 'gpt-4o',
      'gpt-4o-mini': 'gpt-4o-mini',
      'gpt-5': 'gpt-5',
      'gpt-5-nano': 'gpt-5-nano',
      'gpt-5-mini': 'gpt-5-mini',
      'o1-preview': 'o1',
      'o1-mini': 'o1-mini',
      'o1': 'o1',
      'o1-pro': 'o1-pro',
      'gpt-3.5-turbo-0613': 'gpt-4o-mini',
      'gpt-4-1106-preview': 'gpt-4o',
      'gpt-4-turbo-preview': 'gpt-4o',
      'gpt-4.1': 'gpt-4.1',
      'gpt-4.1-mini': 'gpt-4.1-mini',
      'gpt-4.1-nano': 'gpt-4.1-nano',
      'gpt-4.5-preview': 'gpt-4.5-preview',
      
      // Anthropic models
      'claude-3-haiku': 'claude-3-haiku-20240307',
      'claude-3-sonnet': 'claude-3-5-sonnet-20241022',
      'claude-3-opus': 'claude-opus-4',
      'claude-3-5-sonnet': 'claude-3-5-sonnet-20241022',
      'claude-sonnet-4': 'claude-sonnet-4',
      'claude-sonnet-4.5': 'claude-sonnet-4-5',
      'claude-opus-4': 'claude-opus-4',
      'claude-haiku-20240307': 'claude-3-haiku-20240307',
      'claude-3-7-sonnet': 'claude-3-7-sonnet-20250219',
      
      // Google models
      'gemini-pro': 'gemini-1.5-pro',
      'gemini-1.5-pro': 'gemini-1.5-pro',
      'gemini-1.5-flash': 'gemini-1.5-flash',
      'gemini-2.0-flash': 'gemini-2.0-flash',
      
      // Meta models
      'llama3': 'meta-llama/Meta-Llama-3-70B-Instruct-Turbo',
      'llama3.1': 'meta-llama/Meta-Llama-3.1-70B-Instruct-Turbo',
      'llama3.2': 'meta-llama/Llama-3.2-3B-Instruct-Turbo',
      'llama-3-70b': 'meta-llama/Meta-Llama-3-70B-Instruct-Turbo',
      'llama-3.1-405b': 'meta-llama/Meta-Llama-3.1-405B-Instruct-Turbo',
      'llama-3.3-70b': 'meta-llama/Llama-3.3-70B-Instruct-Turbo',
      'llama-3.3-8b': 'meta-llama/llama-3.3-8b-instruct',
      
      // Mistral
      'mistral-large': 'mistral-large-latest',
      'mistral-small': 'mistral-small-latest',
      'mistral-7b': 'open-mistral-7b',
      'mixtral-8x7b': 'open-mixtral-8x7b',
      'mistral-large-241': 'mistral-large-latest',
      'mistral-medium': 'mistral-medium-latest',
      'mistral-tiny': 'mistral-tiny-latest',
      
      // DeepSeek - using the main deepseek models for all variants
      'deepseek-v3-250324': 'deepseek-ai/DeepSeek-V3',
      'deepseek-v3-1-250821': 'deepseek-ai/DeepSeek-V3',
      'deepseek-chat': 'deepseek-chat',
      'deepseek-coder': 'deepseek-chat',
      'deepseek-r1': 'deepseek-ai/DeepSeek-R1',
      'deepseek-v3': 'deepseek-ai/DeepSeek-V3',
      
      // xAI
      'grok-beta': 'grok-beta',
      'grok-2': 'grok-2',
      'grok-2-vision': 'grok-2-vision',
      'grok-3': 'grok-3',
      'grok-3-mini': 'grok-3-mini',
      
      // Qwen
      'qwen3': 'Qwen/Qwen3-235B-A22B-Instruct-2507',
      'qwen2.5': 'Qwen/Qwen2.5-72B-Instruct-Turbo',
      'qwen-max': 'Qwen/Qwen3-235B-A22B-Instruct-2507',
      'qwen-plus': 'Qwen/Qwen3-235B-A22B-Instruct-2507',
      'qwen-turbo': 'Qwen/Qwen3-235B-A22B-Instruct-2507',
      'qwen3-coder': 'Qwen/Qwen3-Coder-480B-A35B-Instruct-FP8',
      
      // Default to gpt-5-nano if model not specifically mapped
      'default': 'gpt-5-nano'
    };
    
    // Return the mapped model or default if not found
    return modelMap[modelName] || modelMap.default;
 }

  async message(messages = [], options = {}) {
    try {
      // Use Puter's AI to generate a response
      const prompt = this.extractPromptFromMessages(messages);
      const model = options.model || this.model;
      
      if (options.stream !== false) {
        const aiResponse = await this.puter.ai.chat(prompt, { model: model, stream: true });
        
        let fullContent = "";
        for await (const part of aiResponse) {
          if (part?.text) {
            fullContent += part.text;
            if (this.onTokenStream) {
              this.onTokenStream(part.text);
            }
          }
        }
        return fullContent;
      } else {
        const aiResponse = await this.puter.ai.chat(prompt, { model: model });
        return aiResponse.message?.content || aiResponse;
      }
    } catch (error) {
      console.error("Error in PuterLLM.message:", error);
      throw error;
    }
  }

  async call(prompt, context, options = {}) {
    try {
      const model = options.model || this.model;
      const streaming = options.stream !== false; // Default to true if not explicitly false
      
      if (streaming) {
        // For streaming, return a promise that resolves to a readable stream
        return new Promise(async (resolve, reject) => {
          try {
            const aiResponse = await this.puter.ai.chat(prompt, { model: model, stream: true });
            
            // Create a readable stream from the async iterable
            const stream = new Readable({
              objectMode: false,
              read() {}
            });
            
            // Process the AI response and push to stream
            (async () => {
              try {
                for await (const part of aiResponse) {
                  if (part?.text) {
                    // Format as SSE data following OpenAI standard format
                    const sseData = {
                      id: `chatcmpl-${Date.now()}`,
                      object: "chat.completion.chunk",
                      created: Math.floor(Date.now() / 1000),
                      model: model,
                      choices: [{
                        index: 0,
                        delta: { content: part.text },
                        finish_reason: null
                      }]
                    };
                    const data = `data: ${JSON.stringify(sseData)}\n\n`;
                    stream.push(data);
                  }
                }
                // Send final completion message
                const finalSseData = {
                  id: `chatcmpl-${Date.now()}`,
                  object: "chat.completion.chunk",
                  created: Math.floor(Date.now() / 1000),
                  model: model,
                  choices: [{
                    index: 0,
                    delta: {},
                    finish_reason: "stop"
                  }]
                };
                stream.push(`data: ${JSON.stringify(finalSseData)}\n\n`);
                // End the stream
                stream.push("data: [DONE]\n\n");
                stream.push(null); // EOF
              } catch (error) {
                stream.destroy(error);
                reject(error);
              }
            })();
            
            resolve({ data: stream });
          } catch (error) {
            reject(error);
          }
        });
      } else {
        const aiResponse = await this.puter.ai.chat(prompt, { model: model });
        return aiResponse;
      }
    } catch (error) {
      console.error("Error in PuterLLM.call:", error);
      throw error;
    }
  }

  extractPromptFromMessages(messages) {
    // Convert messages array to a single prompt string
    if (messages.length === 0) return '';
    
    // For now, return the content of the last message as the prompt
    // In a more sophisticated implementation, you might want to format the conversation properly
    const lastMessage = messages[messages.length - 1];
    return typeof lastMessage.content === 'string' ? lastMessage.content : JSON.stringify(lastMessage.content);
  }

  // Override the start method to handle Puter.js specific streaming
  async start(prompt, context = {}, options = {}) {
    // If context contains messages, use message() method; otherwise use call()
    if (context.messages && context.messages.length > 0) {
      return await this.message(context.messages, options);
    } else {
      // For direct prompt, call the parent start method which will call()
      const response = await this.call(prompt, context, options);
      // Handle SSE if needed
      if (this.responseType === TYPE_ENUM.SSE && response.data) {
        const content = await this.handleSSE(response);
        return content;
      }
      return "";
    }
  }

  // Override the completion method to ensure it works with Puter.js
  async completion(prompt, context = {}, options = {}) {
    // Use the message method for conversation-style prompts, or call for direct prompts
    if (context.messages && context.messages.length > 0) {
      // If there are messages in context, append the prompt as a new user message
      const messages = [...context.messages];
      if (prompt) {
        messages.push({ role: "user", content: prompt });
      }
      return await this.message(messages, options);
    } else {
      // For direct prompts without context messages, use the call method
      const content = await this.start(prompt, context, options);
      return content;
    }
  }
}

module.exports = exports = PuterLLM;