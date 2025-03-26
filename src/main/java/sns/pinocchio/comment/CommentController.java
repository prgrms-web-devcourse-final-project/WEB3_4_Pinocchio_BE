package sns.pinocchio.presentation.comment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
	@PutMapping("/modify")
	public ResponseEntity<String> modifyComment() {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body("테스트 ㅋㅋ");
	}
	@PostMapping("/{commentId}/like")
	public ResponseEntity<String> setCommentLike(@PathVariable Long commentId) {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body("테스트 ㅋㅋ");
	}
	@DeleteMapping
	public ResponseEntity<String> deleteComment() {
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body("테스트 ㅋㅋ");
	}




}
