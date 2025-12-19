package org.example.demo_ssr_v1_1.reply;

import org.example.demo_ssr_v1_1._core.utils.MyDateUtil;

import lombok.Data;

/**
 * 댓글 응답 DTO
 * 
 * Open Session in View가 false일 때:
 * - 트랜잭션이 끝나면 세션이 종료되어 LAZY 로딩 불가
 * - Service에서 필요한 데이터를 모두 조회하고 DTO로 변환하여 반환
 * - 엔티티를 직접 반환하지 않고 DTO를 반환하여 계층 간 결합도 감소
 */
public class ReplyResponse {

    /**
     * 댓글 목록 응답 DTO
     */
    @Data
    public static class ListDTO {
        private Long id;
        private String comment;      // 댓글 내용
        private Long userId;         // 작성자 ID
        private String username;     // 작성자명 (평탄화)
        private String createdAt;    // 포맷된 생성일
        private boolean isOwner;     // 댓글 소유자 여부

        public ListDTO(Reply reply, Long sessionUserId) {
            this.id = reply.getId();
            this.comment = reply.getComment();
            // JOIN FETCH로 이미 로딩된 user 사용 (추가 쿼리 없음)
            if (reply.getUser() != null) {
                this.userId = reply.getUser().getId();
                this.username = reply.getUser().getUsername();
            }
            // 날짜 포맷팅
            if (reply.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(reply.getCreatedAt());
            }
            // 댓글 소유자 여부 확인
            System.out.println("=== ReplyResponse.ListDTO 생성 ===");
            System.out.println("댓글 ID: " + reply.getId());
            System.out.println("댓글 작성자 ID: " + (reply.getUser() != null ? reply.getUser().getId() : null));
            System.out.println("세션 사용자 ID: " + sessionUserId);
            this.isOwner = reply.isOwner(sessionUserId);
            System.out.println("isOwner 결과: " + this.isOwner);
            System.out.println("=================================");
        }
    }
}

