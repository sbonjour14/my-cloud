import axios from 'axios';
const apiUrl = import.meta.env.VITE_API_URL;

export const api = axios.create({
    baseURL: apiUrl,
    withCredentials: true,

})

console.log(apiUrl);


api.interceptors.response.use(
    (res) => res,
    (error) => {
        const isAuthRoute = error.config?.url?.includes("/auth/login") || error.config?.url?.includes("/auth/register");

        if (error.response?.status === 401 && !isAuthRoute) {
            window.location.href = "/login";
        }
        return Promise.reject(error);
    }
);