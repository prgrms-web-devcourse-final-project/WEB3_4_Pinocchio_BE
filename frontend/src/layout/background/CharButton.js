import ChatRoomList from "../../pages/chat/ChatRoomList";
import {useState} from "react";
import ChatPopup from "../../pages/chat/ChatPopup";

const ChatButton = () => {
    const [isOpen, setOpen] = useState(false);
    const [openChats, setOpenChats] = useState([]);

    const openChatRoom = (room) => {
        setOpenChats((prev) => {
            if (prev.find((c) => c.roomId === room.roomId)) return [...prev]; // 중복 방지
            return [...prev, room];
        });
    };

    const closeChatRoom = (id) => {
        setOpenChats((prev) => prev.filter((room) => room.roomId !== id));
    };

    const handleCloseClick = () => {
        setOpen(false);
    };

    return <div className={"chat-button-box"} style={{ position: "fixed", right: "15%", bottom: "12%", zIndex: "100" }}>
        <span className={"ico-chat"} onClick={() => setOpen(true)}></span>
        <div style={{ position: "absolute", left: "-300px", top: "-350px" }}>
            <ChatRoomList isOpen={isOpen} handleCloseClick={handleCloseClick} openChatRoom={openChatRoom}/>
            {openChats.map((room, index) => (
                <ChatPopup key={index} room={room} onClose={closeChatRoom} />
            ))}
        </div>
    </div>
}

export default ChatButton;