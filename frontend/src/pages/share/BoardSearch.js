import { Button, Form } from "react-bootstrap";
import React, { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import {useQueryParam} from "../../hooks/QueryParam";

const BoardSearch = ({ refetch, type }) => {
    const navigate = useNavigate();
    const [queryParam, setQueryParam] = useQueryParam();
    const [keyword, setKeyword] = useState(queryParam.keyword || "");

    const [isAdmin, setIsAdmin] = useState(false);

    useEffect(() => {
        setQueryParam({ keyword }); // keyword 변경 시 쿼리 파라미터 업데이트
    }, [keyword]);

    const handleWriteClick = () => {
        switch (type) {
            case "notice":
                navigate("/board/notice/write");
                break;
            case "faq":
                navigate("/board/faq/write");
                break;
            case "qna":
                navigate("/board/qna/write");
                break;
            case "data":
                navigate("/board/data-room/write");
                break;
            default:
                navigate("/dashboard/dashboard");
                break;
        }
    };

    const handleReset = () => {
        setKeyword('');
        setQueryParam({}); // URL 쿼리스트링 초기화
    };

    return (
        <div className="d-flex align-items-center justify-content-between" style={{ marginBottom: '20px' }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                <div>
                    <Form.Label className="form-label"></Form.Label>
                    <div className="kw-form-search" style={{ width: "380px" }}>
                        <Form.Control
                            type="text"
                            className="form-control"
                            placeholder="입력"
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                        />
                        <button onClick={refetch}>검색</button>
                    </div>
                </div>
            </div>

            <div>
                {(type !== "notice" && type !== "faq") || isAdmin ? (
                    <Button
                        style={{ minWidth: "70px", padding: "5px 15px", fontSize: "16px", height: "35px", }}
                        variant="primary" onClick={handleWriteClick}>
                        글쓰기
                    </Button>
                ) : null}
            </div>
        </div>
    );
}

export default BoardSearch;
