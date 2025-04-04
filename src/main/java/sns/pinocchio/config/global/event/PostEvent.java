package sns.pinocchio.config.global.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostEvent {
	private final String content;
	private final String postId;
}
