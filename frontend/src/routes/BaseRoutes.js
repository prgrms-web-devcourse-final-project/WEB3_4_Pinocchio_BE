import {Navigate, Route, Routes} from "react-router-dom";
import Login from "../pages/login/Login";
import Layout from "../layout/background/Layout";
import NewPost from "../pages/board/NewPost";
import Error404 from "../pages/error/Error404";
import MainBoardList from "../pages/board/MainBoardList";

const BaseRoutes = () => {
    return (
        <Routes>
            <Route exact path="login" element={<Login />} />
            <Route path="/" element={<Layout />} >
                {/* 메인 도메인만 입력 시 로그인으로 이동*/}
                <Route path="" element={<Navigate to="login" replace />} />
                <Route path="board/list" element={<MainBoardList />} />
                <Route path="board/new" element={<NewPost />} />
            </Route>
            {/* 에러페이지 */}
            <Route path={"*"} element={<Error404 />} />
        </Routes>
    );
}

export default BaseRoutes;