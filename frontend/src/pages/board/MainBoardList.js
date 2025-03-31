import PageLayout from "../../layout/page/PageLayout";
import {Button, Col, Form, Row, Stack} from "react-bootstrap";
import profile from "../../assets/images/sample_profile.png"
import BoardPost from "./BoardPost";
import SearchCardBox from "../../shared/SearchCardBox";
import {useNavigate} from "react-router-dom";
import {useInfiniteQuery} from "react-query";
import axios from "axios";

const apiEndpoint = 'https://642d3bd5-7f11-4eef-9a2a-2dd110822440.mock.pstmn.io/api/posts/search';
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
            <SearchCardBox
                useTooltip={false}
            >
                <Form >
                    <Row>
                        <Col md={3}>
                            <img src={profile} style={{ width: "150px", height: "140px" }}/>
                        </Col>
                        <Col >
                            <Stack direction={"horizontal"} gap={3} >
                                <h4>Jake Sully</h4>
                                <Button className={"ms-auto"} size={"md"} onClick={() => navigate("/board/new")}>UPLOAD</Button>
                                <Button size={"md"}>MYPAGE</Button>
                            </Stack>
                            <Row>
                                <Col className={"mt-4"}>안녕하세요.피노키오에 오신것을 환영합니다!</Col>
                            </Row>
                        </Col>
                    </Row>
                </Form>
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