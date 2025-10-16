// @ts-ignore
const router = require("koa-router")();
const { PassThrough } = require("stream");

const calcToken = require('@src/completion/calc.token.js');
const puterAuthService = require('@src/services/puterAuthService');

// Initialize Puter.js service for backend usage
let puter = null;

// Initialize the PuterAuthService
puterAuthService.init().then(service => {
  puter = service.puter;
}).catch(error => {
  console.error('Failed to initialize PuterAuthService:', error);
});

// Ensure puter is initialized before processing requests
const ensurePuterInitialized = async () => {
  if (!puter) {
    // Wait a bit to see if initialization completes
    await new Promise(resolve => setTimeout(resolve, 1000));
    if (!puter) {
      throw new Error("Puter.js SDK not initialized. Please ensure Puter.js is properly configured.");
    }
  }
  return puter;
};

router.post("/v1/chat/completions", async (ctx, next) => {
  const { request, response, state } = ctx;
  const body = request.body || {};
  const conversation_id = body.conversation_id

  // 根据客户端请求判断是否需要流式响应
  const isStream = body.stream === true;

  // Koa 的 response body 可以直接是 stream
  const clientResponseStream = new PassThrough();
  ctx.body = clientResponseStream;
  ctx.status = 200;

  try {
    // Extract messages from request body
    const messages = body.messages || [];
    if (messages.length === 0) {
      ctx.status = 400;
      ctx.body = {
        error: {
          message: "No messages provided",
          type: "invalid_request_error",
          code: "messages_missing",
        },
      };
      return;
    }

    // Get the last message as the prompt (standard practice)
    const lastMessage = messages[messages.length - 1];
    const prompt = lastMessage.content;
    const model = body.model || 'gpt-5-nano'; // Default to a model available in Puter

    // Use Puter's AI to generate a response
    const currentPuter = await ensurePuterInitialized();
    if (isStream) {
      // Handle streaming response
      const aiResponse = await currentPuter.ai.chat(prompt, { model: model, stream: true });
      
      let fullContent = "";
      let choiceIndex = 0;
      
      // Calculate input tokens
      const input_tokens = calcToken('', messages);
      
      for await (const part of aiResponse) {
        if (part?.text) {
          fullContent += part.text;
          
          // Send SSE data
          const sseData = {
            id: `chatcmpl-${Date.now()}-${choiceIndex}`,
            object: "chat.completion.chunk",
            created: Math.floor(Date.now() / 1000),
            model: model,
            choices: [{
              index: choiceIndex,
              delta: {
                content: part.text
              },
              finish_reason: null
            }]
          };
          
          clientResponseStream.write(`data: ${JSON.stringify(sseData)}\n\n`);
          choiceIndex++;
        }
      }
      
      // Send final completion message
      const finalSseData = {
        id: `chatcmpl-${Date.now()}-${choiceIndex}`,
        object: "chat.completion.chunk",
        created: Math.floor(Date.now() / 1000),
        model: model,
        choices: [{
          index: choiceIndex,
          delta: {},
          finish_reason: "stop"
        }]
      };
      
      clientResponseStream.write(`data: ${JSON.stringify(finalSseData)}\n\n`);
      clientResponseStream.write(`data: [DONE]\n\n`);
      
      // Calculate output tokens and log
      const output_tokens = calcToken(fullContent);
      console.log("===input_tokens, output_tokens======", input_tokens, output_tokens);
      
      clientResponseStream.end();
    } else {
      // Handle non-streaming response
      const aiResponse = await currentPuter.ai.chat(prompt, { model: model });
      
      // Calculate tokens
      const input_tokens = calcToken('', messages);
      const output_tokens = calcToken(aiResponse.message.content);
      
      console.log("===input_tokens, output_tokens======nostream:", input_tokens, output_tokens);
      
      // Format response in OpenAI-compatible format
      ctx.body = {
        id: `chatcmpl-${Date.now()}`,
        object: "chat.completion",
        created: Math.floor(Date.now() / 1000),
        model: model,
        choices: [{
          index: 0,
          message: {
            role: "assistant",
            content: aiResponse.message.content
          },
          finish_reason: "stop"
        }],
        usage: {
          prompt_tokens: input_tokens,
          completion_tokens: output_tokens,
          total_tokens: input_tokens + output_tokens
        }
      };
      ctx.status = 200;
    }
  } catch (error) {
    console.error("Error during Puter.js AI proxy:", error);
    
    // Error handling
    ctx.status = 500;
    ctx.body = {
      error: {
        message: error.message || "An unknown error occurred during proxying to Puter AI.",
        type: "proxy_server_error",
        code: null,
      },
    };
    
    // If stream already opened, ensure it's closed
    if (isStream && !clientResponseStream.writableEnded) {
      clientResponseStream.end();
    }
  }
});

module.exports = exports = router.routes();