package org.example.demo_ssr_v1_1.reply;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.example.demo_ssr_v1_1.board.Board;
import org.example.demo_ssr_v1_1.user.User;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

/**
 * 댓글 엔티티
 * 
 * 단방향 관계 설계:
 * - Reply -> Board (ManyToOne): 댓글이 속한 게시글
 * - Reply -> User (ManyToOne): 댓글 작성자
 * 
 * 양방향 관계를 사용하지 않는 이유:
 * - 단방향 관계가 더 단순하고 명확함
 * - 불필요한 양방향 참조로 인한 복잡성 제거
 * - 성능 최적화 (필요한 경우에만 조회)
 */
@Data
@NoArgsConstructor
@Table(name = "reply_tb")
@Entity
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 500)
    private String comment;  // 댓글 내용 (최대 500자)
    
    // 단방향 관계: Reply -> Board (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;
    
    // 단방향 관계: Reply -> User (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public Reply(String comment, Board board, User user) {
        this.comment = comment;
        this.board = board;
        this.user = user;
    }

    /**
     * 댓글 소유자 확인 로직
     * 
     * @param userId 확인할 사용자 ID
     * @return 소유자 여부
     */
    public boolean isOwner(Long userId) {
        if (this.user == null || userId == null) {
            System.out.println("isOwner: user 또는 userId가 null입니다. user=" + this.user + ", userId=" + userId);
            return false;
        }
        // Long 타입 비교 시 equals() 사용 (== 비교는 위험)
        // null 체크 후 비교
        Long replyUserId = this.user.getId();
        if (replyUserId == null) {
            System.out.println("isOwner: replyUserId가 null입니다.");
            return false;
        }
        boolean result = replyUserId.equals(userId);
        System.out.println("isOwner: replyUserId=" + replyUserId + ", userId=" + userId + ", result=" + result);
        return result;
    }

    /**
     * 댓글 내용 수정
     * 
     * @param newComment 새로운 댓글 내용
     */
    public void update(String newComment) {
        if (newComment == null || newComment.trim().isEmpty()) {
            throw new Exception400("댓글 내용은 필수입니다");
        }
        if (newComment.length() > 500) {
            throw new Exception400("댓글은 500자 이하여야 합니다");
        }
        this.comment = newComment;
    }
}

