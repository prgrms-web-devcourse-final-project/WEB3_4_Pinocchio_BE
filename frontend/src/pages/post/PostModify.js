import PageLayout from "../../layout/page/PageLayout";
import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import {useLocation, useNavigate} from "react-router-dom";
import {useState} from "react";
import {useMutation} from "react-query";
import axios from "axios";
import useConfirm from "../../hooks/useConfirm";

const PostModify = () => {
    const location = useLocation();
    const post = location.state || {};
    console.log('post: ', post)
    const [previewUrl, setPreviewUrl] = useState(post.imageUrls[0]);
    const [postVisibility, setPostVisivility] = useState(
        post.visibility.toUpperCase() === "PUBLIC" ? false : true);
    const navigate = useNavigate();
    const {openConfirm} = useConfirm();
    const [content, setContent] = useState(post.content);

    const updateMutation = useMutation((updateData) => axios.patch(`/posts/modify`, updateData), {
        onSuccess: (param) => {
            navigate(`/post/detail/${post.postId}`);
        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });

    const handleSaveClick = () => {
        const updateData = {
            "postId": post.postId,
            "tsid": post.tsId,
            "content": content,
            "visibility": postVisibility ? "private" : "public"
        }
        updateMutation.mutate(updateData);
    }

    return (
        <PageLayout>
            <Row className="mt-5">
                <Col>
                    <Card className="h-100">
                        <Card.Header>
                            <h5>글 내용 작성</h5>
                        </Card.Header>
                        <Card.Body className="py-6 px-6">
                            <Row className="g-6">
                                <Col>
                                    <div>
                                        <img src={previewUrl}
                                             alt="사진을 올려주세요"
                                             style={{ width: "100%", minHeight: "300px", objectFit: 'cover' }}
                                        />
                                    </div>
                                </Col>
                                <div className="col-12">
                                    <Form.Label>
                                        <small style={{marginLeft: '15px'}}>나의 스토리</small>
                                    </Form.Label>
                                    <div style={{ position: "relative" }}>
                                        <Form.Control
                                            as="textarea"
                                            style={{ height: 'unset' }}
                                            rows={3}
                                            value={content}
                                            onChange={(e) => setContent(e.target.value)}
                                        />
                                    </div>
                                </div>
                            </Row>
                        </Card.Body>
                        <Card.Footer>
                            <Stack gap={3} direction={"horizontal"}>
                                <Form.Check
                                    className={"p-2"}
                                    type="switch"
                                    label={<span style={{ marginLeft: '8px' }}>게시글 숨기기 여부</span>}
                                    checked={postVisibility}
                                    onChange={() => setPostVisivility(prev => !prev)}
                                />
                                <div className="ms-auto kw-button p-3">
                                    <Button variant={'secondary'} onClick={() => navigate(-1)}>
                                        취소
                                    </Button>
                                    <Button variant={'primary'} onClick={handleSaveClick}>
                                        저장
                                    </Button>
                                </div>
                            </Stack>
                        </Card.Footer>
                    </Card>
                </Col>
            </Row>
        </PageLayout>
    )
};

export default PostModify;