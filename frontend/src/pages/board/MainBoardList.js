import PageLayout from "../../layout/page/PageLayout";
import {Button, Row} from "react-bootstrap";
import BoardPost from "./BoardPost";
import SearchCardBox from "../../shared/SearchCardBox";
import {useNavigate} from "react-router-dom";
import UserProfile from "./share/UserProfile";
import {useInfiniteQuery} from "react-query";
import axios from "axios";
import {buildQuery} from "../../utils/utils";

const fetchBoardList = async (pageParam) => {
    const params = { cursor: pageParam.pageParam };
    console.log('pageParam: ', pageParam, pageParam.pageParam)

    const response = await axios.get(`/search${buildQuery(params)}`);
    return response.data.data.users;
}

const MainBoardList = () => {
    const navigate = useNavigate();
    const { data, fetchNextPage, hasNextPage } = useInfiniteQuery({
        queryKey: ['boardList'],
        queryFn: fetchBoardList,
        getNextPageParam: (lastData, allData) => {
            console.log('lastData: ', lastData)
            if (lastData?.hasNext) {
                console.log('nextCursor: ', lastData.nextCursor)

                return lastData.nextCursor;
            } else {
                return undefined;
            }
        },
    });

    console.log("데이터", data)

    return (
        <PageLayout>
            <SearchCardBox>
                <UserProfile page={"main"}/>
                {/*<Button onClick={() => fetchNextPage()}>TEST</Button>*/}
            </SearchCardBox>
            <Row className="mb-4">
                {data?.pages.map((page) => {
                    page.map((user) => {
                        console.log(user);
                    })
                })}
            </Row>
            <Row className="mb-4">
                <BoardPost/>
                <BoardPost/>
                <BoardPost/>
            </Row>
        </PageLayout>

    )

}

export default MainBoardList;