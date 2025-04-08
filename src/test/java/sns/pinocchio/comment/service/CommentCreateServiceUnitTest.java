package sns.pinocchio.comment.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.commentDto.CommentCreateRequest;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@Tag("unit")
class CommentCreateServiceUnitTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepositoryMock;

    public CommentCreateServiceUnitTest() {
        MockitoAnnotations.openMocks(this); // @Mock 초기화
    }

    @Test
    void 댓글_생성_테스트() {
        String authorId = "user_001";
        String postId = "post_001";
        String commentId = "comment_001";

        CommentCreateRequest createRequest = CommentCreateRequest.builder()
                .postId(postId)
                .content("댓글이지롱")
                .build();

        Comment mockComment = Comment.builder()
                .id(commentId)
                .userId(authorId)
                .postId(postId)
                .content("댓글이지롱")
                .build();

        when(commentRepositoryMock.save(any(Comment.class))).thenReturn(mockComment);
        when(commentRepositoryMock.findById(commentId)).thenReturn(Optional.of(mockComment));

        Map<String, Object> response = commentService.createComment(createRequest, authorId);
        String createdCommentId = (String) response.get("commentId");

        assertNotNull(createdCommentId);
        assertEquals(commentId, createdCommentId);

        verify(commentRepositoryMock, times(1)).save(any(Comment.class));
        System.out.println("✅ 댓글 생성 성공");
    }
}