import multer from "multer";
import path from "path";

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

export default upload