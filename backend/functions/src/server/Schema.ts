import "reflect-metadata";
import {Arg, buildSchemaSync, Field, FieldResolver, Query, Resolver} from "type-graphql";
import User from "./schema/User";
import Post from "./schema/Post";
import Comment from "./schema/Comment";
import Profile from "./schema/Profile";
import PostType from "./schema/PostType";

// @Resolver()
// class ComediaResolver {
//     @Query((returns?: void) => [User])
//     async users(): Promise<User[]> {
//         return [];
//     }
//
//     @Query((returns?: void) => [Post])
//     async posts(): Promise<Post[]> {
//         return [];
//     }
//
//     @Query((returns?: void) => [Comment])
//     async comments(): Promise<Comment[]> {
//         return [];
//     }
//
//     @Query((returns?: void) => [Profile])
//     async profiles(): Promise<Profile[]> {
//         return [];
//     }
// }

@Resolver((of?: void) => User)
class UserResolver {
    @FieldResolver()
    username(): string {
        return '';
    }

    @FieldResolver()
    email(): string {
        return '';
    }

    @FieldResolver()
    password(): string {
        return '';
    }

    @FieldResolver((type?: void) => [User])
    usersFollowing(): User[] {
        return [];
    }

    @FieldResolver((type?: void) => [User])
    followers(): User[] {
        return [];
    }

    @FieldResolver()
    genresFollowing(): string[] {
        return [];
    }

    @FieldResolver((type?: void) => [Post])
    createdPosts(): Post[] {
        return [];
    }

    @Field((type?: void) => [Post])
    savedPosts(): Post[] {
        return [];
    }

    @FieldResolver((type?: void) => [Comment])
    comments(): Comment[] {
        return [];
    }

    @FieldResolver()
    profile(): Profile {
        return new Profile();
    }
}

@Resolver((of?: void) => Post)
class PostResolver {
    @FieldResolver()
    poster(): User {
        return new User();
    }

    @FieldResolver()
    genre(): string {
        return '';
    }

    @FieldResolver()
    type(): PostType {
        return PostType.TEXT;
    }

    @FieldResolver()
    content(): string {
        return '';
    }

    @FieldResolver((type?: void) => [Comment])
    comments(): Comment[] {
        return [];
    }

    @FieldResolver()
    upvoteCount(): number {
        return -1;
    }

    @FieldResolver()
    downvoteCount(): number {
        return -1;
    }
}

@Resolver((of?: void) => Comment)
class CommentResolver {
    @FieldResolver()
    poster(): User {
        return new User();
    }

    @FieldResolver()
    parent(): Post {
        return new Post();
    }

    @FieldResolver()
    content(): string {
        return '';
    }
}

@Resolver((of?: void) => Profile)
class ProfileResolver {
    @FieldResolver()
    user(): User {
        return new User();
    }

    @FieldResolver()
    profileImage(): string {
        return '';
    }

    @FieldResolver()
    biography(): string {
        return '';
    }
}

@Resolver()
class BasicResolver {
    @Query(() => [User])
    users(@Arg('ids', () => [String]) ids: string[]): User[] {
        return [];
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
