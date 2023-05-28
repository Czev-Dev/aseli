import express from "express";
import bodyParser from "body-parser";
import "./mongoose";
import index from "./routes/index";

const app = express();
app.use(bodyParser.json());

// Routes
app.post("/user/login", index.login);
app.post("/user/register", index.register);

export default app;