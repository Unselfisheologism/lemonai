const PuterLLM = require('./llm.puter');

const map = {
  'puter': PuterLLM,
  // Keep other providers as fallbacks for now, but in production you might want to remove them
  'azure': () => { throw new Error("Azure provider has been replaced with Puter.js"); },
  'openai': () => { throw new Error("OpenAI provider has been replaced with Puter.js"); },
  'glm3': () => { throw new Error("GLM3 provider has been replaced with Puter.js"); },
  'qwen': () => { throw new Error("Qwen provider has been replaced with Puter.js"); },
  'qwen.ali': () => { throw new Error("Qwen Ali provider has been replaced with Puter.js"); },
  'ollama': () => { throw new Error("Ollama provider has been replaced with Puter.js"); },
}

const createLLMInstance = (type, onTokenStream) => {
  // console.log('createLLMInstance.type', type)
  const LLM = map[type];
  // console.log('createLLMInstance.LLM', LLM);
  const llm = new LLM(onTokenStream);
  console.log(type, 'llm', llm);
  return llm;
}

module.exports = exports = createLLMInstance