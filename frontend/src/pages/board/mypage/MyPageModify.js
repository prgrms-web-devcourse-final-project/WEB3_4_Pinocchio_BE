import PageLayout from "../../../layout/page/PageLayout";
import ProfileEditCard from "./ProfileEditCard";
import ChangePasswordCard from "./ChangePasswordCard";


const MyPageModify = () => {
    return (
        <PageLayout>
            <ProfileEditCard />
            <ChangePasswordCard />
        </PageLayout>
    )
}

export default MyPageModify;