const BaseLLM = require('./llm.base')

class ConfigLLM extends BaseLLM {

  constructor(config = {}, onTokenStream) {
    super(onTokenStream)
    const { url, model, splitter = '\n\n' } = config;
    this.splitter = splitter;
    this.CHAT_COMPLETION_URL = url;
    this.model = model;
  }
}

module.exports = exports = ConfigLLM;