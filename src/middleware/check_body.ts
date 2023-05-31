import { User } from "@mongoose";
import express from "express";

declare global {
    namespace Express {
        export interface Response {
            success: (data?: any) => void;
            err: (message?: string | null) => void;
            missing: () => void;
            user_exist: (user_id: string) => boolean
        }
        export interface Request {
            body: {
                missing: (...args: string[]) => boolean;
            },
            params: {
                missing: (...args: string[]) => boolean;
            },
            query: {
                missing: (...args: string[]) => boolean;
            }
        }
    }
}
export default function (req: express.Request, res: express.Response, next: express.NextFunction){
    Object.defineProperties(res, {
        success: {
            enumerable: false,
            configurable: true,
            value: (data: any = null) => res.json({ success: true, data: data })
        },
        err: {
            enumerable: false,
            configurable: true,
            value: (message: string | null = null) => res.json({ success: false, message })
        },
        missing: {
            enumerable: false,
            configurable: true,
            value: () => res.err("Missing field")
        },
        user_exist: {
            enumerable: false,
            configurable: true,
            value: async (user_id: string) => (await User.countDocuments({ _id: user_id })) > 0
        }
    });
    const missing = function(this: {}, ...args: string[]){
        for(var i = 0; i < args.length; i++) if(!(args[i] in this)) return true;
        return false;
    };
    Object.defineProperty(req.body, "missing", { enumerable: false, configurable: true, value: missing });
    Object.defineProperty(req.params, "missing", { enumerable: false, configurable: true, value: missing });
    Object.defineProperty(req.query, "missing", { enumerable: false, configurable: true, value: missing });
    next();
}