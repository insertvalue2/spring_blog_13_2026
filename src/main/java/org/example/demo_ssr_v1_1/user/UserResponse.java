package org.example.demo_ssr_v1_1.user;

import lombok.Data;


/**
 * 사용자 응답 DTO
 * 
 * Open Session in View가 false일 때:
 * - 트랜잭션이 끝나면 세션이 종료되어 LAZY 로딩 불가
 * - Service에서 필요한 데이터를 모두 조회하고 DTO로 변환하여 반환
 * - 엔티티를 직접 반환하지 않고 DTO를 반환하여 계층 간 결합도 감소
 */
public class UserResponse {

    /**
     * 회원정보 수정 화면 응답 DTO
     */
    @Data
    public static class UpdateFormDTO {
        private Long id;
        private String username;
        private String email;

        public UpdateFormDTO(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
    }

    /**
     * 로그인 응답 DTO (세션 저장용)
     * 
     * 주의: 세션에는 엔티티를 저장하지만, 
     * 다른 곳으로 전달할 때는 DTO를 사용하는 것이 좋음
     */
    @Data
    public static class LoginDTO {
        private Long id;
        private String username;
        private String email;

        public LoginDTO(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
    }
}

