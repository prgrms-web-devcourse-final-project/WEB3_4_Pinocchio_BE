package sns.pinocchio.presentation.comment.exception;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommentExceptionHandler {

	@ExceptionHandler(CommentException.class)
	public ResponseEntity<Map<String, String>> handleCommentException(CommentException ex) {
		return ResponseEntity
			.status(ex.getStatus())
			.body(Map.of(
				"code", ex.getCode(),
				"message", ex.getMessage()
			));
	}

}
