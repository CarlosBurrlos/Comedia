import {https} from 'firebase-functions';
import createServer from './server/CreateGraphQLServer';

const server = createServer();

// Graphql api
const api = https.onRequest(server);

export {api};
