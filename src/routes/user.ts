import { Request, Response } from "express";
import { User } from "@mongoose";
import fs from "fs";

// username, password
async function login({ body }: Request, res: Response){
    if(body.missing("username", "password")) return res.missing();
    let user = await User.findOne({ username: body.username, password: body.password }).select({ _id: 1 });
    res.success({ id_user: user ? user._id : null });
}
// username, password, confirm_password
async function register({ body }: Request, res: Response){
    if(body.missing("username", "password", "confirm_password")) return res.missing();
    if(body.password != body.confirm_password) return res.err("Konfirmasi password salah!");

    let exist = await User.countDocuments({ username: body.username });
    if(exist) return res.err("Username sudah digunakan!");

    let user = await User.create({ username: body.username, password: body.password });
    res.success({ id_user: user._id.toString() ?? null });
}
// username
async function get_profil({ params }: Request, res: Response){
    let user = await User.findOne({ username: params.username }, { _id: 0, password: 0, __v: 0 }).lean();
    if(user) res.success(user);
    else res.err();
}
// user_id, profil, description
async function post_profil({ body, file }: Request, res: Response){
    if(body.missing("user_id", "description")) return res.missing();
    let user = await User.findById(body.user_id).select({ profil: 1 });

    if(fs.existsSync("./uploads/" + user?.profil)) fs.unlinkSync("./uploads/" + user?.profil);
    await User.updateOne({ _id: body.user_id }, { profil: file?.filename, description: body.description });
    res.success();
}

export default { login, register, get_profil, post_profil };