import {Card, Col, Image, ListGroup, Row, Stack} from "react-bootstrap";
import sampleProfile from "../../assets/images/sample_profile.png";
import {jwtDecode} from "jwt-decode";
import axios from "axios";
import {useQuery} from "react-query";

const fetchchatList = async () => {
    const token = localStorage.getItem('token');
    const loginUser = jwtDecode(token);
    const response = await axios.get(`/chat/list`);
    return response.data;
}

const ChatRoomList = ({isOpen, handleCloseClick, openChatRoom}) => {
    const { isLoading, data } = useQuery(
        ['fetchchatList'],
        // () => fetchchatList(),
        () => {},
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

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
                {[{roomId: 1}, {roomId: 2}, {roomId: 3}].map(room => {
                    return <ListGroup.Item action>
                        <Row className={"cursor-pointer"} onClick={() => openChatRoom(room)}>
                            <Col xs={3} ><Image src={sampleProfile} rounded fluid /></Col>
                            <Col xs={"auto"} >userId<br />
                                <div className={"text-gray-200"}>마지막 챗 내용</div>
                            </Col>
                        </Row>
                    </ListGroup.Item>
                })}
            </ListGroup>
        </Card>
    );
}

export default ChatRoomList;