package org.example.demo_ssr_v1_1.user;

import lombok.Data;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.springframework.web.multipart.MultipartFile;

public class UserRequest {

    @Data
    public static class LoginDTO {
        private String username;
        private String password;

        public void validate() {
            if(username == null  || username.trim().isEmpty()) {
                throw new Exception400("사용자명을 입력해주세요");
            }
            if(password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호를 입력해주세요");
            }
        }

    } // end of inner class

    @Data
    public static class JoinDTO {
        private String username;
        private String password;
        private String email;
        
        // 프로필 이미지 (선택사항)
        // MultipartFile: Spring에서 파일 업로드를 처리하기 위한 인터페이스
        // - HTML form의 <input type="file"> 태그로 전송된 파일을 받을 수 있음
        // - enctype="multipart/form-data"로 전송된 데이터를 자동으로 바인딩
        // 
        // 선택사항이므로:
        // - 사용자가 파일을 업로드하지 않으면 null 또는 empty 상태
        // - null 체크 후 처리하므로 필수 입력이 아님
        private MultipartFile profileImage;
        
        public void validate() {
            if(username == null  || username.trim().isEmpty()) {
                throw new Exception400("사용자명을 입력해주세요");
            }
            if(password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호를 입력해주세요");
            }
            if(email == null || email.trim().isEmpty()) {
                throw new Exception400("이메일을 입력해주세요");
            }
            if(email.contains("@") == false) {
                throw new Exception400("올바른 이메일 형식이 아닙니다");
            }
            // 프로필 이미지는 선택사항이므로 유효성 검사 생략
            // → 사용자가 이미지를 업로드하지 않아도 회원가입 가능
        }

        /**
         * JoinDTO를 User 엔티티로 변환
         * 
         * 중요: MultipartFile 객체는 DB에 저장할 수 없습니다!
         * - DB에는 파일 자체가 아닌 "파일명"만 저장합니다
         * - 실제 파일은 서버 디스크의 "images/" 폴더에 저장됩니다
         * 
         * @param profileImageFilename Service에서 파일 저장 후 반환받은 파일명
         *                            (예: "abc123-456-789-profile.jpg")
         *                            파일이 없으면 null
         * @return User 엔티티 객체
         */
        public User toEntity(String profileImageFilename) {
            return User.builder()
                    .username(this.username)
                    .password(this.password)
                    .email(this.email)
                    .profileImage(profileImageFilename)  // 파일명만 저장
                    .build();
        }

    }  // end of inner clsss

    @Data
    public static class UpdateDTO {
        private String password;
        private MultipartFile profileImage;  // 프로필 이미지 (선택사항)
        private String profileImageFilename;  // 저장된 파일명 (Service에서 설정)
        // username 은 제외: 변경 불가는한 고유 식별자

        public void validate() {
            if(password == null || password.trim().isEmpty()) {
                throw new Exception400("비밀번호를 입력해주세요");
            }
            if(password.length() < 4) {
                throw new Exception400("비밀번호는 4글자 이상이어야 합니다");
            }
            // 프로필 이미지는 선택사항이므로 유효성 검사 생략
        }
        
        /**
         * 저장된 프로필 이미지 파일명 반환
         * Service에서 설정한 파일명을 반환합니다.
         * 
         * @return 저장된 파일명 (없으면 null)
         */
        public String getProfileImageFilename() {
            return profileImageFilename;
        }
    }
}
