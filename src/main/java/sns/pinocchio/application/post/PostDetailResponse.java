package sns.pinocchio.application.post;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostDetailResponse {

    //  MongoDB: 게시물 ID (_id)
    private String postId;

    //  MySQL: 작성자 TSID (Mongo + MySQL 공통 식별자)
    private String tsid;

    //  MySQL: 작성자 닉네임
    private String nickname;

    //  MySQL: 작성자 프로필 이미지 URL
    private String profileImage;

    //  MongoDB: 게시글 본문 내용
    private String content;

    //  MongoDB: 게시물에 첨부된 이미지 (1장 필수)
    private List<String> imageUrls;

    //  MongoDB: 해시태그 목록 (예: ["#여행", "#제주도"])
    private List<String> hashtags;

    //  MongoDB: 좋아요 수 (조회 시 기준)
    private int likes;

    //  MongoDB: 댓글 수 (조회 시 기준)
    private int commentsCount;

    //  MongoDB: 조회수 (조회 시 +1 증가 처리)
    private int views;

    //  MongoDB: 공개 범위 ("PUBLIC", "PRIVATE")
    private String visibility;

    //  MongoDB: 멘션된 사용자 TSID 목록
    private List<String> mentions;

    //  MongoDB: 게시물 상태 ("active" or "deleted")
    private String status;

    //  MongoDB: 게시물 생성 시간
    private LocalDateTime createdAt;

    //  MongoDB: 게시물 마지막 수정 시간
    private LocalDateTime updatedAt;
}