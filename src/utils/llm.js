const { getDefaultModel } = require('@src/utils/default_model')

const createLLMInstance = require("@src/completion/llm.one.js");
const parseJSON = require("./json.js");
const { PauseRequiredError } = require("@src/utils/errors");

const calcToken = require('@src/completion/calc.token.js')
const Conversation = require('@src/models/Conversation.js')


const defaultOnTokenStream = (ch) => {
  process.stdout.write(ch);
}

const DEFAULT_MODEL_TYPE = "assistant";

const LLM_LOGS = require('@src/models/LLMLogs.js');

/**
 * @param {*} prompt 
 * @param {*} model_type 
 * @param {*} options 
 * @param {*} onTokenStream 
 * @returns {Promise<Object>}
 */
const call = async (prompt, conversation_id, model_type = DEFAULT_MODEL_TYPE, options = { temperature: 0 }, onTokenStream = defaultOnTokenStream) => {
  const model_info = await getDefaultModel(conversation_id)
  const model = `provider#${model_info.platform_name}#${model_info.model_name}`;
  const llm = await createLLMInstance(model, onTokenStream, { model_info });
  
  const { response_format, messages = [], ...restOptions } = options;
  const context = { messages };

  // All model-specific logic is now handled in PuterLLM class
  // Puter.js will handle model-specific configurations internally
  // Model mapping and special handling is done in the PuterLLM.mapModelToPuter() method

  const content = await llm.completion(prompt, context, restOptions);

  // 处理 ERR_BAD_REQUEST 错误
  if (typeof content === 'string' && content.startsWith('ERR_BAD_REQUEST')) {
    throw new PauseRequiredError("LLM Call Failed");
  }

  const inputPrompt = messages.map(item => item.content).join('\n') + '\n' + prompt;
  const input_tokens = calcToken(inputPrompt)
  const output_tokens = calcToken(content)
  if (conversation_id) {
    const conversation = await Conversation.findOne({ where: { conversation_id: conversation_id } })
    if (conversation) {
      // @ts-ignore
      conversation.input_tokens = conversation.input_tokens + input_tokens
      // @ts-ignore
      conversation.output_tokens = conversation.output_tokens + output_tokens
      await conversation.save()
    }
  }

  if (response_format === 'json') {
    const json = parseJSON(content);
    // @ts-ignore
    await LLM_LOGS.create({ model, prompt, messages, content, json, conversation_id });
    return json;
  }
  // @ts-ignore
  await LLM_LOGS.create({ model, prompt, messages, content, conversation_id });
  //return content
  return content;
}

module.exports = exports = call;
