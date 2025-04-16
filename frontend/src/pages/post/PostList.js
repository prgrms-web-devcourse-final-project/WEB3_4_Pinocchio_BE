import PageLayout from "../../layout/page/PageLayout";
import {Button, Row} from "react-bootstrap";
import PostProfile from "./PostProfile";
import SearchCardBox from "../../shared/SearchCardBox";
import {useLocation, useNavigate} from "react-router-dom";
import UserProfile from "../share/UserProfile";
import {useInfiniteQuery} from "react-query";
import axios from "axios";
import {buildQuery} from "../../utils/utils";
import {useEffect, useState} from "react";
import {useInView} from "react-intersection-observer";
import {useQueryParam} from "../../hooks/QueryParam";

const fetchBoardList = async (pageParam, queryParam) => {
    const params = { cursor: pageParam.pageParam, ...queryParam };
    const response = await axios.get(`/posts/search${buildQuery(params)}`);
    return response.data;
}

const PostList = () => {
    const { ref, inView } = useInView({
        threshold: 0.5, // 화면의 50%가 보일 때 감지
    });
    const location = useLocation();
    const { userId } = location.state || {};
    const [queryParam, setQueryParam] = useQueryParam();
    const { data, fetchNextPage, hasNextPage } = useInfiniteQuery({
        queryKey: ['boardList', queryParam],
        queryFn: (pageParam) => fetchBoardList(pageParam, queryParam),
        getNextPageParam: (lastData, allData) => {
            if (lastData?.hasNext) {
                return lastData.nextCursor;
            } else {
                return undefined;
            }
        },
    });
    const [postList, setPostList] = useState([]);

    useEffect(() => {
        if (data) {
            const postList = [];
            data?.pages.map((page) => {
                page.posts.map((post) => {
                    postList.push(post);
                })
            })
            setPostList(postList);
        }
    }, [data])

    useEffect(() => {
        if (inView) {
            fetchNextPage()
        }
    }, [inView]);

    return (
        <PageLayout>
            <SearchCardBox>
                <UserProfile page={"main"} userId={userId}/>
            </SearchCardBox>
            <Row className="mb-4">
                {postList.map((post) => (<PostProfile post={post} /> ))}
            </Row>
            <div ref={ref}></div>
        </PageLayout>
    )

}

export default PostList;