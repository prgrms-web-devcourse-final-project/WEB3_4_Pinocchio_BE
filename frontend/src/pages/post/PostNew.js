import PageLayout from "../../layout/page/PageLayout";
import React, {useRef, useState} from "react";
import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import noImage from "../../assets/images/no_image.png";
import {isEmptyOrNull} from "../../utils/utils";
import useConfirm from "../../hooks/useConfirm";


const PostNew = () => {
    const fileInputRef = useRef(null);
    const [previewUrl, setPreviewUrl] = useState("");
    const [selectedFile, setSelectedFile] = useState(null);
    const [postInvisibility, setPostInvisivility] = useState(false);
    const {openConfirm} = useConfirm();
    const handleButtonClick = () => {
        fileInputRef.current.click();
    };
    const navigate = useNavigate();
    const [content, setContent] = useState("");

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const imageUrl = URL.createObjectURL(file);
        setPreviewUrl(imageUrl); //미리보기용 파일 url
        setSelectedFile(file); // 실제 업로드용으로 따로 file객체 저장
    };

    const handleSaveClick = () => {
        if (isEmptyOrNull(content) || isEmptyOrNull(previewUrl)) {
            openConfirm({
                title: "이미지 혹은 스토리가 없습니다."
            })
            return;
        }

        const matches = content.match(/([#@][\w가-힣_]+)/g) || [];

        const hashtags = matches.filter(tag => tag.startsWith("#"));
        const mentions = matches.filter(tag => tag.startsWith("@")).map(tag => tag.substring(1)); // @ 제거;

        const jsonData = {
            "content": content,
            "hashtags": hashtags,
            "mentions": mentions,
            "visibility": postInvisibility ? "private" : "public"
        }
        // json 데이터 삽입
        const formData = new FormData();
        formData.append("request", new Blob([JSON.stringify(jsonData)], {
            type: "application/json",
        }));
        // file 데이터 삽입
        formData.append("image", selectedFile);
        // 업로드 요청
        axios.post("/posts/swagger", formData, {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }).then((response) => {
            navigate("/post/list")
        }).catch((error) => {
            console.log(error);
        });
    }

    return (
        <>
            <PageLayout>
                <Row className="mt-5">
                    <Col>
                        <Card className="h-100">
                            <Card.Header>
                                <h5>글 내용 작성</h5>
                            </Card.Header>
                            <Card.Body className="py-6 px-6">
                                <Row className="g-6">
                                    <Col>
                                        <label className="form-label"> 사진 추가 </label>
                                        <div> {/* 사진추가 부분 */}
                                            <img src={previewUrl ? previewUrl : noImage} alt="사진을 올려주세요"
                                                 style={{ width: "100%", minHeight: "300px", objectFit: 'cover' }}/>
                                            <br />
                                            <Button className={"w-100"} onClick={handleButtonClick}>이미지 업로드</Button>
                                            <input
                                                type="file"
                                                accept="image/*"
                                                ref={fileInputRef}
                                                style={{ display: 'none' }}
                                                onChange={handleFileChange}
                                            />
                                        </div>
                                    </Col>
                                    <div className="col-12">
                                        <Form.Label>
                                            <small style={{marginLeft: '15px'}}>나의 스토리</small>
                                        </Form.Label>
                                        <div style={{ position: "relative" }}>
                                            <Form.Control
                                                as="textarea"
                                                style={{ height: 'unset' }}
                                                rows={3}
                                                value={content}
                                                onChange={(e) => setContent(e.target.value)}
                                            />
                                        </div>
                                    </div>
                                </Row>
                            </Card.Body>
                            <Card.Footer>
                                <Stack gap={3} direction={"horizontal"}>
                                    <Form.Check
                                        className={"p-2"}
                                        type="switch"
                                        label={<span style={{ marginLeft: '8px' }}>게시글 숨기기 여부</span>}
                                        checked={postInvisibility}
                                        onChange={() => setPostInvisivility(prev => !prev)}
                                    />
                                    <div className="ms-auto kw-button p-3">
                                        <Button variant={'secondary'} onClick={() => navigate(-1)}>
                                            취소
                                        </Button>
                                        <Button variant={'primary'} onClick={handleSaveClick}>
                                            저장
                                        </Button>
                                    </div>
                                </Stack>
                            </Card.Footer>
                        </Card>
                    </Col>
                </Row>
            </PageLayout>
        </>
    );
}

export default PostNew;