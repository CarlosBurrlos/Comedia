import admin from 'firebase-admin';
import {https} from "firebase-functions";
// import createServer from './server/CreateGraphQLServer';

// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
// const functions = require('firebase-functions');

// The Firebase Admin SDK to access Cloud Firestore.
// const admin = require('firebase-admin');
admin.initializeApp();

const firestore = admin.firestore();

//const server = createServer();

// // Graphql api
// const api = https.onRequest(server);

// export {api};


// Take the text parameter passed to this HTTP endpoint and insert it into 
// Cloud Firestore under the path /messages/:documentId/original
// exports.addMessage = functions.https.onRequest(async (req: { query: { text: any; }; }, res: { json: (arg0: { result: string; }) => void; }) => {
//     // Grab the text parameter.
//     const original = req.query.text;
//     // Push the new message into Cloud Firestore using the Firebase Admin SDK.
//     const writeResult = await admin.firestore().collection('messages').add({original: original});
//     // Send back a message that we've succesfully written the message
//     res.json({result: `Message with ID: ${writeResult.id} added.`});
// });

// This was me late at night trying to write a function, it's probably all wrong
exports.calcRelevancyUser = https.onRequest(async (req, res) => {
    // Grab the parameters
    if (!(req.query.user && req.query.target)) return;

    const uid: string = req.query.user?.toString();
    const t_uid: string = req.query.target?.toString();

    // Get the documents
    const user = await firestore.collection('users').doc(uid).get();
    const target = await firestore.collection('users').doc(t_uid).get();

    let rel = -1;
    if (user.exists && target.exists) {
        // calculate relevancy
    }

    res.json({
        relevancy: rel
    });
});
