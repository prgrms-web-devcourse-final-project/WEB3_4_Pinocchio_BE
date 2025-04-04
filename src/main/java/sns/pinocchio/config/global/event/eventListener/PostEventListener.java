package sns.pinocchio.config.global.event.eventListener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sns.pinocchio.config.global.event.PostEvent;
import sns.pinocchio.config.global.event.eventRunner.PostEventRunner;
@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener {
	private final PostEventRunner postEventRunner;
	@Async
	@EventListener
	public void postCreateEvent(PostEvent event) {
		try{
			postEventRunner.createAiComment(event);
		} catch (Exception e) {
			log.error("Failed to create Ai comment :" + e);
		}
	}

}
