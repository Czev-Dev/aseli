import { User } from "@mongoose";
import express from "express";

const paths = /(follow|profil)/g;
export default async function (req: express.Request, res: express.Response, next: express.NextFunction){
    if(paths.test(req.url) && req.method != "GET"){
        if(!("user_id" in req.body)) return res.err("Missing user_id!")
        let user = await User.countDocuments({ _id: req.body.user_id });
        if(user < 1) return res.err("User not found!");
    }
    next();
}