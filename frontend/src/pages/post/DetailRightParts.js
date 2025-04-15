import {Button, Card, Form, Stack} from "react-bootstrap";
import axios from "axios";
import {useMutation, useQuery} from "react-query";
import useConfirm from "../../hooks/useConfirm";
import {useEffect, useState} from "react";
import {dateFormat} from "../../utils/utils";
import {jwtDecode} from "jwt-decode";

const fetchPostComments = async (postId) => {
    const response = await axios.get(`/comments/${postId}`);
    return response.data;
}

const DetailRightParts = ({ post }) => {
    const token = localStorage.getItem('token');
    const loginUser = jwtDecode(token);
    const {openConfirm} = useConfirm();
    const [newComment, setNewComment] = useState('');
    const [editComment, setEditComment] = useState('');
    const [comments, setComments] = useState();
    const { isLoading, isFetching, data, refetch } = useQuery(
        ['fetchPostComments', post?.postId],
        () => fetchPostComments(post.postId),
        { enabled: !!post, keepPreviousData: true, refetchOnWindowFocus: false}
    );

    const commentUpdateMutation = useMutation((updateData) => axios.put(`/comments`, updateData), {
        onSuccess: (param) => {
            setEditComment('');
            refetch()
        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });

    const commentLikeMutation = useMutation(({postId, commentId}) => axios.post(`/comments/${commentId}/like`, {postId}), {
        onSuccess: (param) => {
            refetch()
        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });

    const commentDeleteMutation = useMutation((deleteData) => axios.delete(`/comments`,
        { data: deleteData }), {
        onSuccess: (param) => {
            refetch()
        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });

    useEffect(() => {
        if (data) {
            const enhancedComments = data.comments.map(item => ({
                ...item,
                isEditorMode: false
            }));
            setComments(enhancedComments);
        }
    }, [data]);

    const handleEditCommentClick = (comment) => {
        if (comment.isEditorMode) { //댓글 수정을 끝내고 저장 버튼을 눌렀을 때
            setComments((prevState) => {
                return prevState.map((item) => {
                    if (item.id === comment.id) {
                        return { ...item, isEditorMode: !item.isEditorMode, content: editComment };
                    } else {
                        return item;
                    }
                })
            })
            const updateData = {
                postId: comment.postId,
                commentId: comment.id,
                content: editComment
            }
            commentUpdateMutation.mutate(updateData);
        } else { //수정 버튼을 눌렀을 때
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
        const deleteData = {
            postId: comment.postId,
            commentId: comment.id,
            action: "SOFT_DELETED"
        }
        openConfirm({
            title: '댓글을 삭제하시겠습니까?',
            html: <pre>삭제된 댓글은 복구할 수 없습니다.</pre>,
            callback: () => commentDeleteMutation.mutate(deleteData)
        })
    }

    const handleCommentsLikeClick = (comment) => {
        const likeData = {
            postId: comment.postId,
            commentId: comment.id
        }
        commentLikeMutation.mutate(likeData);
    }

    return (
        <Card style={{height: "100%"}}>
            <Stack gap={3}>
                <div className="p-2" style={{ height: "10%" }}>{post?.content}</div>
                <hr/>
                <div className="p-2" style={{ height: "90%" }}>
                    <strong>댓글</strong>
                    <div className="kw-view-comment m-0">
                        {comments ? comments.map((comment) => {
                            return <div key={comment.createdAt} className="kw-view-comment-item">
                                <Stack gap={2} direction={"horizontal"}>
                                    {comment.isEditorMode ?
                                        <Form.Control style={{ height: "30px", width: "78%" }}
                                                      type="text" value={editComment}
                                                      onChange={(e) => setEditComment(e.target.value)}
                                        /> : <div>{comment.content}</div>}
                                    {loginUser.tsid === comment.userId &&
                                        <div className="ms-auto d-inline-flex gap-2">
                                        <span className="text-gray-200 cursor-pointer"
                                              onClick={() => handleEditCommentClick(comment)}>
                                            {comment.isEditorMode ? "저장" : "수정"}
                                        </span>
                                        <span className="text-gray-200 cursor-pointer"
                                              onClick={() => handleDeleteCommentClick(comment)}>
                                            삭제
                                        </span>
                                    </div>}
                                </Stack>
                                <Stack gap={2} direction={"horizontal"}>
                                    <span className={`ico-like-${comment.liked ? 'fill' : 'empty'} cursor-pointer`}
                                          onClick={() => handleCommentsLikeClick(comment)}
                                    /><span>{comment.likes}</span>
                                    <span className="ms-auto">
                                                {comment.userId}
                                            </span>|
                                    <span>{dateFormat(comment.createdAt, "yyyy-MM-dd")}</span>
                                </Stack>
                            </div>
                        }) : <div className='text-center'>댓글이 존재하지 않습니다.</div>}

                    </div>
                </div>
                <Form onSubmit={(e) => {
                    e.preventDefault();
                    const createData = {
                        postId: post.postId,
                        content: newComment,
                        parentCommentId: null
                    }
                    axios.post(`/comments`, createData).then((response) => {
                        setNewComment('');
                        refetch();
                        console.log(response.data)
                    });
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
    )
};

export default DetailRightParts;