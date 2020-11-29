import admin, { firestore } from 'firebase-admin';
import {https} from "firebase-functions";
// import createServer from './server/CreateGraphQLServer';

// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
// const functions = require('firebase-functions');

// The Firebase Admin SDK to access Cloud Firestore.
// const admin = require('firebase-admin');
admin.initializeApp();

const firestoreInstance = admin.firestore();

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

const resolvers: {[index: string]: (req: https.Request) => Promise<object>} = {
    '/relevantUsers': relevantUsers,
    '/relevantPosts': relevantPosts,
};

exports.calcRelevancyUser = https.onRequest(async (req, res) => {
    const response = await resolvers[req.path](req);
    res.json(response);
});

interface UserRelevancyPair {
    doc_snap: firestore.DocumentSnapshot,
    doc_rel: number
}

async function relevantUsers(req: https.Request): Promise<object> {
    // Get query parameters
    if (!(req.query.user && req.query.mode)) return {};
    const uid: string = req.query.user?.toString();
    const mode: string = req.query.mode?.toString();

    // Get the user and the set of target users to use
    const user = await firestoreInstance.collection('users').doc(uid).get();
    let targets: firestore.DocumentReference[];
    if (mode === "followers") {
        targets = user.get("followers");
    } else if (mode === "following") {
        targets = user.get("usersFollowing");
    } else {
        return {};
    }

    // Create the list of objects
    let UserRelevancyPairs: UserRelevancyPair[] = await Promise.all(targets.map(
        async (target: firestore.DocumentReference) => {
            let target_snap = await target.get();
            let new_pair: UserRelevancyPair = {
                doc_snap: target_snap,
                doc_rel: await calculateUserRelevancy(user,target_snap)
            };
            return new_pair;
        }
    ));

    // Sort by relevancy and return the document snapshots
    UserRelevancyPairs.sort((a,b) => a.doc_rel - b.doc_rel);
    return UserRelevancyPairs.map(pair => pair.doc_snap);
}

function commonLength(array1: any[], array2: any[]): number {
    return array1.filter(value => array2.includes(value)).length;
}

async function calculateUserRelevancy(user: firestore.DocumentSnapshot, target: firestore.DocumentSnapshot): Promise<number> {
    // Default relevancy is zero
    let rel = 0;
    if (user.exists && target.exists) {
        // calculate relevancy

        let rel_mult = 1;
        if (user.get("usersFollowing").contains(target.ref)) {
            rel_mult *= 1.5;
        }
        if (user.get("followers").contains(target.ref)) {
            rel_mult *= 1.5;
        }

        let targetPosts: firestore.DocumentReference[] = target.get("createdPosts");

        // The number of times the user has upvoted the target's posts
        rel += commonLength(user.get("upvotedPosts"), targetPosts);
        
        // The number of times the user has downvoted the target's posts
        rel -= commonLength(user.get("downvotedPosts"), targetPosts);

        // The number of times the user has commented on the target's posts
        let comment_list: firestore.DocumentReference[] = user.get("comments");
        let post_list = comment_list.map(async x => await x.get().then(y => y.get("parent")));
        rel += 2 * commonLength(post_list, targetPosts);

        if (rel > 0) {
            rel *= rel_mult;
        }
    }
    return rel;
}

async function relevantPosts(req: https.Request): Promise<object> {
    // TODO: Return posts sorted by relevance
    return {};
}
