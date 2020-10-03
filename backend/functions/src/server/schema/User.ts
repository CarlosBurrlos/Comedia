import "reflect-metadata";
import {Field, ObjectType} from "type-graphql";
import Comment from "./Comment";
import Post from "./Post";
import Profile from "./Profile";

@ObjectType()
class User {
    @Field()
    username!: string;
    @Field()
    email!: string;
    @Field()
    password!: string;
    @Field((type?: void) => [User])
    usersFollowing!: User[];
    @Field((type?: void) => [User])
    followers!: User[];
    @Field((type?: void) => [String])
    genresFollowing!: string[];
    @Field((type?: void) => [Post])
    createdPosts!: Post[];
    @Field((type?: void) => [Post])
    savedPosts!: Post[];
    @Field((type?: void) => [Comment])
    comments!: Comment[];
    @Field((type?: void) => Profile)
    profile!: Profile;
}

export default User;