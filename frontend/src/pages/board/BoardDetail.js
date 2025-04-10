import PageLayout from "../../layout/page/PageLayout";
import {Button, Card, Carousel, Col, Image, Row} from "react-bootstrap";
import sampleProfile from "../../assets/images/sample_profile.png";
import likeIcon from "../../assets/images/icon/like.png"
const BoardDetail = () => {
    return (
        <PageLayout>
            <Row>
                <Col md={6} >
                    <Card style={{minWidth: "300px"}} >
                        <Card.Header style={{ minHeight: "70px" }}>
                            <Row>
                                <Col md={3}>
                                    <Image src={sampleProfile} rounded fluid />
                                </Col>
                                <Col md={4} >
                                    userId
                                </Col>
                                <Col md={"auto"} className={"ms-auto"}>
                                    2015~
                                </Col>
                            </Row>
                        </Card.Header>
                        <Card.Body>
                            <Image src={sampleProfile} rounded fluid />
                        </Card.Body>
                        <hr className={"solid"}/>
                        <Card.Footer style={{ minHeight: "50px" }}>
                            <Button />
                        </Card.Footer>
                    </Card>
                </Col>
                <Col md={6} >
                    <Card style={{height: "100%"}}>
                        <h3>게시물 제목</h3>
                    </Card>
                </Col>
            </Row>
        </PageLayout>
    );
}

export default BoardDetail;