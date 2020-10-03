import "reflect-metadata";
import {Field, ObjectType} from "type-graphql";
import User from "./User";

@ObjectType()
class Profile {
    @Field((type?: void) => User)
    user!: User;
    @Field()
    profileImage!: string;
    @Field()
    biography!: string;
}

export default Profile;
