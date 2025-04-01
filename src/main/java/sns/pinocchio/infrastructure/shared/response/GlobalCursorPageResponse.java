package sns.pinocchio.infrastructure.shared.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GlobalCursorPageResponse {

  private final String nextCursor;

  private final boolean hasNext;
}
