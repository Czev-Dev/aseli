import { Post, User } from "@mongoose";
import { Request, Response } from "express";
import { unlinkSync } from "fs";

// page?
async function get({ params }: Request, res: Response){
    if(params.page != null && isNaN(parseInt(params.page))) return res.err("Page is not a number!");
    const page = (params.page != null && Number(params.page) > 1) ? Number(params.page) - 1 : 0;
    let posts = await Post.find({}, {}, { skip: page * 10, limit: 10  });
    res.success(posts);
}
// user_id, description, image
async function post({ body, file }: Request, res: Response){
    if(body.missing("user_id", "description") || file == undefined) return res.missing();
    let checkUser = await User.findById(body.user_id).select({ postCooldown: 1 });

    if(checkUser?.postCooldown){
        unlinkSync("./uploads/" + file.filename);
        return res.err("Kamu harus menunggu sebelum bisa memposting lagi!");
    }
    let user = await Post.create({ userId: body.user_id, description: body.description, imageName: file.filename });
    res.success({ post_id: user._id });
    await User.updateOne({ _id: body.user_id }, { postCooldown: true });
    setTimeout(async () => await User.updateOne({ _id: body.user_id }, { postCooldown: false }), 30 * 1000);
}
export default { get, post };