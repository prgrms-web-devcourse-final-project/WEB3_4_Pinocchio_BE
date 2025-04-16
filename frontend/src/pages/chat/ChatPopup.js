import Draggable from "react-draggable";
import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import styles from "./ChatPopup.module.scss";
import {useQuery} from "react-query";
import {dateFormat} from "../../utils/utils";
import {useState} from "react";
import {jwtDecode} from "jwt-decode";
import axios from "axios";

const fetchchatMessage = async (chatId) => {
    const response = await axios.get(`/chat/${chatId}/messages`);
    console.log('fetchchatMessage: ', response.data.data)
    return response.data.data;
};

const ChatPopup = ({ room, onClose }) => {
    console.log('room: ', room)
    const token = localStorage.getItem('token')
    const loginUser = jwtDecode(token);
    const { isLoading, data, refetch } = useQuery(
        ['fetchchatMessage'],
        () => fetchchatMessage(room.roomId),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );
    const [position] = useState({
        top: Math.floor(Math.random() * 600),
        left: Math.floor(Math.random() * 600),
    })
    const [newMessage, setNewMessage] = useState('');

    return (
        <Draggable handle=".popup-header">
            <Card className={styles.chatCard}
                style={{
                    zIndex: 999,
                    top: 300 - position.top,
                    left: 300 - position.left
                }}
            >
                <Card.Header
                    className="popup-header"
                    style={{
                        padding: "10px",
                        // backgroundColor: "#eee",
                        cursor: "move",
                        borderBottom: "1px solid #ccc",
                    }}
                >
                    <Stack direction={"horizontal"} gap={2} >
                        <strong>Message</strong>
                        <span className={"ico-close ms-auto cursor-pointer"} onClick={() => onClose(room.roomId)}></span>
                    </Stack>
                </Card.Header>
                <Card.Body style={{
                    overflowX: "hidden",
                    overflowY: "auto"
                }}>
                    <div className="kw-chat">
                        <div className="kw-chat__list">
                            {data?.chatMessages.map((message) => {
                                const isMyMessage = message.senderId === loginUser.tsid; // 내 글인지 판별
                                return <div key={message.msgId} className={isMyMessage ? "chat__me" : "chat__other"}>
                                    {/* 채팅 본문*/}
                                    <div className="text mt-1">
                                        <p>{message.content}</p>
                                    </div>
                                    {/*채팅 하단 - 작성 시간*/}
                                    <span className="datetime text-muted small">
                                        {dateFormat(message.createdAt, "yyyy-MM-dd HH:mm")}
                                    </span>
                                </div>
                            })}
                        </div>
                    </div>
                </Card.Body>
                <Card.Footer>
                    <Form onSubmit={(e) => {
                        e.preventDefault();
                    }}>
                        <Stack direction={"horizontal"} gap={2} className={"m-2"}>
                            <Form.Control style={{ height: "30px" }}
                                          type="text"
                                          value={newMessage}
                                          onChange={(e) => setNewMessage(e.target.value)}
                            />
                            <Button size={"sm"} type={"submit"}>전송</Button>
                        </Stack>
                    </Form>
                </Card.Footer>
            </Card>
        </Draggable>
    );
};

export default ChatPopup;