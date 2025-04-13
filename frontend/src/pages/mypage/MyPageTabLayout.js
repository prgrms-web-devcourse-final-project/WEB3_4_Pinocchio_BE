import {useLocation, useNavigate} from "react-router-dom";
import TabLayout from "../../shared/TabLayout";

const tabMenu = [
    { key: "like", title: "좋아요 목록" }
    , { key: "comment", title: "내 댓글" }
    , { key: "follower", title: "팔로우 목록" }
    // , { key: "mentions", title: "멘션 조회" }
    , { key: "block", title: "차단 목록" }
]
const MyPageTabLayout = ({ children, currentTabKey }) => {
    const navigate = useNavigate();
    const location = useLocation();

    //탭 변경시 동작함수
    const handleTabChange = (key) => {
        // 마지막 url을 탭 메뉴로 이동
        const currentPathArray = location.pathname.split("/");
        currentPathArray[currentPathArray.length - 1] = key; // 마지막 path 변경
        const targetUrl = currentPathArray.join("/");
        navigate(targetUrl); // 변경된 URL로 이동
    }

    return <TabLayout currentTabKey={currentTabKey} tabMenu={tabMenu} handleTabChange={handleTabChange}>
        {children}
    </TabLayout>
}

export default MyPageTabLayout;