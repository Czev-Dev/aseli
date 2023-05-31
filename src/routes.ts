import express from "express";
import bodyParser from "body-parser";
import "./mongoose";
import check_body from "./middleware/check_body";
import check_user_id from "./middleware/check_user_id";
import upload from "./middleware/multer";
import user from "./routes/user";
import post from "./routes/post"

const app = express();
app.use("/uploads", express.static("./uploads"));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(check_body);

// Routes
app.get("/post/:page?", post.get);
app.post("/post", [upload.single("image"), check_user_id, check_body], post.post);

app.post("/user/login", user.login);
app.post("/user/register", user.register);
app.post("/user/follow", check_user_id, user.follow);

app.get("/user/profil/:username/:user_id?", user.get_profil);
app.post("/user/profil", [upload.single("profil"), check_user_id, check_body], user.post_profil);

export default app;