package org.example.demo_ssr_v1_1._core.errors.exception;

/**
 * 400 Bad Request 커스텀 예외처리 클래스
 */
public class Exception400 extends RuntimeException {
    public Exception400(String msg) {
        super(msg);
    }
}
