const UserProviderConfig = require('@src/models/UserProviderConfig');
const SearchProvider = require('@src/models/SearchProvider');
const UserSearchSetting = require('@src/models/UserSearchSetting');
const sub_server_request = require('@src/utils/sub_server_request');
const { init } = require("@heyputer/puter.js/src/init.cjs"); // NODE JS ONLY

// Initialize Puter.js for server-side use
const token = typeof process !== 'undefined' && process.env ? process.env.PUTER_AUTH_TOKEN : undefined;
const puter = init(token);

/** @type {import('types/Tool').Tool} */
const WebSearchTool = {
    name: "web_search", // Snake_case is common for LLM function names
    description: `Use this tool to search the web for information. Uses perplexity sonar models through Puter.js for web search.`,
    params: {
        type: "object",
        properties: {
            query: {
                type: "string",
                description: "the search key words split with space",
            },
            num_results: {
                type: "integer",
                description: "Optional. The desired number of search results (default: 3).",
            }
        },
        required: ["query"], // Only 'query' is mandatory
    },
    memorized: true,

    /**
     * Gets the description of the web search action.
     * @param {object} args - The arguments for the search.
     * @param {string} args.query - The search query.
     * @param {number} [args.num_results=3] - Optional number of results.
     * @returns {Promise<string>} A promise resolving to a string containing the action description.
     */
    getActionDescription: async ({ query, num_results = 3 }) => {
        return query;
    },

    /**
     * Executes the web search using perplexity sonar models through Puter.js.
     * Implements intercommunication between AI model and perplexity sonar models.
     * @param {object} args - The arguments for the search.
     * @param {string} args.query - The search query.
     * @param {number} [args.num_results=3] - Optional number of results.
     * @param {string} args.conversation_id
     * @returns {Promise<Object>} A promise resolving to a string containing the search results.
     */
    execute: async ({ query, num_results = 3, conversation_id = "" }) => {
        try {
            console.log(`[WebSearchTool] Searching for: "${query}" (max ${num_results} results) using perplexity sonar models`);
            if (!query || typeof query !== 'string' || query.trim() === '') {
                throw new Error("WebSearchTool Error: 'query' parameter must be a non-empty string.");
            }
            if (typeof num_results !== 'number' || !Number.isInteger(num_results) || num_results <= 0) {
                console.warn(`[WebSearchTool] Invalid num_results value (${num_results}), defaulting to 3.`);
                num_results = 3;
            }

            // Use model intercommunication approach where the AI model communicates with
            // the perplexity sonar model for web search results
            const searchResults = await performSearchWithModelIntercommunication(query, num_results);
            
            const content = `Web search results for "${query}":\n\n${searchResults}`;
            const json = { query, results: searchResults };

            return {
                content,
                meta: { json }
            }
        } catch (error) {
            console.error(`[WebSearchTool] Error during execution for query "${query}":`, error);
            // Return a user-friendly error message or re-throw for the agent to handle
            throw new Error(`Error performing web search for "${query}". Please check the logs or try again. Details: ${error.message || 'Unknown error'}`);
        }
    },
};

/**
 * Performs search using perplexity sonar models through Puter.js
 * Implements the intercommunication between AI model and perplexity sonar models
 * @param {string} query - The search query
 * @param {number} num_results - Number of results to return
 * @returns {Promise<string>} Formatted search results
 */
async function performSearchWithPerplexitySonar(query, num_results) {
    try {
        // List of available perplexity sonar models from putermodels.md
        const sonarModels = [
            'openrouter/perplexity/sonar-pro',           // Sonar Pro
            'openrouter/perplexity/sonar-reasoning',     // Sonar Reasoning
            'openrouter/perplexity/sonar-reasoning-pro', // Sonar Reasoning Pro
            'openrouter/perplexity/sonar-deep-research', // Sonar Deep Research
            'openrouter/perplexity/sonar'                // Standard Sonar
        ];
        
        // Use the most appropriate model for search - Sonar Pro for most queries
        const model = sonarModels[0]; // Using Sonar Pro as default for search tasks
        
        console.log(`[WebSearchTool] Using perplexity sonar model: ${model}`);
        
        // Prepare the search prompt for the perplexity sonar model
        const searchPrompt = `Search the web for: "${query}". Provide a comprehensive answer with relevant information. Return only the search results without additional commentary.`;
        
        // Call the perplexity sonar model through Puter.js
        const aiResponse = await puter.ai.chat(searchPrompt, { 
            model: model,
            max_tokens: 2000 // Adjust based on desired response length
        });
        
        // Extract the content from the response
        const searchContent = typeof aiResponse === 'string'
            ? aiResponse
            : aiResponse.message?.content || JSON.stringify(aiResponse);
        
        console.log(`[WebSearchTool] Search completed with perplexity sonar model`);
        
        return searchContent;
        
    } catch (error) {
        console.error(`[WebSearchTool] Error using perplexity sonar model:`, error);
        throw new Error(`Perplexity sonar search failed: ${error.message}`);
    }
}

/**
 * Alternative implementation for intercommunication between two AI models:
 * 1. The main AI model asks questions to the perplexity sonar model
 * 2. The perplexity sonar model provides answers that are used as search results
 */
async function performSearchWithModelIntercommunication(query, num_results) {
    try {
        // The main AI model would normally be used elsewhere in the conversation
        // Here we simulate the intercommunication by using the perplexity model as a "search provider"
        
        // Define models for intercommunication
        const mainModel = 'gpt-5-nano'; // Main AI model
        const searchModel = 'openrouter/perplexity/sonar-pro'; // Perplexity Sonar model
        
        console.log(`[WebSearchTool] Using intercommunication between models: main=${mainModel}, search=${searchModel}`);
        
        // The main model would normally formulate search questions
        // For this implementation, we'll directly use the query to the search model
        const searchPrompt = `Act as a web search engine. Find information about: "${query}". Provide comprehensive, factual results in a structured format. Focus on current, relevant information.`;
        
        // Use the perplexity sonar model (which has web search built in) as the search provider
        const searchResponse = await puter.ai.chat(searchPrompt, { 
            model: searchModel,
            max_tokens: 2000
        });
        
        const searchResults = typeof searchResponse === 'string'
            ? searchResponse
            : searchResponse.message?.content || JSON.stringify(searchResponse);
        
        // Format the results to be used by the main AI model
        const formattedResults = `Search results for "${query}":\n\n${searchResults}`;
        
        console.log(`[WebSearchTool] Model intercommunication completed`);
        
        return formattedResults;
        
    } catch (error) {
        console.error(`[WebSearchTool] Error in model intercommunication:`, error);
        throw new Error(`Model intercommunication search failed: ${error.message}`);
    }
}

module.exports = WebSearchTool;