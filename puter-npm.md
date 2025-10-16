@heyputer/puter.js
TypeScript icon, indicating that this package has built-in type declarations
2.0.15 • Public • Published 10 hours ago
Puter.js
The official JavaScript SDK for Puter.com

Free, Serverless, Cloud and AI from the frontend code.

Learn More · Docs · Tutorials · Examples · X


Installation
NPM:
npm install @heyputer/puter.js
CDN:
Include Puter.js directly in your HTML via CDN in the <head> section:

<script src="https://js.puter.com/v2/"></script>

Usage
Browser
ES Modules
import {puter} from '@heyputer/puter.js';
// or
import puter from '@heyputer/puter.js';
// or 
import '@heyputer/puter.js'; // puter will be available globally
CommonJS
const {puter} = require('@heyputer/puter.js');
// or
const puter = require('@heyputer/puter.js');
// or
require('@heyputer/puter.js'); // puter will be available globally
Node.js (with Auth Token)
const {init} = require("@heyputer/puter.js/src/init.cjs"); // NODE JS ONLY
// or
import {init} from "@heyputer/puter.js/src/init.cjs";

const puter = init(process.env.puterAuthToken); // uses your auth token
const puter2 = init(process.env.puterAuthToken2); // use some other auth token

Usage Example
After importing, you can use the global puter object:

// Print a message
puter.print('Hello from Puter.js!');

// Chat with GPT-5 nano
puter.ai.chat('What color was Napoleon\'s white horse?').then(response => {
  puter.print(response);
});

Setting Custom Origins
By default puter.js uses the official Puter API and GUI origins. You can customize these origins by setting global variables before importing the SDK like so:

// For API origin
globalThis.PUTER_API_ORIGIN = 'https://custom-api.puter.com';
// For GUI origin
globalThis.PUTER_ORIGIN = 'https://custom-gui.puter.com';

import {puter} from '@heyputer/puter.js'; // or however you import it for your env





when i asked a puter.js developer about my doubts (you could also have these doubts) : 

My Doubt: what is the "node.js (with auth token)" example (mentioned above) about and what is it for? also, i thought that puter.js handles the authentication entirely by itself. so why is there a "puterAuthToken" mentioned in this node.js example


Answer: The "Node.js (with Auth Token)" example is for developers who want to use a specific authentication token to initialize the Puter.js SDK, instead of relying on the standard, automated authentication handled by the library. This allows an application to use a specific, pre-obtained puterAuthToken to access Puter's services. It is for advanced use cases where a developer might want to manage different authentication tokens, perhaps for different users or with varying permissions, as shown in the example with puterAuthToken and puterAuthToken2. 
While Puter.js handles authentication automatically for most use cases, particularly for frontend applications running in a browser, the use of an explicit authentication token as shown in the Node.js example provides more manual control. The standard process for browser-based applications is that Puter.js will automatically prompt a user to sign in to their Puter.com account when an app attempts to access a cloud service. This is intended to be a seamless process where the developer does not need to handle API keys or credentials. The puterAuthToken is mentioned specifically for a Node.js environment, suggesting a server-side use case where direct token management may be necessary. 


My Follow-Up Doubt: so for a general ai agent mobile app, is this needed or not?


Answer: Using an authentication token with the Node.js example is generally not necessary for a typical mobile app using Puter.js. Puter.js is designed to handle authentication automatically for frontend applications, including those used on mobile devices. When your mobile app needs to access a cloud service, Puter.js will automatically prompt the user to sign in to their Puter.com account if they aren't already authenticated. 
This automatic, user-centric authentication process is a key feature of Puter.js and is intended to simplify development by removing the need for a developer to manage API keys or tokens. It ensures that each user's data is isolated and they cover their own usage costs. 
The "Node.js (with Auth Token)" example is more relevant for advanced use cases where an application needs to manage different authentication tokens manually, such as in a server-side environment. For a general AI agent mobile app, a developer can simply rely on the built-in authentication methods provided by the Puter.js library. These methods include puter.auth.signIn(), puter.auth.isSignedIn(), and puter.auth.signOut() for manually controlling the authentication flow if desired. 