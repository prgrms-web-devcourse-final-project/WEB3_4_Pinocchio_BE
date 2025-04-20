package sns.pinocchio.application.post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.S3img.S3Uploader;
import sns.pinocchio.config.global.event.PostEvent;
import sns.pinocchio.config.localImg.LocalUploader;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.post.Hashtag;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.Visibility;
import sns.pinocchio.infrastructure.persistence.mongodb.PostLikeRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;
import sns.pinocchio.infrastructure.persistence.mysql.HashtagRepository;
import sns.pinocchio.presentation.post.exception.PostErrorCode;
import sns.pinocchio.presentation.post.exception.PostException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final HashtagRepository hashtagRepository;
    private final MemberService memberService;
    private final ApplicationEventPublisher publisher;
    private final LocalUploader localUploader;

    // 이미지 업로드
    public String createPostWithImage(PostCreateRequest request, MultipartFile image, String tsid) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지는 반드시 1장 첨부해야 합니다.");
        }

        String imageUrl = localUploader.uploadFile(image, "post-image");
        request.setImageUrls(List.of(imageUrl)); // 기존 로직 재활용

        return createPost(request, tsid);
    }

    // 멘션 추출
    private List<String> extractMentions(String content) {
        List<String> mentions = new ArrayList<>();
        Pattern pattern = Pattern.compile("@(\\S+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            mentions.add(matcher.group(1)); // 예: "동생이랑"
        }
        return mentions;
    }

    // 게시물 생성
    public String createPost(PostCreateRequest request, String tsid) {
        List<String> imageUrls = request.getImageUrls();// 정책상 1장 추후 여러장 가능

        // 이미지가 반드시 1장이어야 함
        if (imageUrls == null || imageUrls.size() != 1) {
            throw new IllegalArgumentException("이미지는 정확히 1장만 등록해야 합니다.");
        }

        request.setMentions(extractMentions(request.getContent()));

        Post post = Post.builder()
                .tsid(tsid)  // 작성자 TSID (JWT에서 추출한 고유 식별자)
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
        publisher.publishEvent(new PostEvent(request.getContent(),post.getId()));

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

    // 게시물 수정
    @Transactional
    public void modifyPost(PostModifyRequest request, String logintsid) {
        // 작성자 본인의 게시물인지 확인하고 조회 (소프트 삭제 제외)
        Post post = postRepository.findByIdAndTsidAndStatus(
                request.getPostId(), logintsid, "active"
        ).orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // 수정 가능한 항목만 업데이트
        post.setContent(request.getContent());  // 본문 수정
        post.setVisibility(Visibility.valueOf(request.getVisibility().toUpperCase()));  // 공개 범위 수정
        post.setUpdatedAt(LocalDateTime.now());  // 수정 시간 갱신
        // 이미지 수정은 허용하지 않음

        postRepository.save(post);  // 수정 내용 저장
    }

    // 게시물 삭제
    @Transactional
    public void deletePost(String postId, String loginTsid, String action) {
        // 작성자 본인의 게시물인지 확인하고 조회 (status: active)
        Post post = postRepository.findByIdAndTsidAndStatus(postId, loginTsid, "active")
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        switch (action.toLowerCase()) {
            case "delete" -> {
                // 🔸 소프트 삭제 (status = deleted + 비공개 전환)
                post.setStatus("deleted");
                post.setVisibility(Visibility.PRIVATE);
            }
            case "private" -> {
                // 🔸 공개 → 비공개 전환만 수행
                post.setVisibility(Visibility.PRIVATE);
            }
            default -> {
                // 지원하지 않는 action 값인 경우 예외 발생
                throw new IllegalArgumentException("지원하지 않는 삭제 유형입니다. (delete | private 만 허용)");
            }
        }

        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    // 게시물 상세 조회
    @Transactional
    public PostDetailResponse getPostDetail(String postId, String loginTsid) {
        // 1. 게시글 조회 (status = active)
        Post post = postRepository.findByIdAndStatus(postId, "active")
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        // 2. 비공개 게시글이면 작성자 본인만 볼 수 있음
        if (post.getVisibility() == Visibility.PRIVATE && !post.getTsid().equals(loginTsid)) {
            throw new PostException(PostErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 3. 조회수 +1 처리
        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        // 4. 현재 사용자가 이 게시글에 좋아요 했는지 확인
        boolean liked = postLikeRepository.existsByPostIdAndTsid(post.getId(), loginTsid);

        // 5. 작성자 정보 (MySQL에서 조회)
        Member member = memberService.findByTsid(post.getTsid()); //  TSID 기반 조회

        // 6. DTO로 응답
        return PostDetailResponse.builder()
                .postId(post.getId())
                .tsid(post.getTsid())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImageUrl()) // null 가능성 있음
                .content(post.getContent())
                .imageUrls(post.getImageUrls())
                .hashtags(post.getHashtags())
                .likes(post.getLikes())
                .commentsCount(post.getCommentsCount())
                .views(post.getViews())
                .visibility(post.getVisibility().name())
                .mentions(post.getMentions())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .liked(liked) //  좋아요 여부 응답에 포함
                .build();
    }

}
