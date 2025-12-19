package org.example.demo_ssr_v1_1._core.errors.exception;

/**
 * 401 Unauthorized 인증 처리 오류
 */
public class Exception401 extends RuntimeException {
    public Exception401(String msg) {
        super(msg);
    }
}
