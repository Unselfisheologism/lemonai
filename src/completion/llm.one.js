const CHANNEL = {
  OFFICIAL: 'official', // 官方部署-代理大模型服务
  PROVIDER: 'provider', // 模型服务商
  PRIVATE: 'private' // 私有化部署
}

const PuterLLM = require('./llm.puter');

const resolveKey = (model) => {
  if (model && model.startsWith('doubao')) {
    const envKey = 'ALIAS_' + model.replaceAll('-', '_').toUpperCase();
    console.log('envKey', envKey);
    return process.env[envKey] || model
  }
  return model
}

/**
 * 创建 LLM 调用实例
 * @param {*} config
 * @param {*} onTokenStream
 * @returns
 */
const createLLMInstance = async (config, onTokenStream, options = {}) => {

  if (typeof config === 'string') {
    const [channel, service, model] = config.split('#')
    config = {
      channel,
      service,
      model: resolveKey(model)
    }
  }
  console.log('config', config);
 const { channel, service, model } = config;

  // Prepare model_info to pass to PuterLLM
  const model_info = {
    model_name: model
  };

  // Include model_info in options so PuterLLM can access it
  const puterOptions = {
    ...options,
    model_info: model_info
  };

  // Use Puter.js as the underlying router for ALL AI models
  // This ensures all AI access is routed through puter.ai.chat() regardless of the service
  const llm = new PuterLLM(onTokenStream, model, puterOptions);
  return llm;
}

module.exports = exports = createLLMInstance