import {https} from 'firebase-functions';
import createServer from './server/CreateGraphQLServer';

const serverPromise = createServer();

// Graphql api
let api;
serverPromise.then((server) => {
    api = https.onRequest(server);
});

export {api};
