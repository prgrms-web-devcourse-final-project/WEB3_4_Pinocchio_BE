import SearchCardBox from "../../../shared/SearchCardBox";
import PageLayout from "../../../layout/page/PageLayout";
import {useQuery} from "react-query";
import axios from "axios";
import UserProfile from "../share/UserProfile";
import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import ProfileEditCard from "./ProfileEditCard";
import ChangePasswordCard from "./ChangePasswordCard";

const fetchMyPageLikeList = async () => {
    const response = await axios.get(`/user/{userId}/activities/likes`);
    return response.data;
};

const MyPageModify = () => {
    return (
        <PageLayout>
            <ProfileEditCard />
            <ChangePasswordCard />
        </PageLayout>
    )
}

export default MyPageModify;