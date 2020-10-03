import express from 'express';
import {ApolloServer} from 'apollo-server-express';

import schema from './Schema';

function createGraphQLServer() {
    const app = express();

    const apolloServer = new ApolloServer({
        schema: schema,
        // Enable graphiql gui
        introspection: true,
        playground: true
    });

    apolloServer.applyMiddleware({app, path: '/', cors: true});

    return app;
}

export default createGraphQLServer;
