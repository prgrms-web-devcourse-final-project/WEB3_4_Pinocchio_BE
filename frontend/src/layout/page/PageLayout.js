const PageLayout = ({children}) => {
    return (
        <div className="kw-page">
            <div className="kw-contents">
                <div className="kw-inner">
                {children}
                </div>
            </div>
        </div>
    );
}

export default PageLayout;