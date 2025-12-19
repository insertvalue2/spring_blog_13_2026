package org.example.demo_ssr_v1_1._core.interceptor;

import org.example.demo_ssr_v1_1.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * [세션 정보를 뷰 모델에 주입하는 인터셉터]
 * * 역할: 모든 컨트롤러가 실행된 후(postHandle), 공통적으로 뷰(Mustache)에서 
 * 로그인 사용자 정보(sessionUser)를 쓸 수 있도록 모델에 넣어주는 역할.
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {

    /**
     * postHandle: 컨트롤러 로직 수행 '후', 뷰(HTML)가 그려지기 '전'에 실행됨.
     * * @param modelAndView : 컨트롤러가 반환한 '데이터(Model)'와 '화면정보(View)'가 합쳐진 객체
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        
        // 주의: @ResponseBody나 RestController를 쓰면 modelAndView가 null일 수 있음.
        // 따라서 null이 아니고, 실제 뷰 렌더링을 할 때만 로직을 실행해야 안전함.
        if (modelAndView != null) {
            
            // [핵심] getSession(false)를 사용하는 이유:
            // true(기본값)를 쓰면 로그인 안 한 방문자에게도 강제로 세션을 생성하여 메모리를 낭비함.
            // false를 써서 "있으면 가져오고, 없으면 null을 반환"하게 하여 불필요한 세션 생성을 방지함.
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                // 세션이 있다면 사용자 정보를 꺼냄
                User sessionUser = (User) session.getAttribute("sessionUser");
                
                // [데이터 주입]
                // 컨트롤러에서 model.addAttribute("sessionUser", user) 한 것과 똑같은 효과.
                // 이제 모든 Mustache 파일에서 {{#sessionUser}}...{{/sessionUser}} 사용 가능.
                if (sessionUser != null) {
                    modelAndView.addObject("sessionUser", sessionUser);
                }
            }
        }
    }
}