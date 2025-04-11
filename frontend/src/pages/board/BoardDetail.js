import PageLayout from "../../layout/page/PageLayout";
import {
    Button,
    Card,
    Carousel,
    Col,
    Form,
    Image,
    Row,
    Stack
} from "react-bootstrap";
import sampleProfile from "../../assets/images/sample_profile.png";
import {useMutation} from "react-query";
import axios from "axios";
import useConfirm from "../../hooks/useConfirm";
import {useState} from "react";
const BoardDetail = () => {
    const {openConfirm} = useConfirm();
    const [newComment, setNewComment] = useState('');
    const [editComment, setEditComment] = useState('');
    const likeMutation = useMutation(() => axios.post(`/posts/{postId}/like`), {
        onSuccess: (param) => {

        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });
    const [post, setPost] = useState(
        {
            content: "게시글 샘플입니다",
            likes: 3,
            isLike: false
        }
    );
    const [comments, setComments] = useState([
        {
            id: "0123",
            userId: "0KBKT85AGSNTB",
            content: "댓글 샘플입니다",
            likes: 1,
            isLike: false,
            isEditorMode: false,
            createAt: '2025.4.11'
        }
    ]);

    const handleEditCommentClick = (comment) => {
        if (comment.isEditorMode) {
            setComments((prevState) => {
                return prevState.map((item) => {
                    if (item.id === comment.id) {
                        return { ...item, isEditorMode: !item.isEditorMode, content: editComment };
                    } else {
                        return item;
                    }
                })
            })
            setEditComment('');
        } else {
            setEditComment(comment.content)
            setComments((prevState) => {
                return prevState.map((item) => {
                    if (item.id === comment.id) {
                        return { ...item, isEditorMode: !item.isEditorMode };
                    } else {
                        return item;
                    }
                })
            })
        }
    }

    const handleDeleteCommentClick = (comment) => {
        openConfirm({
            title: '댓글을 삭제하시겠습니까?',
            html: <pre>삭제된 댓글은 복구할 수 없습니다.</pre>,
            callback: () => console.log(comment.id, '댓글 삭제')
        })
    }

    return (
        <PageLayout>
            <Row>
                <Col md={6} >
                    <Card style={{minWidth: "300px"}} >
                        <Card.Header style={{ minHeight: "70px" }}>
                            <Row>
                                <Col md={3}>
                                    <Image src={sampleProfile} rounded fluid />
                                </Col>
                                <Col md={4} >
                                    userId
                                </Col>
                                <Col md={"auto"} className={"ms-auto"}>
                                    2015~
                                </Col>
                            </Row>
                        </Card.Header>
                        <Card.Body>
                            <Image src={sampleProfile} rounded fluid />
                        </Card.Body>
                        <hr/>
                        <Card.Footer className="d-flex align-items-center gap-2" style={{ minHeight: "50px" }}>
                            <span className={`ico-like-${post.isLike ? 'fill' : 'empty'} cursor-pointer`}
                                  onClick={() => setPost({...post, isLike: !post.isLike})}
                            />
                            <span>{post.isLike ? 4 : 3}</span>
                        </Card.Footer>
                    </Card>
                </Col>
                <Col md={6} >
                    <Card style={{height: "100%"}}>
                        <Stack gap={3}>
                            <div className="p-2" style={{ height: "10%" }}>{post.content}</div>
                            <hr/>
                            <div className="p-2" style={{ height: "90%" }}>
                                <strong>댓글</strong>
                                <div className="kw-view-comment m-0">
                                    {comments.map((comment) => {
                                        return <div className="kw-view-comment-item">
                                            <Stack gap={2} direction={"horizontal"}>
                                                {comment.isEditorMode ?
                                                    <Form.Control style={{ height: "30px", width: "80%" }}
                                                                  type="text" value={editComment}
                                                                  onChange={(e) => setEditComment(e.target.value)}
                                                    /> : <div>{comment.content}</div>}
                                                <div className="ms-auto d-inline-flex gap-2">
                                                    <span className="text-gray-200 cursor-pointer"
                                                          onClick={() => handleEditCommentClick(comment)}>
                                                        {comment.isEditorMode ? "저장" : "수정"}
                                                    </span>
                                                    <span className="text-gray-200 cursor-pointer"
                                                          onClick={() => handleDeleteCommentClick(comment)}>
                                                        삭제
                                                    </span>
                                                </div>
                                            </Stack>
                                            <Stack gap={2} direction={"horizontal"}>
                                            <span className={`ico-like-${comment.isLike ? 'fill' : 'empty'} cursor-pointer`}
                                                  onClick={() => setComments((prevState) => {
                                                      return prevState.map((item) => {
                                                          if (item.id === comment.id) {
                                                              return { ...item, isLike: !item.isLike };
                                                          } else {
                                                              return item;
                                                          }
                                                      })
                                                  })}
                                            /><span>{comment.isLike ? 2 : 1}</span>
                                                <span className="ms-auto">
                                                {comment.userId}
                                            </span>|
                                                <span>{comment.createAt}</span>
                                            </Stack>
                                        </div>
                                    })}
                                    {/*<div className='text-center'>(댓글없을 땐)댓글이 존재하지 않습니다.</div>*/}
                                </div>
                            </div>
                            <Form onSubmit={(e) => {
                                e.preventDefault();
                                console.log('newMessage: ', newComment)
                            }}>
                                <Stack direction={"horizontal"} gap={2} className={"m-2"}>
                                    <Form.Control style={{ height: "35px" }}
                                                  type="text"
                                                  value={newComment}
                                                  onChange={(e) => setNewComment(e.target.value)}
                                    />
                                    <Button size={"md"} type={"submit"}>전송</Button>
                                </Stack>
                            </Form>
                        </Stack>
                    </Card>
                </Col>
            </Row>
        </PageLayout>
    );
}

export default BoardDetail;