import {Field, ObjectType} from "type-graphql";
import Comment from "./Comment";
import PostType from "./PostType";
import User from "./User";

@ObjectType()
class Post {
    @Field()
    poster!: User;
    @Field()
    genre!: string;
    @Field()
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
