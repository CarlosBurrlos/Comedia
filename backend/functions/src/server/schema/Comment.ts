import {Field, ObjectType} from "type-graphql";
import User from "./User";
import Post from "./Post";

@ObjectType()
class Comment {
    @Field()
    poster!: User;
    @Field()
    parent!: Post;
    @Field()
    content!: string;
}

export default Comment;
