this repo was originally a general ai agent ( lemonai ) desktop software. the ai agent can do many things like run commands in a shell, generate ai images, write and run html, etc.  it used to plan before it executes or acts. meaning, it used to have a planning phase, to-do list, etc. made before it executed any command or request given to it by the user. it depended on 2 docker containers; one for the actual app and one for the sandbox meant for the code execution for running commands (2nd feature mentioned above). for the AI features, it used API KEYs from external AI API providers and routers like deepseek, openai, doubao, etc. it used web search api keys for providing the ai agent with the ability to search the web. it also has a headless browser functionality for browser automation and web scraping. 


my goal was to turn this into a mobile app. since there are significant architectural mismatches between a desktop and a mobile app, i had to make many changes. i also wanted to avoid spending any money for this. so this is what i did (or at least planned to do) :

1. un-containerize the entire app and make it docker-free and independent of containers, as containers and docker just dont work on mobile devices. 

2. create a mobile UI for the entire app (for now, just android). it will use webview for previewing webpages. this is needed. believe me. then for making the user have a browser-like experience, through tabs, etc. all of this is mentioned in the @/UI-description.md file. please go through this file as well. go through it very thoroughly. 

3. for the AI features, modify the usage of ai api providers and their api keys and enpoint urls , to shift them to use puter.js (more information about puter.js is mentioned in these files: @/puterdocs.md , @/puter-npm.md and @/puter-related.md ). this is for all the ai features in this project. even for the ai features of text to image generation. 

4. replace the web search APIs (tavily, cloudsway, local search, baidu, bing, etc.) with a feature where the perplexity sonar models provided by puter.js (these models have web search built into them) communicate with the AI model which the user is chatting with. like an intercommunication between two ai models (**IN NATURAL LANGUAGE ONLY**) . the AI model being used in the chat communicates with the perplexity sonar model and asks it specific questions. answers to these questions (given by the perplexity sonar models) will then be considered and used (by the ai agent being used in the chat) as the web search results. 

5. to be able to give the ai agent the power to create, read, update, delete, schedule and/or execute workflows. for this, i had to build a workflow builder. for this, i planned to implement a optimized-for-mobile-ui version of n8n ( please search on the web using the web-search mcp, for more information about n8n ) . please read the @/answers.md file.

6. hide the entire UI of n8n ( this could be possible maybe because n8n can also be installed as an npm package, as i told earlier. yes, the entire n8n software, which is only 7MB as the unpacked npm package. ) so that the AI agent is the only one who controls and manages the all the workflows. 

7. for connecting to external tools like gmail, notion, etc. i will use the session cookies from the browser session of the user from the webview, and then i will pass these OAuth credentials from the session cookies to the tool's specific integration node inside of n8n, like the gmail node, notion node, etc. 

8. optionally ( in the settings of the app ) , provide the users the option to control the workflows manually. for technical users who know to do so. but even in this method, the external tools will be pre-connected as mentioned in point 8, the above point. **for now, this is the least priority task for me.**

9. allow the ai agent to use the UI of the phone of the user by touching UI elements, by being able to view the phone screen, etc. for this , i thought of modifying blurr, which is a google-assistant-like, home-button-based, popup-based AI assistant that can use the phone and thereby automate tasks and schedule commands as per the user's command. blurr is only for android. ( i plan to keep this entire mobile app project android-only, for now. ) the entire codebase of blurr is located @/blurr-repo.md . 

