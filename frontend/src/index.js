import React, {StrictMode} from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import {BrowserRouter} from "react-router-dom";
import {RecoilRoot} from "recoil";
import {QueryClient, QueryClientProvider} from "react-query";
import axios from "axios";
import {decode, encode} from "html-entities";

// react-qeury사용을 위해 선언
const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: false // Disable retries globally
            // , onError: async (error) => { // 쿼리 실패시 에러처리
            //     const errorMessage = error?.response?.data?.message || '잠시 후 다시 시도해 주세요';
                // await Alert({
                //     html: `처리 중 오류가 발생하였습니다.<br>${errorMessage}`
                // });
            // } //onError end
        },
    },
});

// Axios 요청 인터셉터 - 요청 전에 실행됨
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    // Content-Type이 없으면 기본적으로 application/json으로 설정
    if (!config.headers['Content-Type']) {
        config.headers['Content-Type'] = 'application/json';
    }

    // Content-Type이 application/json인 경우 이스케이프 처리
    /*if (config.data && config.headers['Content-Type'] === 'application/json') {
        const encodeObject = (obj) => {
            for (let key in obj) {
                if (typeof obj[key] === 'string') {
                    obj[key] = encode(obj[key]);
                } else if (typeof obj[key] === 'object') {
                    encodeObject(obj[key]);
                }
            }
        };
        encodeObject(config.data); // 요청 데이터 이스케이프 처리
    }*/
    return config;
}, error => {
    return Promise.reject(error);
});

// Axios 응답 인터셉터 - 응답 후에 실행됨
axios.interceptors.response.use(response => {
    const token = response.headers['authorization'];
    if (token) {
        localStorage.setItem('token', token);
    }

    /*if (response.data instanceof Blob) {
        return response;
    } else if (response.data && typeof response.data === 'string') {
        response.data = decode(response.data);
    } else if (response.data && typeof response.data === 'object') {
        const decodeObject = (obj) => {
            for (let key in obj) {
                if (typeof obj[key] === 'string') {
                    obj[key] = decode(obj[key]);
                } else if (typeof obj[key] === 'object') {
                    decodeObject(obj[key]);
                }
            }
        };
        decodeObject(response.data);
    }*/
    // 응답 데이터 디코드 로직 - Blob 타입이 아닌 경우에만 디코드 처리
    // if (response.data instanceof Blob) {
    //     return response;
    // } else if (response.data && typeof response.data === 'string') {
    //     response.data = decode(response.data);
    // } else if (response.data && typeof response.data === 'object') {
    //     const decodeObject = (obj) => {
    //         for (let key in obj) {
    //             if (typeof obj[key] === 'string') {
    //                 obj[key] = decode(obj[key]);
    //             } else if (typeof obj[key] === 'object') {
    //                 decodeObject(obj[key]);
    //             }
    //         }
    //     };
    //     decodeObject(response.data);
    // }
    return response;
}, error => {
    // 401 또는 403 에러인 경우 로그인 페이지로 리다이렉트
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
        //이 부분은 원래는 navigate로 핸들링 해야하지만 현재 리액트 포팅 진행중이므로 window객체에 직접 접근함
        //추후 navigate로 포팅되어야 함
        window.location.href = "/login";
    }
    return Promise.reject(error);
});

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
    <BrowserRouter>
        <StrictMode>
            <RecoilRoot>
                <QueryClientProvider client={queryClient}>
                    <App />
                </QueryClientProvider>
            </RecoilRoot>
        </StrictMode>
    </BrowserRouter>
);

