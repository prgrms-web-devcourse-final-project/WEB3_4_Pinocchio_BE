import PageLayout from "../../layout/page/PageLayout";
import React, {useRef, useState} from "react";
import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import axios from "axios";

const NewPost = () => {
    const fileInputRef = useRef(null);
    const [previewUrl, setPreviewUrl] = useState("");
    const [selectedFile, setSelectedFile] = useState(null);
    const [postVisibility, setPostVisivility] = useState(false);
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
        const matches = content.match(/([#@][\w가-힣_]+)/g) || [];

        const hashtags = matches.filter(tag => tag.startsWith("#"));
        const mentions = matches.filter(tag => tag.startsWith("@"));

        console.log("해시태그:", hashtags); // ['#바다', '#여행']
        console.log("멘션:", mentions); // ['@friend', '@you_too']
        const jsonData = {
            "content": content,
            "imageUrls": [""],
            "hashtags": ["#테스트!!"],
            "mentions": ["testUser"],
            "visibility": postVisibility ? "private" : "public"
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
                                            {previewUrl ? <img src={previewUrl}
                                                               alt="사진을 올려주세요"
                                                               style={{ width: "100%", minHeight: "300px", objectFit: 'cover' }}
                                            /> :
                                                <span>아래 버튼을 눌러 사진을 업로드 해주세요</span>
                                            }
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
                                        checked={postVisibility}
                                        onChange={() => setPostVisivility(prev => !prev)}
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

export default NewPost;