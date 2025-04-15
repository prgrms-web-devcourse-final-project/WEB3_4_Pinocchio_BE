import {Button, Card, Col, Image, Row, Stack} from "react-bootstrap";
import noImage from "../../assets/images/no_image.png";
import {buildQuery, dateFormat} from "../../utils/utils";
import {useState} from "react";
import {useMutation} from "react-query";
import axios from "axios";
import {Link, useNavigate} from "react-router-dom";
import {jwtDecode} from "jwt-decode";
import useConfirm from "../../hooks/useConfirm";

const DetailLeftParts = ({ post, postRefetch }) => {
    const token = localStorage.getItem('token');
    const loginUser = jwtDecode(token);
    const [isPostLike, setPostLike] = useState(false);
    const {openConfirm} = useConfirm();
    const navigate = useNavigate();
    const likeMutation = useMutation((postId) => axios.post(`/posts/like/${postId}`), {
        onSuccess: (param) => {
            console.log(param);
            postRefetch()
        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });

    const deleteMutation = useMutation((postId) => axios.delete(`/posts/delete/${postId}`), {
        onSuccess: (param) => {
            console.log(param);
            openConfirm({
                title: '게시글이 삭제되었습니다.',
                callback: () => navigate('/post/list'),
                showCancelButton: false
            })
        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });

    const handlePostLikeClick = () => {
        setPostLike(prev => !prev);
        likeMutation.mutate(post.postId);
    }

    const handlePostDeleteClick = () => {
        openConfirm({
            title: '게시물을 삭제하시겠습니까?',
            callback: () => deleteMutation.mutate(post.postId)
        })
    }

    return (
        <Card style={{minWidth: "300px"}} >
            <Card.Header style={{ minHeight: "50px" }}>
                <Row>
                    <Col md={2}>
                        <Image src={post?.profileImage ? post.profileImage : noImage} rounded fluid />
                    </Col>
                    <Col md={6} >
                        <Link to={`/post/list${buildQuery({ type: "users", query: post?.nickname })}`}>{post?.tsid}</Link>
                        {post?.tsid === loginUser.tsid ? <Stack direction={"horizontal"} gap={4}>
                            <Button size={"sm"}
                                    onClick={() => navigate('/post/modify', {state: post})}>수정</Button>
                            <Button size={"sm"} onClick={handlePostDeleteClick}>삭제</Button>
                        </Stack> : null}
                    </Col>
                    <Col md={4}>
                        {dateFormat(post?.createdAt, "yyyy-MM-dd")}
                        <Button size={"sm"} onClick={() => navigate('/post/list')}>목록</Button>
                    </Col>
                </Row>
            </Card.Header>
            <Card.Body>
                <Image src={post?.imageUrls[0]} rounded fluid />
            </Card.Body>
            <hr/>
            <Card.Footer className="d-flex align-items-center gap-2" style={{ minHeight: "50px" }}>
                <span className={`ico-like-${post?.liked ? 'fill' : 'empty'} cursor-pointer`}
                      onClick={handlePostLikeClick}
                />
                <span>{post?.likes}</span>
            </Card.Footer>
        </Card>
    )
}

export default DetailLeftParts;