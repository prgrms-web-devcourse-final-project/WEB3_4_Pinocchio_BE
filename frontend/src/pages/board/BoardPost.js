import { Container, Row, Col, Card, ProgressBar } from "react-bootstrap";
import React, { useState, useEffect } from 'react';
import axios from "axios";
import useConfirm from "../../hooks/useConfirm";
import sampleProfile from "../../assets/images/sample_profile.png"
import {useNavigate} from "react-router-dom"

const loadNoticeList = async () => {
    const response = await axios.get(`/api/`);
    return response.data;
};

const BoardPost = () => {
    const { openConfirm } = useConfirm();
    const navigate = useNavigate();

    return (
        <Col md={4}>
            <Card style={{marginBottom : '10px', height: "250px"}} onClick={() => navigate('/board/detail/1')}>
                <img src={sampleProfile} />
            </Card>
        </Col>
    );
};

export default BoardPost;