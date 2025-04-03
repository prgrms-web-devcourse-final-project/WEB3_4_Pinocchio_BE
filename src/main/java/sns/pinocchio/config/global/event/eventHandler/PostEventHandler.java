package sns.pinocchio.config.global.event.eventHandler;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.config.global.event.Event;

@Component
@RequiredArgsConstructor
public class PostEventHandler {
	private final CommentService commentService;

	@EventListener
	public void createComment(Event event){
		//commentService.createComment();
	}
}
