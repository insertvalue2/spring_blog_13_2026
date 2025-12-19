package org.example.demo_ssr_v1_1.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 댓글 Repository 인터페이스
 * 
 * 핵심 개념:
 * 1. JpaRepository<Reply, Long>: Spring Data JPA가 제공하는 인터페이스
 * 2. 쿼리 메서드: 메서드 이름만으로 쿼리 자동 생성
 * 3. @Query: 복잡한 쿼리는 직접 작성
 */
@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    
    /**
     * 게시글 ID로 댓글 목록 조회 (작성자 정보 포함, JOIN FETCH 사용)
     * 
     * JOIN FETCH를 사용하여 Reply, Board, User를 한 번의 쿼리로 함께 조회합니다.
     * OSIV False 환경에서도 안전하게 사용 가능합니다.
     * 
     * 생성되는 SQL:
     * SELECT r.*, b.*, u.* 
     * FROM reply_tb r 
     * INNER JOIN board_tb b ON r.board_id = b.id 
     * INNER JOIN user_tb u ON r.user_id = u.id 
     * WHERE r.board_id = ?
     * ORDER BY r.created_at ASC
     * 
     * @param boardId 게시글 ID
     * @return 댓글 목록 (생성일 기준 오름차순)
     */
    @Query("SELECT r FROM Reply r JOIN FETCH r.user JOIN FETCH r.board WHERE r.board.id = :boardId ORDER BY r.createdAt ASC")
    List<Reply> findByBoardIdWithUser(@Param("boardId") Long boardId);
    
    /**
     * 댓글 ID로 조회 (작성자 정보 포함, JOIN FETCH 사용)
     * 
     * @param id 댓글 ID
     * @return 댓글 (Optional)
     */
    @Query("SELECT r FROM Reply r JOIN FETCH r.user JOIN FETCH r.board WHERE r.id = :id")
    Optional<Reply> findByIdWithUser(@Param("id") Long id);
    
    /**
     * 게시글 ID로 댓글 삭제
     * 
     * 게시글 삭제 시 외래키 제약조건 위반을 방지하기 위해
     * 게시글에 속한 모든 댓글을 먼저 삭제하는 데 사용
     * 
     * @param boardId 게시글 ID
     */
    void deleteByBoardId(Long boardId);
}

