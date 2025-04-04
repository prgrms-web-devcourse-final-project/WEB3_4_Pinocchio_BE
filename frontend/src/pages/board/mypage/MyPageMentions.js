import {useQuery} from "react-query";
import axios from "axios";
import PageLayout from "../../../layout/page/PageLayout";
import SearchCardBox from "../../../shared/SearchCardBox";
import MyPageTabLayout from "./MyPageTabLayout";
import TableBackGroundCard from "../../../shared/TableBackGroundCard";
import FlexibleTable from "../../../shared/table/FlexibleTable";
import UserProfile from "../share/UserProfile";

const fetchMyPageMentionsList = async (userId) => {
    const response = await axios.get(`/user/{userId}/activities/mentions`);
    return response.data;
};


const MyPageMentions = () => {
    const userId = 1;
    const { isLoading, data } = useQuery(
        ['fetchMyPageMentionsList'],
        () => fetchMyPageMentionsList(userId),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    const initColumns = [
        {
            accessorKey: "postId",
            header: "게시물 ID",
            size: 200,
        },
        {
            accessorKey: "userId",
            header: "유저 ID",
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
            <MyPageTabLayout currentTabKey={"mentions"} >
                <TableBackGroundCard>
                    <FlexibleTable initColumns={initColumns} data={data?.mention || []} isLoading={isLoading} />
                </TableBackGroundCard>
            </MyPageTabLayout>
        </PageLayout>
    )
}

export default MyPageMentions;