import SearchCardBox from "../../shared/SearchCardBox";
import {Button, Col, Form, Row, Stack} from "react-bootstrap";
import profile from "../../assets/images/sample_profile.png";
import PageLayout from "../../layout/page/PageLayout";
import {Link, useNavigate} from "react-router-dom";
import {useMutation, useQuery} from "react-query";
import axios from "axios";
import FlexibleTable from "../../shared/table/FlexibleTable";
import TableBackGroundCard from "../../shared/TableBackGroundCard";
import useConfirm from "../../hooks/useConfirm";
import Spinner from "../../shared/Spinner";
import {useState} from "react";
import MyPageTabLayout from "./MyPageTabLayout";

const fetchMyPageLikeList = async () => {
    const response = await axios.get(`/user/{userId}/activities/likes`);
    return response.data;
};

const fetchUser = async () => {
    const response = await axios.get(`/user/{userId}`);
    return response.data;
};

const MyPage = () => {
    const navigate = useNavigate();
    const [spinnerLoading, setSpinerLoading] = useState(false);
    const { openConfirm } = useConfirm();
    const { isLoading, data } = useQuery(
        ['fetchMyPageLikeList'],
        () => fetchMyPageLikeList(),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    const { isLoading: userLoading, data: user } = useQuery(
        ['fetchUser'],
        () => fetchUser(),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    const deleteMutation = useMutation(() => axios.delete(`/user/{userId}`), {
        onSuccess: (param) => {
            openConfirm({
                title: "회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다."
                , callback: () => navigate("/login")
                , showCancelButton: false
            });
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
            accessorKey: "postId",
            header: "게시물 ID",
            size: 200,
            cell: ({ getValue }) => getValue()
        },
        {
            accessorKey: "content",
            header: "내용",
            size: 400,
        },
    ];

    const handleDeleteUser = () => {
        openConfirm({
            title: "계정 삭제 안내",
            html: "계정을 정말로 삭제하시겠습니까? 삭제된 계정은 복구할 수 없습니다.",
            callback: () => {
                deleteMutation.mutate()
            }
        })
    }

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
                                {userLoading || <h4>{user?.data.nickname} ({user?.data.name})</h4>}
                                <Button className={"ms-auto"} size={"md"}>수정</Button>
                                <Button size={"md"} onClick={handleDeleteUser}>계정 삭제</Button>
                            </Stack>
                            <Row>
                                <Col className={"mt-4"}>{user?.data.bio}</Col>
                            </Row>
                        </Col>
                    </Row>
                </Form>
            </SearchCardBox>
            <MyPageTabLayout currentTabKey={"like"} >
                <TableBackGroundCard>
                    <FlexibleTable initColumns={initColumns} data={data?.likes || []} isLoading={isLoading} />
                </TableBackGroundCard>
            </MyPageTabLayout>
            <Spinner show={spinnerLoading} />
        </PageLayout>
    )
}

export default MyPage;