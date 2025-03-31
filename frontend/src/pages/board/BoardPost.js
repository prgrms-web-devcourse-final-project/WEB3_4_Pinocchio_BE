import { Container, Row, Col, Card, ProgressBar } from "react-bootstrap";
import React, { useState, useEffect } from 'react';
import axios from "axios";
import useConfirm from "../../hooks/useConfirm";
import sampleProfile from "../../assets/images/sample_profile.png"

const loadNoticeList = async () => {
    const response = await axios.get(`/api/`);
    return response.data;
};

const BoardPost = () => {
    const { openConfirm } = useConfirm();

    return (
        <Col md={4}>
            <Card style={{marginBottom : '10px', height: "250px"}}>
                <img src={sampleProfile} />
            </Card>
        </Col>
    );
};

export default BoardPost;