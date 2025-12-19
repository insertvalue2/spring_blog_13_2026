package org.example.demo_ssr_v1_1.reply;

import lombok.Data;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.example.demo_ssr_v1_1.board.Board;
import org.example.demo_ssr_v1_1.user.User;

/**
 * 댓글 요청 DTO
 * 
 * Controller와 Service 사이에서 데이터를 전송하는 객체
 */
public class ReplyRequest {

    /**
     * 댓글 작성 DTO
     */
    @Data
    public static class SaveDTO {
        private String comment;  // 댓글 내용
        private Long boardId;    // 게시글 ID

        /**
         * 유효성 검사
         */
        public void validate() {
            if (comment == null || comment.trim().isEmpty()) {
                throw new Exception400("댓글 내용을 입력해주세요");
            }
            if (comment.length() > 500) {
                throw new Exception400("댓글은 500자 이하여야 합니다");
            }
            if (boardId == null) {
                throw new Exception400("게시글 ID가 필요합니다");
            }
        }

        /**
         * DTO를 엔티티로 변환
         * 
         * @param board 게시글 엔티티
         * @param user 작성자 엔티티
         * @return Reply 엔티티
         */
        public Reply toEntity(Board board, User user) {
            return Reply.builder()
                    .comment(this.comment)
                    .board(board)
                    .user(user)
                    .build();
        }
    }

    /**
     * 댓글 수정 DTO
     */
    @Data
    public static class UpdateDTO {
        private String comment;  // 댓글 내용

        /**
         * 유효성 검사
         */
        public void validate() {
            if (comment == null || comment.trim().isEmpty()) {
                throw new Exception400("댓글 내용을 입력해주세요");
            }
            if (comment.length() > 500) {
                throw new Exception400("댓글은 500자 이하여야 합니다");
            }
        }
    }
}

