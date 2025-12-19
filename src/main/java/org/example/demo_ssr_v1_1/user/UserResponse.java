package org.example.demo_ssr_v1_1.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.ToString;

import java.util.Properties;


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

    // JSON 형식에 코딩 컨벤션이 스네이크 케이스를 카멜 노테이션으로 할당하라!
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Data
    public static class OAuthToken {
        private String tokenType;	// 토큰	토큰 타입, bearer로 고정(JWT란 의미)
        private String accessToken;	// String 사용자 액세스 토큰 값(카카오에 사용자 정보를 요청할 수 있는 인증 토큰)
        private Integer expiresIn;
        private String refreshToken;
        private String refreshTokenExpiresIn;

    }

    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Data
    public static class KakaoProfile {
        private Long id;
        private String connectedAt;
        private Properties properties;
    }


    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Data
    public static class Properties {
        private String nickname;
        private String profileImage;
        private String thumbnailImage;
    }

}

