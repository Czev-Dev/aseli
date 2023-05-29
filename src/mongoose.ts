import { connect, Schema, model } from "mongoose";

let env = process.env;
connect(`mongodb://${env.DB_HOST}`, { dbName: env.DB_NAME })
.then(() => console.log("Connected to database!"));

export const User = model("User", new Schema({
    username: String,
    password: String,
    description: {
        default: null,
        type: String
    },
    profil: {
        default: null,
        type: String
    }
}));