package sns.pinocchio.domain.loginHistory;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loginHistoryId;

    private Long userId;

    private String loginIp;

    private String loginDevice;

    private String userAgent;

    private LocalDateTime loginTime;

    @Builder
    public LoginHistory(Long userId, String loginIp, String loginDevice, String userAgent, LocalDateTime loginTime) {
        this.userId = userId;
        this.loginIp = loginIp;
        this.loginDevice = loginDevice;
        this.userAgent = userAgent;
        this.loginTime = loginTime;
    }
}