package org.example.demo_ssr_v1_1.board;

import lombok.Data;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.example.demo_ssr_v1_1.user.User;

// 정적 내부 클래스 활용
// 컨트롤러와 비즈니스 로직 사이에서 데이터를 전송하는 객체
public class BoardRequest {

    // 게시글 저장 DTO
    @Data
    public static class SaveDTO {
        private String title;
        private String content;
        private String username;

        public Board toEntity(User user) {
            return new Board(title, content, user);
        }

    }

    @Data
    public static class UpdateDTO {
        private String title;
        private String content;
        private String username;

        // 검증 메서드
        public void  validate() {
            if(title == null || title.trim().isEmpty()) {
                throw new Exception400("제목은 필수 입니다");
            }

            if(content == null || content.trim().isEmpty()) {
                throw new Exception400("내용은 필수 입니다");
            }
        }

    }

}
