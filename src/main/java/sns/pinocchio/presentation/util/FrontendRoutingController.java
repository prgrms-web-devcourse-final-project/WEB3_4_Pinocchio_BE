package sns.pinocchio.presentation.util;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendRoutingController {

    // 아래 경로들은 React Router가 처리할 수 있도록 index.html을 넘김
    @RequestMapping({
            "/",
            "/board/**",
            "/dashboard/**",
            "/login",
            "/signup",
            "/profile/**",
            "/qna/**",
            "/notice/**",
            "/post/**",
            "/posts/**",
            "/mypage/**",
    })
    public String forwardToIndex() {
        return "forward:/index.html"; // public/index.html로 요청 전달  //도커 푸시 다시
    }
}