package sns.pinocchio.application.post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.domain.post.Hashtag;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.Visibility;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;
import sns.pinocchio.infrastructure.persistence.mysql.HashtagRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;

    // 게시물 생성
    public String createPost(PostCreateRequest request, String userTsid) {
        Post post = Post.builder()
                .userTsid(userTsid)  // 작성자 TSID (JWT에서 추출한 고유 식별자)
                .content(request.getContent())  // 게시글 본문
                .imageUrls(request.getImageUrls())  // 이미지 URL 목록
                .hashtags(request.getHashtags())  // 해시태그 목록
                .mentions(request.getMentions())  // 멘션된 사용자 목록
                .visibility(Visibility.valueOf(request.getVisibility().toUpperCase()))  // 공개 여부 (문자열 → Enum)
                .likes(0)  // 초기 좋아요 수
                .commentsCount(0)  // 초기 댓글 수
                .views(0)  // 초기 조회수
                .status("active")  // 게시 상태 (soft delete 구분용)
                .createdAt(LocalDateTime.now())  // 생성 시간
                .updatedAt(LocalDateTime.now())  // 수정 시간
                .build();

        Post savedPost = postRepository.save(post);

        // 📌 해시태그 MySQL 반영  (신규 생성 or 사용량 증가)
        updateHashtagUsage(request.getHashtags());

        return savedPost.getId();
    }

    //  게시물 생성시 해시태그 저장
    private void updateHashtagUsage(List<String> hashtags) {
        for (String tag : hashtags) {
            hashtagRepository.findByTag(tag)
                    .ifPresentOrElse(
                            hashtag -> {
                                // 기존 해시태그 존재 시 사용량 증가
                                hashtag.setUsageCount(hashtag.getUsageCount() + 1);
                                hashtagRepository.save(hashtag);
                            },
                            () -> {
                                // 새로운 해시태그 등록
                                Hashtag newTag = Hashtag.builder()
                                        .tag(tag)
                                        .usageCount(1)
                                        .build();
                                hashtagRepository.save(newTag);
                            }
                    );
        }
    }


    @Transactional
    public void modifyPost(PostModifyRequest request, String loginUserTsid) {
        // 작성자 본인의 게시물인지 확인하고 조회 (소프트 삭제 제외)
        Post post = postRepository.findByIdAndUserTsidAndStatus(
                request.getPostId(), loginUserTsid, "active"
        ).orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // 수정 가능한 항목만 업데이트
        post.setContent(request.getContent());  // 본문 수정
        post.setImageUrls(request.getImageUrls());  // 이미지 수정
        post.setVisibility(Visibility.valueOf(request.getVisibility().toUpperCase()));  // 공개 범위 수정
        post.setUpdatedAt(LocalDateTime.now());  // 수정 시간 갱신

        postRepository.save(post);  // 수정 내용 저장
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(String postId, String loginUserTsid) {
        Post post = postRepository.findByIdAndUserTsidAndStatus(postId, loginUserTsid, "active")
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.setStatus("deleted");
        post.setUpdatedAt(LocalDateTime.now());

        postRepository.save(post);
    }

}
