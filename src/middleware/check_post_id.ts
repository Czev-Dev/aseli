import { Post } from "@mongoose";
import express from "express";

export default async function (req: express.Request, res: express.Response, next: express.NextFunction){
    if(!("post_id" in req.body) || req.body.post_id.length < 24) return res.err("Missing user_id!")
    let post = await Post.countDocuments({ _id: req.body.post_id });
    if(post < 1) return res.err("Post not found!");
    next();
}