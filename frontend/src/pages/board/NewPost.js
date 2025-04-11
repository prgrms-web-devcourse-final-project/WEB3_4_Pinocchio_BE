import PageLayout from "../../layout/page/PageLayout";
import React, {useEffect, useState} from "react";
import {Button, Card, Col, Form, Row} from "react-bootstrap";
import axios from "axios";
import useConfirm from "../../hooks/useConfirm";
import NoMappingFile from "../../shared/fileUpload/NoMappingFile";
import CreateEditor from "../../shared/quillEditor/CreateEditor";
import {useNavigate} from "react-router-dom";
import Spinner from "../../shared/Spinner";
import {useToast} from "../../hooks/useToast";
import ProfileImageZone from "./mypage/ImageDropZone";

const NewPost = () => {
    const showToast = useToast();
    const { openConfirm } = useConfirm();
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();
    const [formData, setFormData] = useState({softwareName:'', softwareVersionName:'', expectedReleaseDate:'', prevInspectionHistory:'', description:''});
    // 업로드한 파일
    const [uploadedFileData, setUploadedFileData] = useState([]);
    const [content, setContent] = useState("");


    return (
        <>
            {isLoading && <Spinner/>}
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
                                        <ProfileImageZone/>
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
                                <div className="kw-list-foot mt-4">
                                    <div className="kw-list-foot-start"></div>
                                    <div className="kw-list-foot-center"></div>
                                    <div className="kw-d-foot-end">
                                        <div className="kw-button">
                                            <Button variant={'secondary'} onClick={() => navigate(-1)}>
                                                취소
                                            </Button>
                                            <Button variant={'primary'} onClick={() => navigate("/board/list")}>
                                                저장
                                            </Button>
                                        </div>
                                    </div>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
            </PageLayout>
        </>
    );
}

export default NewPost;