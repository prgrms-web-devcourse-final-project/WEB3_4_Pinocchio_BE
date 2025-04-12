import SearchCardBox from "../../shared/SearchCardBox";
import PageLayout from "../../layout/page/PageLayout";
import {useQuery} from "react-query";
import axios from "axios";
import FlexibleTable from "../../shared/table/FlexibleTable";
import TableBackGroundCard from "../../shared/TableBackGroundCard";
import MyPageTabLayout from "./MyPageTabLayout";
import UserProfile from "../share/UserProfile";
import {jwtDecode} from "jwt-decode";

const fetchMyPageLikeList = async () => {
    const token = localStorage.getItem('token');
    const loginUser = jwtDecode(token);
    const response = await axios.get(`/user/${loginUser.tsid}/activities/likes`);
    return response.data;
};

const MyPageLike = () => {
    const { isLoading, data } = useQuery(
        ['fetchMyPageLikeList'],
        () => fetchMyPageLikeList(),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    const initColumns = [
        {
            accessorKey: "postId",
            header: "게시물 ID",
            size: 200,
        },
        {
            accessorKey: "content",
            header: "내용",
            size: 400,
        },
    ];

    return (
        <PageLayout>
            <SearchCardBox>
                <UserProfile page={"mypage"}/>
            </SearchCardBox>
            <MyPageTabLayout currentTabKey={"like"} >
                <TableBackGroundCard>
                    <FlexibleTable initColumns={initColumns} data={data?.likes || []} isLoading={isLoading} />
                </TableBackGroundCard>
            </MyPageTabLayout>
        </PageLayout>
    )
}

export default MyPageLike;