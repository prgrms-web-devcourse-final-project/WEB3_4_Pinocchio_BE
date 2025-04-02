import PageLayout from "../../layout/page/PageLayout";
import {Row} from "react-bootstrap";
import BoardPost from "./BoardPost";
import SearchCardBox from "../../shared/SearchCardBox";
import {useNavigate} from "react-router-dom";
import UserProfile from "./share/UserProfile";

const MainBoardList = () => {
    const navigate = useNavigate();
    // const { data, fetchNextPage, hasNextPage } = useInfiniteQuery({
    //     queryKey: ['boardList'],
    //     queryFn: async ({ pageParam = 1 }) => { // pageParam의 형식을 직접 지정
    //         const url = pageParam
    //             ? `${apiEndpoint}?next-cursor=${pageParam}`
    //             : apiEndpoint;
    //         const response = await axios.get(url);
    //         return response.data;
    //     },
    //     getNextPageParam: (lastPage) => {
    //         return lastPage.nextCursor === -1 ? undefined : lastPage.nextCursor;
    //     },
    //     initialPageParam: 1
    // });

    // console.log("데이터", data)

    return (
        <PageLayout>
            <SearchCardBox>
                <UserProfile page={"main"}/>
            </SearchCardBox>
            <Row className="mb-4">
                <BoardPost/>
                <BoardPost/>
                <BoardPost/>
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