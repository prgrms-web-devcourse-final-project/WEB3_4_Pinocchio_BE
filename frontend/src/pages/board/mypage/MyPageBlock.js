import {useMutation, useQuery, useQueryClient} from "react-query";
import axios from "axios";
import PageLayout from "../../../layout/page/PageLayout";
import SearchCardBox from "../../../shared/SearchCardBox";
import MyPageTabLayout from "./MyPageTabLayout";
import TableBackGroundCard from "../../../shared/TableBackGroundCard";
import FlexibleTable from "../../../shared/table/FlexibleTable";
import UserProfile from "../share/UserProfile";
import {Button, Stack} from "react-bootstrap";
import useConfirm from "../../../hooks/useConfirm";

const fetchMyPageBlockList = async (userId) => {
    const response = await axios.get(`/block/users`);
    return response.data;
};


const MyPageBlock = () => {
    const userId = 1;
    const { isLoading, data, refetch } = useQuery(
        ['fetchMyPageBlockList'],
        () => fetchMyPageBlockList(userId),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );
    const { openConfirm } = useConfirm();
    const queryClient = useQueryClient();
    const deleteMutation = useMutation((userId) => axios.delete(`/block/user/{userId}`), {
        onSuccess: () => {
            console.log('요청 성공');
            queryClient.invalidateQueries('fetchMyPageBlockList')
        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });

    const initColumns = [
        {
            accessorKey: "blockedUserId",
            header: "유저 ID",
            cell: ({getValue}) => <Stack direction={"horizontal"} gap={3} >
                {getValue()}
                <Button className={"ms-auto"}
                        size={"md"}
                        variant={"danger"}
                        onClick={() => deleteMutation.mutate(getValue())}
                >해제</Button>
            </Stack>
        },
    ];

    return (
        <PageLayout>
            <SearchCardBox>
                <UserProfile page={"mypage"}/>
            </SearchCardBox>
            <MyPageTabLayout currentTabKey={"block"} >
                <TableBackGroundCard>
                    <FlexibleTable initColumns={initColumns} data={data?.data || []} isLoading={isLoading} />
                </TableBackGroundCard>
            </MyPageTabLayout>
        </PageLayout>
    )
}

export default MyPageBlock;