import "reflect-metadata";
import {Field, ObjectType} from "type-graphql";
import Comment from "./Comment";
import PostType from "./PostType";
import User from "./User";

@ObjectType()
class Post {
    @Field((type?: void) => User)
    poster!: User;
    @Field()
    genre!: string;
    @Field((type?: void) => PostType)
    type!: PostType;
    @Field()
    content!: string;
    @Field((type?: void) => [Comment])
    comments!: Comment[];
    @Field()
    upvoteCount!: number;
    @Field()
    downvoteCount!: number;
}

export default Post;
