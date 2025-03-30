package sns.pinocchio.application.report.reportDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sns.pinocchio.domain.report.ReportedType;

// 신고 요청 Dto
public record ReportRequestDto(
        @NotNull(message = "신고 대상 닉네임은 필수입니다.")
        String reportedNickname,

        @NotNull(message = "신고 대상 유형은 필수입니다.")
        ReportedType reportedType, // POST, COMMENT, USER

        @Size(min = 10, max = 150, message = "신고 사유는 10자 이상 150자 내외입니다.")
        String reason
) {}