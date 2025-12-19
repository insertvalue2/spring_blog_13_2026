package org.example.demo_ssr_v1_1._core.errors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.demo_ssr_v1_1._core.errors.exception.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

// @ControllerAdvice - 모든 컨트롤러에서 발생하는 예외를 이 클래스에서 중앙 집중화 시킴
// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
@ControllerAdvice
@Slf4j
public class MyExceptionHandler {

    // 내가 지켜볼 예외를 명시를 해주면 ControllerAdvice 가 가지고와 처리 함
    @ExceptionHandler(Exception400.class)
    public String ex400(Exception400 e, HttpServletRequest request, Model model) {
        log.warn("=== 400 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        model.addAttribute("msg", e.getMessage());
        // [수정] request.setAttribute -> model.addAttribute
        return "err/400";
    }

    // 401 인증 오류
//    @ExceptionHandler(Exception401.class)
//    public String ex401(Exception401 e, HttpServletRequest request, Model model) {
//        log.warn("=== 401 에러 발생  ===");
//        log.warn("요청 URL : {}", request.getRequestURL());
//        log.warn("에러 메세지 : {}", e.getMessage());
//        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
//        model.addAttribute("msg", e.getMessage());
//        return "err/401";
//    }
    // 401 인증 오류 (로그인 필요)
    @ExceptionHandler(Exception401.class)
    public ResponseEntity<String> ex401(Exception401 e, HttpServletRequest request) { // Model 제거
        log.warn("=== 401 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());

        // 자바스크립트 생성: alert 띄우고 -> location.href로 이동
        // 메시지의 작은따옴표를 이스케이프 처리
        String message = e.getMessage() != null ? e.getMessage() : "인증이 필요합니다";
        String escapedMessage = message.replace("'", "\\'");
        String script = "<script>" +
                "alert('" + escapedMessage + "');" +
                "location.href='/login';" +
                "</script>";

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401 상태 코드 설정
                .contentType(MediaType.TEXT_HTML) // HTML 응답임을 명시
                .body(script);
    }

    // 403 인가 오류
//    @ExceptionHandler(Exception403.class)
//    public String ex401(Exception403 e, HttpServletRequest request) {
//        log.warn("=== 403 에러 발생  ===");
//        log.warn("요청 URL : {}", request.getRequestURL());
//        log.warn("에러 메세지 : {}", e.getMessage());
//        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
//        request.setAttribute("msg", e.getMessage());
//        return "err/403";
//    }

    @ExceptionHandler(Exception403.class)
    @ResponseBody
    public ResponseEntity<String> ex403(Exception403 e) {
        // 메시지의 작은따옴표를 이스케이프 처리
        String message = e.getMessage() != null ? e.getMessage() : "접근 권한이 없습니다";
        String escapedMessage = message.replace("'", "\\'");
        String script = "<script>alert('"+escapedMessage+"');" +
                "history.back();" +
                "</script>";

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.TEXT_HTML)
                .body(script);
    }


    // 404 인가 오류
    @ExceptionHandler(Exception404.class)
    public String ex404(Exception404 e, HttpServletRequest request, Model model) {
        log.warn("=== 404 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        model.addAttribute("msg", e.getMessage());
        return "err/404";
    }

    // 500 서버 내부 오류
    @ExceptionHandler(Exception500.class)
    public String ex500(Exception500 e, HttpServletRequest request, Model model) {
        log.warn("=== 500 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        model.addAttribute("msg", e.getMessage());
        return "err/500";
    }

    // 데이터베이스 제약조건 위반 오류 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolationException(DataIntegrityViolationException e,
                                                         HttpServletRequest request, Model model) {
        log.warn("=== 데이터베이스 제약조건 위반 오류 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        
        // 외래키 제약조건 위반인 경우
        String errorMessage = e.getMessage();
        if (errorMessage != null && errorMessage.contains("FOREIGN KEY")) {
            model.addAttribute("msg", "관련된 데이터가 있어 삭제할 수 없습니다. (예: 게시글에 댓글이 있는 경우)");
        } else {
            model.addAttribute("msg", "데이터베이스 제약조건 위반: " + e.getMessage());
        }
        return "err/500";
    }

    // 기타 모든 실행시점 오류 처리
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e,
                                         HttpServletRequest request, Model model) {
        log.warn("=== 예상하지 못한 에러 발생  ===");
        log.warn("요청 URL : {}", request.getRequestURL());
        log.warn("에러 메세지 : {}", e.getMessage());
        log.warn("예외 클래스 : {}", e.getClass().getSimpleName());
        model.addAttribute("msg", e.getMessage());
        return "err/500";
    }

    // 클래스 로딩 오류 처리 (NoClassDefFoundError, ClassNotFoundException 등)
    @ExceptionHandler(Error.class)
    public String handleError(Error e, HttpServletRequest request, Model model) {
        log.error("=== 심각한 오류 발생 (Error) ===");
        log.error("요청 URL : {}", request.getRequestURL());
        log.error("에러 메세지 : {}", e.getMessage());
        log.error("예외 클래스 : {}", e.getClass().getSimpleName());
        log.error("스택 트레이스:", e);
        
        // NoClassDefFoundError의 경우 더 명확한 메시지 제공
        String errorMessage = e.getMessage();
        if (e instanceof NoClassDefFoundError) {
            model.addAttribute("msg", "클래스를 찾을 수 없습니다: " + errorMessage + 
                    " (빌드 정리 후 재컴파일이 필요할 수 있습니다)");
        } else {
            model.addAttribute("msg", "심각한 오류가 발생했습니다: " + errorMessage);
        }
        return "err/500";
    }

    // 모든 예외 및 오류 처리 (최후의 안전장치)
    @ExceptionHandler(Throwable.class)
    public String handleThrowable(Throwable e, HttpServletRequest request, Model model) {
        log.error("=== 처리되지 않은 예외/오류 발생 ===");
        log.error("요청 URL : {}", request.getRequestURL());
        log.error("에러 메세지 : {}", e.getMessage());
        log.error("예외 클래스 : {}", e.getClass().getSimpleName());
        log.error("스택 트레이스:", e);
        
        model.addAttribute("msg", "예상치 못한 오류가 발생했습니다: " + 
                (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        return "err/500";
    }



}
