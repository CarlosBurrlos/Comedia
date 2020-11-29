import admin from 'firebase-admin';
import {https} from "firebase-functions";
import DocumentReference = admin.firestore.DocumentReference;
import DocumentSnapshot = admin.firestore.DocumentSnapshot;
import Timestamp = admin.firestore.Timestamp;
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

function commonLength(array1: any[], array2: any[]): number {
    return array1.filter(value => array2.includes(value)).length;
}

const resolvers: { [index: string]: (req: https.Request) => Promise<object> } = {
    '/relevantUsers': relevantUsers,
    '/relevantPosts': relevantPosts,
};

// This was me late at night trying to write a function, it's probably all wrong
exports.calcRelevancyUser = https.onRequest(async (req, res) => {
    const response = await resolvers[req.path](req);
    res.json(response);
});

async function relevantUsers(req: https.Request): Promise<object> {
    // TODO: Return users sorted by relevance
    return {};
}

// NOTE: The params here may change depending on what we need it to do
async function calculateUserRelevancy(req: https.Request): Promise<number> {
    // Grab the parameters
    if (!(req.query.user && req.query.target)) return -1;

    const uid: string = req.query.user.toString();
    const t_uid: string = req.query.target.toString();

    // Get the documents
    const user = await firestore.collection('users').doc(uid).get();
    const target = await firestore.collection('users').doc(t_uid).get();

    let rel = 0;
    if (user.exists && target.exists) {
        // calculate relevancy
        let user_data = user.data()
        let target_data = target.data()

        let rel_mult = 1;
        if (user.get("usersFollowing").contains(target.ref)) {
            rel_mult *= 1.5;
        }
        if (user.get("followers").contains(target.ref)) {
            rel_mult *= 1.5;
        }

        // The number of times the user has upvoted the target's posts
        rel += commonLength(user.get("upvotedPosts"), target.get("createdPosts"));

        // The number of times the user has downvoted the target's posts
        rel -= commonLength(user.get("downvotedPosts"), target.get("createdPosts"));

        // The number of times the user has commented on the target's posts
        //rel += commonLength(user.get("upvoteList"),target.get("createdPosts"));

        rel *= rel_mult;
        // TODO: Calculate relevancy for two users
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
