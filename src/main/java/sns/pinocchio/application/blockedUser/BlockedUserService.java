package sns.pinocchio.application.blockedUser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.infrastructure.blockedUser.BlockedUserRepository;

@RequiredArgsConstructor
@Service
public class BlockedUserService {

    private final BlockedUserRepository blockedUserRepository;

}