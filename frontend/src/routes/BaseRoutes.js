import {Navigate, Route, Routes} from "react-router-dom";
import Login from "../pages/login/Login";
import Layout from "../layout/background/Layout";
import NewPost from "../pages/post/NewPost";
import Error404 from "../pages/error/Error404";
import PostList from "../pages/post/PostList";
import Signup from "../pages/login/Signup";
import MyPageLike from "../pages/mypage/MyPageLike";
import MyPageComment from "../pages/mypage/MyPageComment";
import MyPageFollower from "../pages/mypage/MyPageFollower";
import MyPageMentions from "../pages/mypage/MyPageMentions";
import MyPageBlock from "../pages/mypage/MyPageBlock";
import MyPageModify from "../pages/mypage/MyPageModify";
import PostDetail from "../pages/post/PostDetail";

const BaseRoutes = () => {
    return (
        <Routes>
            <Route exact path="login" element={<Login />} />
            <Route exact path="signup" element={<Signup />} />
            <Route path="/" element={<Layout />} >
                {/* 메인 도메인만 입력 시 로그인으로 이동*/}
                <Route path="" element={<Navigate to="login" replace />} />
                <Route path="post/list" element={<PostList />} />
                <Route path="post/new" element={<NewPost />} />
                <Route path="post/detail/:postId" element={<PostDetail />} />
                <Route path="mypage/like" element={<MyPageLike />} />
                <Route path="mypage/comment" element={<MyPageComment />} />
                <Route path="mypage/follower" element={<MyPageFollower />} />
                <Route path="mypage/mentions" element={<MyPageMentions />} />
                <Route path="mypage/block" element={<MyPageBlock />} />
                <Route path="mypage/modify" element={<MyPageModify />} />
            </Route>
            {/* 에러페이지 */}
            <Route path={"*"} element={<Error404 />} />
        </Routes>
    );
}

export default BaseRoutes;