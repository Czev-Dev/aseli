import { connect, Schema, model } from "mongoose";

let env = process.env;
connect(`mongodb://${env.DB_HOST}`, { dbName: env.DB_NAME })
.then(() => console.log("Connected to database!"));

export const User = model("User", new Schema({
    username: String,
    password: String,
    description: {
        type: String,
        default: null
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