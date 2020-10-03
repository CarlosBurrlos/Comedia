import {https} from 'firebase-functions';
import createServer from './server/CreateGraphQLServer';

const serverPromise = createServer();

// Graphql api
let api;

serverPromise.then((server) => {
    api = https.onRequest(server);
}).catch(() => {
    console.log("Failed to start server.")
})

export {api};
