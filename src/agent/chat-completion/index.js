require("module-alias/register");
require("dotenv").config();

const createLLMInstance = require("@src/completion/llm.one.js"); // Use unified router
const { getDefaultModel } = require('@src/utils/default_model')
const resolveAutoReplyPrompt = require('@src/agent/prompt/auto_reply.js');
const sub_server_request = require('@src/utils/sub_server_request')
const conversation_token_usage = require('@src/utils/get_sub_server_token_usage')

const chat_completion = async (question, options, conversation_id, onTokenStream) => {
  let model_info = await getDefaultModel(conversation_id)
  if (model_info.is_subscribe) {
    // For subscription models, use the unified PuterLLM router
    return chat_completion_puter(question, options, conversation_id, onTokenStream)
  }
  return chat_completion_local(question, options, conversation_id, onTokenStream)
}

const chat_completion_puter = async (question, options, conversation_id, onTokenStream) => {
  try {
    // Use the unified router to ensure all AI access goes through PuterLLM
    const model = options.model || 'gpt-5-nano';
    
    // Create a PuterLLM instance through the unified router
    const llm = await createLLMInstance({
      channel: 'provider',
      service: 'puter',
      model: model
    }, onTokenStream, options);
    
    // Use the completion method which will route through puter.ai.chat()
    const content = await llm.completion(question, {}, options);
    return content;
  } catch (error) {
    console.error("Error in Puter.js chat completion:", error);
    throw error;
  }
};

const chat_completion_local = async (question, options, conversation_id, onTokenStream) => {
  // Call the model to get a response in English based on the goal
  return createLLMInstance('official#provider#default', onTokenStream, options)
    .then(llm => llm.completion(question, {}, options));
}

module.exports = exports = chat_completion;
