package org.example.demo_ssr_v1_1._core.errors.exception;

/**
 * 500 Internal Server Error 커스텀 예외처리 클래스
 */
public class Exception500 extends RuntimeException {
    public Exception500(String msg) {
        super(msg);
    }
}
