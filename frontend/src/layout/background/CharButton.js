import ChatRoomList from "./ChatRoomList";
import {useState} from "react";
import ChatPopup from "../../shared/DraggablePopup";

const ChatButton = () => {
    const [isOpen, setOpen] = useState(false);
    const [openChats, setOpenChats] = useState([]);

    const openChatRoom = (room) => {
        console.log('openChatRoom param : ', room)
        setOpenChats((prev) => {
            if (prev.find((c) => c.id === room.id)) return [...prev]; // 중복 방지
            return [...prev, room];
        });
    };

    const closeChatRoom = (id) => {
        setOpenChats((prev) => prev.filter((room) => room.id !== id));
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