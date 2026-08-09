import axios from 'axios';

export const api = axios.create({
    baseURL: "/api"
})

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("my-cloud-token");
    if(token) 
        config.headers.Authorization = `Bearer ${token}`;

    return config;
})

api.interceptors.response.use(
    (res) => res,
    (error) => {
        const isAuthRoute = error.config?.url?.includes("/auth/login") || error.config?.url?.includes("/auth/register");

        if (error.response?.status === 401 && !isAuthRoute) {
            localStorage.removeItem("my-cloud-token");
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);