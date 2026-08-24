import { api } from "../axios";
import type { LoginFormBody, RegisterFormBody } from "@/types";


export async function registerUser(payload: RegisterFormBody) : Promise<void> {
    await api.post("/auth/register", payload);
}

export async function loginUser(payload: LoginFormBody) : Promise<void>{
    await api.post("/auth/login", payload);
}

export async function logoutUser() : Promise<void>{
    await api.post("/auth/logout");
}