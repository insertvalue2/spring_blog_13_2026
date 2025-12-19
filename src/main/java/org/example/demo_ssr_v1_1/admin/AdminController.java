package org.example.demo_ssr_v1_1.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.example.demo_ssr_v1_1.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 관리자 전용 페이지 컨트롤러.
 *
 * - /admin/** URI는 WebMvcConfig에서 LoginInterceptor와 AdminInterceptor가 처리합니다.
 * - 로그인 및 관리자 권한 체크는 인터셉터에서 자동으로 처리되므로 컨트롤러에서는 불필요합니다.
 */
@Controller
@RequiredArgsConstructor
public class AdminController {

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        
        // if(sessionUser == null) {
        //     throw new Exception401("로그인이 필요합니다");
        // }

        // if(!sessionUser.isAdmin()) {
        //     throw new Exception401("관리자만 접근 가능 합니다");
        // }

        model.addAttribute("adminName", sessionUser.getUsername());
        return "admin/dashboard";
    }
}


