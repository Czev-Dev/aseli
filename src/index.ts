import dotenv from "dotenv";
dotenv.config()
import app from "./routes"

const env = process.env;
const PORT = Number(env.SERVER_PORT) ?? 8080;
const HOST = env.SERVER_HOST ?? "localhost";

app.listen(PORT, HOST, () => console.log(`Listening on ${HOST}:${PORT}`));