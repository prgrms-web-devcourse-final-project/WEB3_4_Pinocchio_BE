import {useQuery} from "react-query";
import axios from "axios";
import PageLayout from "../../../layout/page/PageLayout";
import SearchCardBox from "../../../shared/SearchCardBox";
import MyPageTabLayout from "./MyPageTabLayout";
import TableBackGroundCard from "../../../shared/TableBackGroundCard";
import FlexibleTable from "../../../shared/table/FlexibleTable";
import UserProfile from "../share/UserProfile";
import {jwtDecode} from "jwt-decode";

const fetchMyPageCommentList = async () => {
    const token = localStorage.getItem('token');
    const loginUser = jwtDecode(token);
    const response = await axios.get(`/user/${loginUser.tsid}/activities/comments`);
    return response.data;
};


const MyPageComment = () => {
    const { isLoading, data } = useQuery(
        ['fetchMyPageCommentList'],
        () => fetchMyPageCommentList(),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    const initColumns = [
        {
            accessorKey: "postId",
            header: "게시물 ID",
            size: 200,
        },
        {
            accessorKey: "id",
            header: "댓글 ID",
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
            <MyPageTabLayout currentTabKey={"comment"} >
                <TableBackGroundCard>
                    <FlexibleTable initColumns={initColumns} data={data?.comments || []} isLoading={isLoading} />
                </TableBackGroundCard>
            </MyPageTabLayout>
        </PageLayout>
    )
}

export default MyPageComment;