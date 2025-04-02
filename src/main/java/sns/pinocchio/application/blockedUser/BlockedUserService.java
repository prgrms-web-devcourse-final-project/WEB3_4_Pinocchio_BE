package sns.pinocchio.application.blockedUser;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.application.blockedUser.response.BlockedUserResponse;
import sns.pinocchio.domain.blockedUser.BlockedUser;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.blockedUser.BlockedUserRepository;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.block.exception.BlockErrorCode;
import sns.pinocchio.presentation.block.exception.BlockException;

@RequiredArgsConstructor
@Service
public class BlockedUserService {

  private final BlockedUserRepository blockedUserRepository;
  private final MemberRepository memberRepository;

  public void saveBlock(Long blockerId, Long blockedId) {
    if (blockerId.equals(blockedId)) {
      throw new BlockException(BlockErrorCode.CANNOT_BLOCK_SELF);
    }

    boolean isAlreadyBlocked =
        blockedUserRepository.existsByBlockerUserIdAndBlockedUserId(blockerId, blockedId);

    if (isAlreadyBlocked) {
      throw new BlockException(BlockErrorCode.ALREADY_BLOCKED);
    }

    BlockedUser blockedUser =
        BlockedUser.builder().blockerUserId(blockerId).blockedUserId(blockedId).build();

    blockedUserRepository.save(blockedUser);
  }

  public void deleteBlock(Long blockerId, Long blockedId) {
    BlockedUser blockedUser =
        blockedUserRepository
            .findByBlockerUserIdAndBlockedUserId(blockerId, blockedId)
            .orElseThrow(() -> new BlockException(BlockErrorCode.BLOCK_NOT_FOUND));

    blockedUserRepository.delete(blockedUser);
  }

  // 차단 내역 조회
  public List<BlockedUserResponse.UserData> getBlockedUsers(Long blockerId) {
    List<BlockedUser> blockedUsers = blockedUserRepository.findByBlockerUserId(blockerId);

    if (blockedUsers.isEmpty()) {
      throw new BlockException(BlockErrorCode.BLOCK_NOT_FOUND);
    }

    List<Long> blockedUserIds =
        blockedUsers.stream().map(BlockedUser::getBlockedUserId).collect(Collectors.toList());

    List<Member> blockedMembers = memberRepository.findAllById(blockedUserIds);

    return blockedMembers.stream()
        .map(BlockedUserResponse.UserData::of)
        .collect(Collectors.toList());
  }

}
