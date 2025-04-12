import PageLayout from "../../layout/page/PageLayout";
import {
    Col,
    Row,
} from "react-bootstrap";
import {useQuery} from "react-query";
import axios from "axios";
import {useState} from "react";
import {useLocation} from "react-router-dom";
import DetailLeftParts from "./DetailLeftParts";
import DetailRightParts from "./DetailRightParts";

const fetchPost = async (postId) => {
    const response = await axios.get(`/posts/${postId}`);
    return response.data;
}

const PostDetail = () => {
    const location = useLocation();
    const pathSegments = location.pathname.split("/").filter(Boolean);
    const postId = pathSegments[pathSegments.length - 1];
    const { isLoading, data, refetch } = useQuery(
        ['fetchPost'],
        () => fetchPost(postId),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    return (
        <PageLayout>
            <Row>
                <Col md={6} >
                    <DetailLeftParts post={data} postRefetch={refetch} />
                </Col>
                <Col md={6} >
                    <DetailRightParts post={data} />
                </Col>
            </Row>
        </PageLayout>
    );
}

export default PostDetail;