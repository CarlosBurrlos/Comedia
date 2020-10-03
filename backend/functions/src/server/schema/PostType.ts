import {registerEnumType} from "type-graphql";

enum PostType {
    TEXT,
    IMAGE,
    URL
}
registerEnumType(PostType, {
    name: "PostType",
    description: "The type of content contained in a post."
})

export default PostType;
