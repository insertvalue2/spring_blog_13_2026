package org.example.demo_ssr_v1_1._core.errors.exception;

/**
 * 404 Not Found 커스텀 예외처리 클래스
 */
public class Exception404 extends RuntimeException {
    public Exception404(String msg) {
        super(msg);
    }
}
