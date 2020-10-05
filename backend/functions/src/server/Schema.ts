import "reflect-metadata";
import {Arg, buildSchemaSync, Query, Resolver} from "type-graphql";
import User from "./schema/User";
import Post from "./schema/Post";
import Comment from "./schema/Comment";
import Profile from "./schema/Profile";
import * as admin from 'firebase-admin';
admin.initializeApp();

@Resolver()
class BasicResolver {
    @Query(() => User)
    async user(@Arg('username',() => String) username: string): Promise<User> {
        const users = await admin.firestore().collection("users").where('username','==',username).get();
        //console.log("users");
        //users.docs.forEach((user) => {console.log(user.data())});
        return users.docs[0].data() as User;
    }

    @Query(() => [User])
    users(@Arg('ids', () => [String]) ids: string[]): User[] {
        return [ new User() ];
    }

    @Query(() => [Post])
    posts(@Arg('ids', () => [String]) ids: string[]): Post[] {
        return [];
    }

    @Query(() => [Comment])
    comments(@Arg('ids', () => [String]) ids: string[]): Comment[] {
        return [];
    }

    @Query(() => [Profile])
    profiles(@Arg('ids', () => [String]) ids: string[]): Profile[] {
        return [];
    }
}

const schema = buildSchemaSync({
    resolvers: [BasicResolver]
});
export default schema;
