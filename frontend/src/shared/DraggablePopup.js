import Draggable from "react-draggable";
import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import styles from "./DraggablePopup.module.scss";


const ChatPopup = ({ room, onClose }) => {
    return (
        <Draggable handle=".popup-header">
            <Card className={styles.chatCard}
                style={{
                    zIndex: 999,
                    top: 50 + room.id * 30,
                    left: 100 + room.id * 30
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
                    <strong>{room.id}</strong>
                    <button style={{ float: "right" }} onClick={() => onClose(room.id)}>
                        ✕
                    </button>
                </Card.Header>
                <Card.Body>
                    <div style={{ padding: "10px" }}>
                        <p>채팅 내용 (room id: {room.id})</p>
                        {/* 여기에 채팅 컴포넌트 연결 가능 */}
                    </div>
                </Card.Body>
                <Card.Footer>
                    <Stack direction={"horizontal"} gap={2} className={"m-2"}>
                        <Form>
                            <Form.Control style={{ height: "30px" }} type="text" />
                        </Form>
                        <Button size={"sm"}>전송</Button>
                    </Stack>
                </Card.Footer>
            </Card>
        </Draggable>
    );
};

export default ChatPopup;