import { api } from "../axios";
import type { LoginFormBody, RegisterFormBody, RegisterResponse } from "@/types";


export async function registerUser(payload: RegisterFormBody) : Promise<RegisterResponse> {
    const {data : response} = await api.post("/auth/register", payload);
    return response as RegisterResponse;
}

export async function loginUser(payload: LoginFormBody) : Promise<string>{
    const { data } = await api.post("/auth/login", payload);
    return data.token as string;
}

export function logoutUser() {
    localStorage.removeItem("my-cloud-token")
}