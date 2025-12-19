package org.example.demo_ssr_v1_1._core.interceptor;

import org.example.demo_ssr_v1_1._core.errors.exception.Exception401;
import org.example.demo_ssr_v1_1.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 로그인 인증 인터셉터
 * 
 * 컨트롤러에 진입하기 전에 세션에 로그인 정보가 있는지 확인합니다.
 * 로그인하지 않은 사용자의 경우 Exception401을 발생시켜 접근을 차단합니다.
 * 
 * @Component: IoC 컨테이너에 빈으로 등록 (싱글톤 패턴)
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * preHandle: 컨트롤러 진입 전에 실행되는 메서드
     * 
     * 동작 흐름:
     * 1. 요청이 들어오면 이 메서드가 먼저 실행됩니다.
     * 2. 세션에서 sessionUser를 확인합니다.
     * 3. sessionUser가 null이면 Exception401을 발생시켜 접근을 차단합니다.
     * 4. sessionUser가 있으면 true를 반환하여 컨트롤러로 진입을 허용합니다.
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
        
        // 세션에 로그인 정보가 없으면 인증 오류 발생
        if (sessionUser == null) {
            throw new Exception401("로그인 먼저 해주세요");
        }
        
        // 로그인 정보가 있으면 컨트롤러로 진입 허용
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

