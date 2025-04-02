import SearchCardBox from "../../../shared/SearchCardBox";
import PageLayout from "../../../layout/page/PageLayout";
import {useQuery} from "react-query";
import axios from "axios";
import UserProfile from "../share/UserProfile";
import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import {useNavigate} from "react-router-dom";

const fetchMyPageLikeList = async () => {
    const response = await axios.get(`/user/{userId}/activities/likes`);
    return response.data;
};

const MyPageModify = () => {
    const navigate = useNavigate();
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
            <Card className="p-5">
                    <Card.Body>
                        <Form>
                            <Form.Label>
                                email
                            </Form.Label>
                            <Form.Control
                                plaintext
                                value={"123@123"}
                                readOnly
                            />
                            <Form.Label>
                                닉네임
                            </Form.Label>
                            <Form.Control
                                type="text"
                                value={""}
                                className={"w-75"}
                            />
                            <Form.Label>
                                소개글
                            </Form.Label>
                            <Form.Control
                                as="textarea"
                                style={{ height: 'unset' }}
                                rows={3}
                                className={"w-75"}
                            />
                            <Form.Label>
                                웹페이지
                            </Form.Label>
                            <Form.Control
                                type="text"
                                value={""}
                                className={"w-75"}
                            />
                        </Form>
                    </Card.Body>
                    <Card.Footer className={"mt-5"}>
                        <Stack direction={"horizontal"} gap={3} >
                            <Button variant={'primary'} className={"ms-auto"} size={"md"}>
                                수정
                            </Button>
                            <Button variant={'secondary'} size={"md"} >
                                취소
                            </Button>
                        </Stack>
                    </Card.Footer>
                </Card>
                <Card className="p-5">
                    <Card.Body>
                        <Form>
                            <Form.Label>
                                기존 비밀번호
                            </Form.Label>
                            <Form.Control
                                type="text"
                                value={""}
                                className={"w-75"}
                            />
                            <Form.Label>
                                비밀번호 변경
                            </Form.Label>
                            <Form.Control
                                type="text"
                                value={""}
                                className={"w-75"}
                            />
                        </Form>
                    </Card.Body>
                    <Card.Footer className={"mt-5"}>
                        <Stack direction={"horizontal"} gap={3} >
                            <Button variant={'primary'} className={"ms-auto"} size={"md"}>
                                수정
                            </Button>
                            <Button variant={'secondary'} size={"md"} >
                                취소
                            </Button>
                        </Stack>
                    </Card.Footer>
                </Card>
        </PageLayout>
    )
}

export default MyPageModify;