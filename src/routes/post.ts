import { Post, User } from "@mongoose";
import { Request, Response } from "express";
import { unlinkSync } from "fs";

// page?
async function get({ params, query }: Request, res: Response){
    if(params.page != null && isNaN(parseInt(params.page))) return res.err("Page is not a number!");
    const page = (params.page != null && Number(params.page) > 1) ? Number(params.page) - 1 : 0;
    let where: { userId?: string | undefined } = {}

    if("user_id" in query) where.userId = query.user_id?.toString();
    else if("username" in query){
        let user = await User.findOne({ username: query.username }).select({ _id: 1 });
        where.userId = user?._id.toString();
    }

    let curPosts = await Post.find(where, {}, { skip: page * 10, limit: 10  })
        .select({ description: 1, imageName: 1, time: 1, comments: 1, ril: 1, fek: 1 });
    let posts = curPosts.map(post => {
        let val = Date.now() - post.time.getTime();
        let seconds = Math.floor(val / 1000);
        let minutes = Math.floor(seconds / 60);
        let hours = Math.floor(minutes / 60);
        let days = Math.floor(hours / 24);
        let months = Math.floor(days / 30);
        let years = Math.floor(months / 12);
        let time = seconds + " detik yang lalu";
        switch(true){
            case (years > 0):
                time = years + " tahun yang lalu";
                break;
            case (months > 0):
                time = months + " bulan yang lalu";
                break;
            case (days > 0):
                time = days + " hari yang lalu";
                break;
            case (hours > 0):
                time = hours + " jam yang lalu";
                break;
            case (minutes > 0):
                time = minutes + " menit yang lalu";
                break;
        }
        return { post_id: post._id, description: post.description, imageName: post.imageName, comments: post.comments, time, ril: post.ril.length, fek: post.fek.length };
    })
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
// user_id, post_id
async function ril({ body }: Request, res: Response){
    if(body.missing("user_id", "post_id")) return res.missing();
    let post = await Post.findById(body.post_id).select({ ril: 1 });
    let user = await User.findById(body.user_id).select({ ril: 1 });

    let exist = post?.ril.includes(body.user_id) || user?.ril.includes(body.post_id);
    res.success(`Success ${exist ? "un" : ""}ril post!`);
    if(exist){
        await post?.updateOne({ $pull: { ril: body.user_id } });
        await user?.updateOne({ $pull: { ril: body.post_id } });
    } else {
        await post?.updateOne({ $push: { ril: body.user_id } });
        await user?.updateOne({ $push: { ril: body.post_id } });
    }
}
// user_id, post_id
async function fek({ body }: Request, res: Response){
    if(body.missing("user_id", "post_id")) return res.missing();
    let post = await Post.findById(body.post_id).select({ fek: 1 });
    let user = await User.findById(body.user_id).select({ fek: 1 });

    let exist = post?.fek.includes(body.user_id) || user?.fek.includes(body.post_id);
    res.success(`Success ${exist ? "un" : ""}fek post!`);
    if(exist){
        await post?.updateOne({ $pull: { fek: body.user_id } });
        await user?.updateOne({ $pull: { fek: body.post_id } });
    } else {
        await post?.updateOne({ $push: { fek: body.user_id } });
        await user?.updateOne({ $push: { fek: body.post_id } });
    }
}
export default { get, post, ril, fek };