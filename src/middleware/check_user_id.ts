import { User } from "@mongoose";
import express from "express";

export default async function (req: express.Request, res: express.Response, next: express.NextFunction){
    if(!("user_id" in req.body) || req.body.user_id.length < 24) return res.err("Missing user_id!")
    let user = await User.countDocuments({ _id: req.body.user_id });
    if(user < 1) return res.err("User not found!");
    next();
}