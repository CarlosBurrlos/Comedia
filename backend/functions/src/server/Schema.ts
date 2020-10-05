import "reflect-metadata";
import {Arg, buildSchemaSync, Query, Resolver} from "type-graphql";
import User from "./schema/User";
import Post from "./schema/Post";
import Comment from "./schema/Comment";
import Profile from "./schema/Profile";

@Resolver((of?: void) => User)
class UserResolver {
}

@Resolver((of?: void) => Post)
class PostResolver {
}

@Resolver((of?: void) => Comment)
class CommentResolver {
}

@Resolver((of?: void) => Profile)
class ProfileResolver {
}

@Resolver()
class BasicResolver {
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
    resolvers: [BasicResolver, UserResolver, PostResolver, CommentResolver, ProfileResolver],
    emitSchemaFile: {
        path: 'C:\\Users\\nicho\\Desktop\\schema.txt'
    }
});
export default schema;
