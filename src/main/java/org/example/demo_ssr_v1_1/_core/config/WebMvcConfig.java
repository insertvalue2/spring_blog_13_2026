package org.example.demo_ssr_v1_1._core.config;

import org.example.demo_ssr_v1_1._core.interceptor.AdminInterceptor;
import org.example.demo_ssr_v1_1._core.interceptor.LoginInterceptor;
import org.example.demo_ssr_v1_1._core.interceptor.SessionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정 클래스
 * 
 * 인터셉터 등록 및 URL 패턴 설정을 담당합니다.
 * 
 * @Configuration: 스프링 설정 클래스로 인식되어 IoC 컨테이너에 빈으로 등록됩니다.
 * @RequiredArgsConstructor: final 필드에 대한 생성자를 자동 생성 (DI)
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    // LoginInterceptor를 의존성 주입 (final로 불변 객체 보장)
    private final LoginInterceptor loginInterceptor;
    
    // SessionInterceptor를 의존성 주입 (모든 요청에 세션 정보 추가)
    private final SessionInterceptor sessionInterceptor;
    
    // AdminInterceptor를 의존성 주입 (관리자 권한 확인)
    private final AdminInterceptor adminInterceptor;

    /**
     * 인터셉터 등록 및 URL 패턴 설정
     * 
     * addInterceptors 메서드를 오버라이드하여 인터셉터를 등록합니다.
     * 
     * 동작 방식:
     * 1. addPathPatterns: 인터셉터가 적용될 URL 패턴을 지정합니다.
     * 2. excludePathPatterns: 인터셉터에서 제외할 URL 패턴을 지정합니다.
     * 
     * 패턴 우선순위:
     * - excludePathPatterns가 addPathPatterns보다 우선순위가 높습니다.
     * - 예: /board/**는 포함되지만, /board/list는 제외되면 제외 패턴이 적용됩니다.
     * 
     * @param registry 인터셉터 레지스트리
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // SessionInterceptor를 모든 요청에 등록 (세션 정보를 모델에 추가)
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/**");
        
        // LoginInterceptor를 시스템에 등록
        registry.addInterceptor(loginInterceptor)
                // 인터셉터가 동작할 URL 패턴 지정
                // /board/** : /board로 시작하는 모든 경로
                // /user/** : /user로 시작하는 모든 경로
                // /reply/** : /reply로 시작하는 모든 경로 (댓글 작성/삭제는 로그인 필요)
                // /admin/** : 관리자 전용 페이지 (반드시 로그인 + ADMIN 권한 필요)
                .addPathPatterns("/board/**", "/user/**", "/reply/**", "/admin/**")
                // 인터셉터에서 제외할 URL 패턴 지정
                .excludePathPatterns(
                        // 로그인 관련 (인증이 필요 없는 페이지)
                        "/login",                    // 로그인 화면 및 처리
                        "/join",                     // 회원가입 화면 및 처리
                        "/logout",                   // 로그아웃 (인증된 사용자도 접근 가능)
                        
                        // 게시글 조회 관련 (인증 없이도 볼 수 있는 페이지)
                        "/board/list",              // 게시글 목록
                        "/",                        // 메인 페이지 (게시글 목록)
                        "/board/{id:\\d+}",         // 게시글 상세보기 (숫자 ID만 허용)
                        
                        // 정적 리소스 (CSS, JS, 이미지 등)
                        "/css/**",                  // CSS 파일
                        "/js/**",                   // JavaScript 파일
                        "/images/**",               // 이미지 파일
                        "/favicon.ico",             // 파비콘
                        
                        // H2 데이터베이스 콘솔 (개발 환경용)
                        "/h2-console/**"            // H2 콘솔 접근
                );
                // \\d+ 는 정규표현식으로 "1개 이상의 숫자"를 의미
                // 예: /board/1, /board/123 등은 로그인 없이도 접근 가능
                // 하지만 /board/abc 같은 경우는 매칭되지 않음
        
        // AdminInterceptor를 관리자 전용 페이지에 등록
        // 주의: LoginInterceptor 이후에 실행되므로 로그인은 이미 확인된 상태입니다.
        registry.addInterceptor(adminInterceptor)
                // 관리자 전용 페이지에만 적용
                .addPathPatterns("/admin/**");
    }

    /**
     * 정적 리소스 핸들러 설정
     * 
     * 업로드된 이미지 파일을 웹에서 접근할 수 있도록 설정합니다.
     * 
     * @param registry 리소스 핸들러 레지스트리
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /images/** 경로로 요청이 오면 images/ 디렉토리에서 파일을 찾습니다
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:images/");
    }
}

