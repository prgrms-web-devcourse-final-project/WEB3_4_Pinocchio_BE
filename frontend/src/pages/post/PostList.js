import PageLayout from "../../layout/page/PageLayout";
import {Button, Row} from "react-bootstrap";
import PostProfile from "./PostProfile";
import SearchCardBox from "../../shared/SearchCardBox";
import {useNavigate} from "react-router-dom";
import UserProfile from "../share/UserProfile";
import {useInfiniteQuery} from "react-query";
import axios from "axios";
import {buildQuery} from "../../utils/utils";
import {useEffect, useState} from "react";

const fetchBoardList = async (pageParam) => {
    const params = { cursor: pageParam.pageParam };
    const response = await axios.get(`/posts/search${buildQuery(params)}`);
    return response.data;
}

const PostList = () => {
    const navigate = useNavigate();
    const { data, fetchNextPage, hasNextPage } = useInfiniteQuery({
        queryKey: ['boardList'],
        queryFn: fetchBoardList,
        getNextPageParam: (lastData, allData) => {
            if (lastData?.hasNext) {
                console.log('nextCursor: ', lastData.nextCursor)

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
            console.log(postList);
            setPostList(postList);
        }
    }, [data])

    return (
        <PageLayout>
            <SearchCardBox>
                <UserProfile page={"main"}/>
                <Button onClick={() => fetchNextPage()}>TEST</Button>
            </SearchCardBox>
            <Row className="mb-4">
                {postList.map((post) => (<PostProfile post={post} /> ))}
            </Row>
        </PageLayout>
    )

}

export default PostList;