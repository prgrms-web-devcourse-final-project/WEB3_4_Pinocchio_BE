import {useQuery} from "react-query";
import axios from "axios";
import PageLayout from "../../layout/page/PageLayout";
import SearchCardBox from "../../shared/SearchCardBox";
import MyPageTabLayout from "./MyPageTabLayout";
import TableBackGroundCard from "../../shared/TableBackGroundCard";
import FlexibleTable from "../../shared/table/FlexibleTable";
import UserProfile from "../share/UserProfile";
import {Col, Row} from "react-bootstrap";
import {jwtDecode} from "jwt-decode";

const fetchMyPageFollowerList = async (userId) => {
    const response = await axios.post(`/user/${userId}/followers`);
    return response.data;
};

const fetchMyPageFollowingList = async (userId) => {
    const response = await axios.post(`/user/${userId}/followings`);
    return response.data;
};

const MyPageFollower = () => {
    const token = localStorage.getItem('token');
    const loginUser = jwtDecode(token);
    const { isLoading: follwerLoading, data: followerData } = useQuery(
        ['fetchMyPageFollowerList'],
        () => fetchMyPageFollowerList(loginUser.tsid),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    const { isLoading: followingLoading, data: followingData } = useQuery(
        ['fetchMyPageFollowingList'],
        () => fetchMyPageFollowingList(loginUser.tsid),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    const initFollwerColumns = [
        {
            accessorKey: "nickname",
            header: "팔로우",
        },
    ];

    const initFollowingColumns = [
        {
            accessorKey: "nickname",
            header: "팔로워",
        },
    ];

    return (
        <PageLayout>
            <SearchCardBox>
                <UserProfile page={"mypage"}/>
            </SearchCardBox>
            <MyPageTabLayout currentTabKey={"follower"} >
                <TableBackGroundCard>
                    <Row>
                        <Col>
                            <FlexibleTable initColumns={initFollwerColumns} data={followerData?.followers || []} isLoading={follwerLoading} />
                        </Col>
                        <Col>
                            <FlexibleTable initColumns={initFollowingColumns} data={followingData?.followings || []} isLoading={followingLoading} />
                        </Col>
                    </Row>
                </TableBackGroundCard>
            </MyPageTabLayout>
        </PageLayout>
    )
}

export default MyPageFollower;