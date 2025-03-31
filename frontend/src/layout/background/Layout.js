import Header from "./Header";
import {Outlet} from "react-router-dom";

const Layout = () => {
    return (
        <div className={"kw"}>
            <Header />
            <Outlet />
        </div>
    );
}

export default Layout;