import express from "express";
import bodyParser from "body-parser";
import multer from "multer";
import path from "path";

import "./mongoose";
import user from "./routes/user";
import check_body from "./middleware/check_body";

const app = express();
const storage = multer.diskStorage({
    destination: (_req, _file, cb) => cb(null, "./uploads"),
    filename: (_req, file, cb) => cb(null, file.fieldname + "-" + Date.now() + path.extname(file.originalname))
});
const upload = multer({
    storage: storage,
    fileFilter: (_req, file, cb) => {
        var ext = path.extname(file.originalname).replace(".", "");
        if(!/^image\/(png|jpe?g|gif)$/.test(file.mimetype) || !/^(png|jpe?g|gif)$/.test(ext))
            return cb(new Error("Hanya foto yang diperbolehkan!"));
        cb(null, true);
    },
    limits:{ fileSize: 3 * 1024 * 1024 }
});
app.use("/uploads", express.static("./uploads"));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(check_body);

// Routes
app.post("/user/login", user.login);
app.post("/user/register", user.register);
app.get("/user/profil/:username", user.get_profil);
app.post("/user/profil", [upload.single("profil"), check_body], user.post_profil);

export default app;