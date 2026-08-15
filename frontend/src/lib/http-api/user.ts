import type { UserResponse } from "@/types";
import { api } from "../axios";



export async function me() : Promise<UserResponse> {
    const {data:response} =    await api.get("/me");
    return response;
}