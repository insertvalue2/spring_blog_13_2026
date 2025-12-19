package org.example.demo_ssr_v1_1._core.interceptor;

import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 관리자 인가 인터셉터
 * 
 * 관리자 전용 페이지(/admin/**)에 접근할 때 관리자 권한을 확인합니다.
 * 로그인은 LoginInterceptor에서 이미 확인했으므로, 여기서는 관리자 권한만 확인합니다.
 * 
 * 동작 흐름:
 * 1. LoginInterceptor가 먼저 실행되어 로그인 여부를 확인합니다.
 * 2. 로그인된 사용자라면 이 인터셉터가 실행됩니다.
 * 3. 세션의 사용자가 관리자(ADMIN 권한)인지 확인합니다.
 * 4. 관리자가 아니면 Exception403을 발생시켜 접근을 차단합니다.
 * 5. 관리자이면 컨트롤러로 진입을 허용합니다.
 * 
 * @Component: IoC 컨테이너에 빈으로 등록 (싱글톤 패턴)
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    /**
     * preHandle: 컨트롤러 진입 전에 실행되는 메서드
     * 
     * 동작 흐름:
     * 1. 세션에서 sessionUser를 확인합니다.
     * 2. sessionUser가 null이면 Exception403을 발생시킵니다. (이론적으로는 LoginInterceptor에서 이미 걸러졌지만 안전장치)
     * 3. sessionUser.isAdmin()이 false이면 Exception403을 발생시켜 접근을 차단합니다.
     * 4. 관리자이면 true를 반환하여 컨트롤러로 진입을 허용합니다.
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler 실행될 핸들러(컨트롤러 메서드)
     * @return true: 컨트롤러로 진입 허용, false: 진입 차단
     * @throws Exception 예외 발생 시
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 세션에서 로그인 사용자 정보 조회
        HttpSession session = request.getSession();
        User sessionUser = (User) session.getAttribute("sessionUser");
        
        // 1. 로그인 체크는 LoginInterceptor가 이미 했으므로 생략 가능하지만, 안전을 위해 한번더 확인 
        if (sessionUser == null) {
            throw new Exception403("로그인이 필요합니다");
        }
        
        // 관리자 권한이 없으면 인가 오류 발생
        if (!sessionUser.isAdmin()) {
            throw new Exception403("관리자 권한이 필요합니다");
        }
        
        // 관리자 권한이 있으면 컨트롤러로 진입 허용
        return true;
    }

    /**
     * postHandle: 컨트롤러 실행 후, 뷰 렌더링 전에 실행되는 메서드
     * 
     * 컨트롤러에서 반환한 ModelAndView를 조작하거나 추가 작업을 수행할 수 있습니다.
     * 현재는 기본 구현을 사용합니다.
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler 실행된 핸들러
     * @param modelAndView 컨트롤러에서 반환한 ModelAndView
     * @throws Exception 예외 발생 시
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // 필요시 뷰 렌더링 전 추가 작업 수행
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * afterCompletion: 요청 처리가 완전히 끝난 후 실행되는 메서드
     * 
     * 뷰 렌더링까지 완료된 후에 호출됩니다.
     * 리소스 정리나 로깅 등의 작업을 수행할 수 있습니다.
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler 실행된 핸들러
     * @param ex 예외가 발생한 경우 예외 객체, 없으면 null
     * @throws Exception 예외 발생 시
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 필요시 요청 완료 후 추가 작업 수행 (리소스 정리, 로깅 등)
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}

