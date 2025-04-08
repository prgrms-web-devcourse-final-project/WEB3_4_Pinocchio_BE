import {Navigate, Route, Routes} from "react-router-dom";
import Login from "../pages/login/Login";
import Layout from "../layout/background/Layout";
import NewPost from "../pages/board/NewPost";
import Error404 from "../pages/error/Error404";
import MainBoardList from "../pages/board/MainBoardList";
import Signup from "../pages/login/Signup";
import MyPageLike from "../pages/board/mypage/MyPageLike";
import MyPageComment from "../pages/board/mypage/MyPageComment";
import MyPageFollower from "../pages/board/mypage/MyPageFollower";
import MyPageMentions from "../pages/board/mypage/MyPageMentions";
import MyPageBlock from "../pages/board/mypage/MyPageBlock";
import MyPageModify from "../pages/board/mypage/MyPageModify";

const BaseRoutes = () => {
    return (
        <Routes>
            <Route exact path="login" element={<Login />} />
            <Route exact path="signup" element={<Signup />} />
            <Route path="/" element={<Layout />} >
                {/* 메인 도메인만 입력 시 로그인으로 이동*/}
                <Route path="" element={<Navigate to="login" replace />} />
                <Route path="board/list" element={<MainBoardList />} />
                <Route path="board/new" element={<NewPost />} />
                <Route path="board/mypage/like" element={<MyPageLike />} />
                <Route path="board/mypage/comment" element={<MyPageComment />} />
                <Route path="board/mypage/follower" element={<MyPageFollower />} />
                <Route path="board/mypage/mentions" element={<MyPageMentions />} />
                <Route path="board/mypage/block" element={<MyPageBlock />} />
                <Route path="board/mypage/modify" element={<MyPageModify />} />
            </Route>
            {/* 에러페이지 */}
            <Route path={"*"} element={<Error404 />} />
        </Routes>
    );
}

export default BaseRoutes;