import { connect, Schema, model } from "mongoose";

let env = process.env;
connect(`mongodb://${env.DB_HOST}`, { dbName: env.DB_NAME })
.then(() => console.log("Connected to database!"));

export const User = model("user", new Schema({
    username: String,
    password: String,
    postCooldown: {
        type: Boolean,
        default: false 
    },
    commentCooldown: {
        type: Boolean,
        default: false 
    },
    description: {
        type: String,
        default: null
    },
    ril: {
        type: [Schema.Types.ObjectId],
        default: []
    },
    fek: {
        type: [Schema.Types.ObjectId],
        default: []
    },
    following: {
        type: [String],
        default: []
    },
    followers: {
        type: [String],
        default: []
    },
    profil: {
        type: String,
        default: null
    }
}));
export const Post = model("post", new Schema({
    userId: Schema.Types.ObjectId,
    description: String,
    imageName: String,
    time: {
        type: Date,
        default: Date.now
    },
    ril: {
        type: [Schema.Types.ObjectId],
        default: []
    },
    fek: {
        type: [Schema.Types.ObjectId],
        default: []
    },
    comments: {
        type: [{
            username: String,
            comment: String,
            time: {
                type: Date,
                default: Date.now
            }
        }],
        default: []
    }
}));