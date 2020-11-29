import admin from 'firebase-admin';
import {https} from "firebase-functions";
import DocumentReference = admin.firestore.DocumentReference;
import Timestamp = admin.firestore.Timestamp;
import DocumentSnapshot = admin.firestore.DocumentSnapshot;
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

const resolvers: { [index: string]: (req: https.Request) => Promise<object> } = {
    '/relevantUsers': relevantUsers,
    '/relevantPosts': relevantPosts
};

exports.calcRelevancy = https.onRequest(async (req, res) => {
    const response = await resolvers[req.path](req);
    res.json(response);
});

interface UserRelevancyPair {
    doc_snap: DocumentSnapshot,
    doc_rel: number
}

async function relevantUsers(req: https.Request): Promise<object> {
    // Get query parameters
    if (!(req.query.user && req.query.mode)) return {};
    const uid: string = req.query.user?.toString();
    const mode: string = req.query.mode?.toString();

    // Get the user and the set of target users to use
    const user = await firestore.collection('users').doc(uid).get();
    let targets: DocumentReference[];
    if (mode === "followers") {
        targets = user.get("followers");
    } else if (mode === "following") {
        targets = user.get("usersFollowing");
    } else {
        return {};
    }

    // Create the list of objects
    let UserRelevancyPairs: UserRelevancyPair[] = await Promise.all(targets.map(
        async (target: DocumentReference) => {
            let target_snap = await target.get();
            let new_pair: UserRelevancyPair = {
                doc_snap: target_snap,
                doc_rel: await calculateUserRelevancy(user, target_snap)
            };
            return new_pair;
        }
    ));

    // Sort by relevancy and return the document snapshots
    UserRelevancyPairs.sort((a,b) => b.doc_rel - a.doc_rel);
    //logRelevancy(UserRelevancyPairs);
    return UserRelevancyPairs.map(pair => pair.doc_snap.ref.path);
}

function commonLength(array1: DocumentReference[], array2: DocumentReference[]): number {
    let num = 0;
    // Algorithm won't need to be very efficient
    array1.forEach(doc1 => {
        array2.forEach(doc2 => {
            if (doc1.id == doc2.id) {
                num++;
            }
        })
    })
    return num;
    //return array1.filter(value => array2.includes(value)).length;
}

// function logRelevancy(array: UserRelevancyPair[]): void {
//     array.forEach(pair => {
//         console.log(`${pair.doc_snap.get("username")}: ${pair.doc_rel}\n`);
//     });
// }

async function calculateUserRelevancy(user: DocumentSnapshot, target: DocumentSnapshot): Promise<number> {
    // Default relevancy is zero
    let rel = 0;
    if (user.exists && target.exists) {
        // calculate relevancy

        let rel_mult = 1;
        let user_list: DocumentReference[] = user.get("usersFollowing");
        if (user_list.map(u => (u.id == target.id)).includes(true)) {
            rel_mult *= 1.5;
        }
        user_list = user.get("followers");
        if (user_list.map(u => (u.id == target.id)).includes(true)) {
            rel_mult *= 1.5;
        }

        let targetPosts: DocumentReference[] = target.get("createdPosts");

        // The number of times the user has upvoted the target's posts
        rel += commonLength(user.get("upvotedPosts"), targetPosts);

        // The number of times the user has downvoted the target's posts
        rel -= commonLength(user.get("downvotedPosts"), targetPosts);

        // The number of times the user has downvoted the target's posts
        rel += 8 * commonLength(user.get("savedPosts"), targetPosts);

        // The number of times the user has commented on the target's posts
        let comment_list: DocumentReference[] = user.get("comments");
        let post_list: DocumentReference[] = await Promise.all(comment_list.map(async x => {
            let snap: DocumentSnapshot = await x.get();
            return snap.get("parent");
        }));
        rel += 4 * commonLength(post_list, targetPosts);

        if (rel > 0) {
            rel *= rel_mult;
        }
    }
    return rel;
}

interface UserModel {
    comments: DocumentReference[];
    createdPosts: DocumentReference[];
    downvotedPosts: DocumentReference[];
    email: string;
    followers: DocumentReference[];
    genresFollowing: string[];
    profile: DocumentReference;
    savedPosts: DocumentReference[];
    username: string;
    usersFollowing: DocumentReference[];
}

interface GenreModel {
    posts: DocumentReference[];
}

class RelevantPostResponse {
    posts: PostModel[] = [];
}

type VariableDepthArray<T> = T | VariableDepthArray<T>[];

function flatten<T>(arr: VariableDepthArray<T>[], result: T[] = []): T[] {
    for (let i = 0, length = arr.length; i < length; i++) {
        const value = arr[i];
        if (Array.isArray(value)) {
            flatten(value, result);
        } else {
            result.push(value);
        }
    }
    return result;
}

async function getUserPostCandidates(user: UserModel): Promise<DocumentReference[]> {
    const followedUsers = Promise.all(user.usersFollowing.map(async user => {
        const followedUser = (await user.get()).data() as UserModel;
        return followedUser.createdPosts;
    }));
    const followedGenres = Promise.all(user.genresFollowing.map(async genre => {
        const followedGenre = await firestore.collection('genres').doc(genre).get();
        const followedGenreData = followedGenre.data() as GenreModel;
        return followedGenreData.posts;
    }));
    const unprocessedCandidates = await Promise.all([followedUsers, followedGenres]);
    const mergedUnprocessedCandidates = flatten(unprocessedCandidates);
    return mergedUnprocessedCandidates.filter((item, index) => {
        return index === mergedUnprocessedCandidates.indexOf(item);
    });
}

interface PostModel {
    anon: boolean;
    comments: DocumentReference[];
    content: string;
    created: Timestamp;
    downvoteCount: number;
    downvoteList: DocumentReference[];
    genre: string;
    poster: DocumentReference;
    title: string;
    upvoteCount: number;
    upvoteList: DocumentReference[];
}

interface CommentModel {
    poster: DocumentReference;
    parent: DocumentReference;
    content: string;
}

async function postRelevancy(postReference: DocumentReference, userReference: DocumentReference, user: UserModel): Promise<number> {
    // upvotes - downvotes + 2 * comments
    const postData = (await postReference.get()).data();
    if (!postData) return -1;
    const post = postData as PostModel;
    if (post.poster == userReference) return Number.MIN_VALUE;
    const comments = await Promise.all(post.comments.map(async reference => {
        return (await reference.get()).data() as CommentModel;
    }))

    const isSaved = user.savedPosts.includes(postReference);
    const posterIsFollowed = user.usersFollowing.includes(post.poster);
    const genreIsFollowed = user.genresFollowing.includes(post.genre);
    const hasUpvoted = post.upvoteList.includes(userReference);
    const hasDownvoted = post.downvoteList.includes(userReference);
    const commentsMade = comments.filter(comment => comment.poster == userReference).length;

    let relevanceScore = 0;
    relevanceScore += hasDownvoted? -1 : 0;
    relevanceScore += hasUpvoted? 1 : 0;
    relevanceScore += 2 * commentsMade;
    relevanceScore += isSaved? 2 : 0;

    if (!posterIsFollowed && genreIsFollowed) relevanceScore *= 1;
    else if (posterIsFollowed && !genreIsFollowed) relevanceScore *= 2;
    else relevanceScore *= 3;

    return relevanceScore;
}

const POST_LIMIT_DEFAULT = 10;
const POST_LIMIT_MAXIMUM = 30;

async function relevantPosts(req: https.Request): Promise<RelevantPostResponse> {
    if (!req.query.uid) return new RelevantPostResponse();
    const uid = req.query.uid.toString();
    let maxPosts = POST_LIMIT_DEFAULT;
    if (req.query.limit) {
        const limit = +req.query.limit;
        if (limit > 0 && limit < POST_LIMIT_MAXIMUM) {
            maxPosts = limit;
        }
    }

    const userReference = firestore.collection('users').doc(uid);
    const userData = (await userReference.get()).data()
    if (!userData) return new RelevantPostResponse();
    const user = userData as UserModel;

    const postCandidates = await getUserPostCandidates(user);
    const postScores = await Promise.all(postCandidates.map(post => postRelevancy(post, userReference, user)));
    const candidatesWithScores = postScores.map((score, index) => {
        return {
            score: score,
            post: postCandidates[index],
        };
    });
    const mostRelevantCandidates = candidatesWithScores
        .sort((a, b) => a.score - b.score)
        .slice(0, maxPosts);
    const result = await Promise.all(mostRelevantCandidates.map(pair => pair.post.get()));

    return {
        posts: result.map(post => post.data() as PostModel),
    };
}
