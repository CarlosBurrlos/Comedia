import "reflect-metadata";
import {Field, ObjectType} from "type-graphql";
import User from "./User";
import Post from "./Post";

@ObjectType()
class Comment {
    @Field((type?: void) => User)
    poster!: User;
    @Field((type?: void) => Post)
    parent!: Post;
    @Field()
    content!: string;
}

export default Comment;
