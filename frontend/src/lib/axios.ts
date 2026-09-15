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
        return Promise.reject(error);
    }
);