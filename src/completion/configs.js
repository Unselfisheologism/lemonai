const env = process.env || {}

// Only include Puter.js configuration since we're replacing all other AI providers with Puter.js
const configs = [
  {
    channel: 'provider',
    service: 'puter',
    name: 'Puter AI',
    // Puter.js doesn't use traditional API endpoints since it's an SDK-based integration
    config: {
      // Puter.js handles authentication automatically, no API key needed
    },
    models: [
      'gpt-5-nano',
      'gpt-5-mini',
      'claude-sonnet-4',
      'gemini-2.0-flash'
    ]
  }
]

const fs = require('fs')
const llmJsonPath = env.LLM_JSON_PATH
if (llmJsonPath) {
  const llmJson = fs.readFileSync(llmJsonPath, 'utf-8')
  const list = JSON.parse(llmJson)
  for (const item of list) {
    configs.push(item)
  }
}

module.exports = exports = configs;