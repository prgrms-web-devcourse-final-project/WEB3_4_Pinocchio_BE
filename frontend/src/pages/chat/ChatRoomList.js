import {Card, Col, Image, ListGroup, Row, Stack} from "react-bootstrap";
import noImage from "../../assets/images/no_image.png";
import axios from "axios";
import {useInfiniteQuery, useQuery} from "react-query";
import {useEffect, useState} from "react";
import {useInView} from "react-intersection-observer";

const fetchChatRoomList = async (pageParam) => {
    const response = await axios.get(`/chat/list`);
    return response.data.data;
}

const ChatRoomList = ({isOpen, handleCloseClick, openChatRoom}) => {
    const [chatList, setChatList] = useState([]);
    const { ref, inView } = useInView({
        threshold: 0.5, // 화면의 50%가 보일 때 감지
    });
    const { data, fetchNextPage, hasNextPage } = useInfiniteQuery({
        queryKey: ['ChatRoomList'],
        queryFn: (pageParam) => fetchChatRoomList(pageParam),
        getNextPageParam: (lastData, allData) => {
            if (lastData?.hasNext) {
                return lastData.nextCursor;
            } else {
                return undefined;
            }
        },
    });

    useEffect(() => {
        if (data) {
            const chatList = [];
            data?.pages.map((page) => {
                page.chatrooms.map((room) => {
                    chatList.push(room);
                })
            })
            setChatList(chatList);
        }
    }, [data])

    useEffect(() => {
        if (inView) {
            fetchNextPage()
        }
    }, [inView]);

    return (
        isOpen &&
        <Card bg={"light"} style={{
            height: "25rem",
            width: "18rem",
            border: "1px solid black"
        }}>
            <Card.Header>
                <Stack direction={"horizontal"} gap={2} >
                    <span>채팅방 목록</span>
                    <span className={"ico-close ms-auto cursor-pointer"} onClick={handleCloseClick}></span>
                </Stack>
            </Card.Header>
            <ListGroup style={{ overflowY: "auto", overflowX: "hidden" }}>
                {chatList.map(room => {
                    return <ListGroup.Item action>
                        <Row className={"cursor-pointer"} onClick={() => openChatRoom(room)}>
                            <Col xs={3} ><Image src={room.targetUserProfileImageUrl ? room.targetUserProfileImageUrl : noImage} rounded fluid /></Col>
                            <Col xs={"auto"} >{room.targetUserNickName}<br />
                                <div className={"text-gray-200"}>{room.lastMessage}</div>
                            </Col>
                        </Row>
                    </ListGroup.Item>
                })}
            </ListGroup>
            <div ref={ref}></div>
        </Card>
    );
}

export default ChatRoomList;