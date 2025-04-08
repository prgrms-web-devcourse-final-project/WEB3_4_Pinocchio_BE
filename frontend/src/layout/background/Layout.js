import Header from "./Header";
import {Outlet} from "react-router-dom";
import Footer from "./Footer";
import ChatButton from "./CharButton";

const Layout = () => {
    return (
        <div className={"kw"}>
            <Header />
            <Outlet />
            <Footer />
            <ChatButton />
        </div>
    );
}

export default Layout;