import { Col, Card } from "react-bootstrap";
import React from 'react';
import {useNavigate} from "react-router-dom"


const PostProfile = ({ post }) => {
    const navigate = useNavigate();

    return (
        <Col md={4}>
            <Card style={{marginBottom : '10px', height: "250px"}}
                  className={"cursor-pointer"}
                  onClick={() => navigate(`/post/detail/${post.postId}`)}>
                <img src={post.imageUrl[0]} />
            </Card>
        </Col>
    );
};

export default PostProfile;