import express from "express";
import { User } from "@mongoose";

async function login({ body }: express.Request, res: express.Response){
    let user = await User.findOne({ username: body.username, password: body.password }).select({ _id: 1 });
    res.json({ success: !!user, id_user: user ? user._id : null });
}

async function register({ body }: express.Request, res: express.Response){
    if(body.password != body.confirm_password) return res.json({ status:false, message: "Konfirmasi password salah!" });

    let exist = await User.countDocuments({ username: body.username });
    if(exist) return res.json({ success: false, message: "Username sudah digunakan!" });

    let user = await User.create({ username: body.username, password: body.password });
    res.json({ success: true, id_user: user._id.toString() });
}

export default { login, register };