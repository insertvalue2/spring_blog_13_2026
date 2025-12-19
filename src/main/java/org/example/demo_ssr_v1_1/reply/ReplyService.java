package org.example.demo_ssr_v1_1.reply;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception404;
import org.example.demo_ssr_v1_1.board.Board;
import org.example.demo_ssr_v1_1.board.BoardRepository;
import org.example.demo_ssr_v1_1.user.User;
import org.example.demo_ssr_v1_1.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 댓글 서비스 레이어 (Service Layer)
 * 
 * 핵심 개념:
 * 1. 서비스 레이어의 역할:
 *    - 비즈니스 로직을 처리하는 계층
 *    - Controller와 Repository 사이의 중간 계층
 *    - 트랜잭션 관리 (@Transactional)
 *    - 여러 Repository를 조합하여 복잡한 비즈니스 로직 처리
 * 
 * 2. @Service:
 *    - Spring이 이 클래스를 서비스 빈으로 등록
 *    - @Component의 특수한 형태
 *    - 비즈니스 로직을 담당하는 클래스임을 명시
 * 
 * 3. @RequiredArgsConstructor:
 *    - final 필드에 대한 생성자를 자동 생성
 *    - 의존성 주입(DI)을 위한 생성자 주입 방식
 * 
 * 4. @Transactional:
 *    - 메서드 실행 시 트랜잭션을 시작하고 종료 시 커밋
 *    - 예외 발생 시 자동 롤백
 *    - 더티 체킹(Dirty Checking) 활성화
 */
@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    /**
     * 댓글 목록 조회
     * 
     * OSIV False 환경 대응:
     * - 트랜잭션 내에서 필요한 데이터를 모두 조회하고 DTO로 변환
     * - JOIN FETCH로 Reply, User를 한 번의 쿼리로 함께 조회
     * - 엔티티를 DTO로 변환하여 반환 (LAZY 로딩 문제 방지)
     * 
     * @param boardId 게시글 ID
     * @param sessionUserId 세션 사용자 ID (댓글 소유자 여부 확인용, null 가능)
     * @return 댓글 목록 DTO (생성일 기준 오름차순)
     */
    @Transactional(readOnly = true)
    public List<ReplyResponse.ListDTO> 댓글목록조회(Long boardId, Long sessionUserId) {
        // JOIN FETCH로 Reply, User, Board를 한 번의 쿼리로 함께 조회
        // N+1 문제 해결 및 OSIV False 환경 대응
        List<Reply> replyList = replyRepository.findByBoardIdWithUser(boardId);
        
        // 트랜잭션 내에서 엔티티를 DTO로 변환
        // Stream을 사용하여 각 Reply 엔티티를 ListDTO로 변환
        // 람다 표현식 사용: reply -> new ReplyResponse.ListDTO(reply, sessionUserId)
        return replyList.stream()
                .map(reply -> new ReplyResponse.ListDTO(reply, sessionUserId))
                .collect(Collectors.toList());
    }

    /**
     * 댓글 작성
     * 
     * 트랜잭션:
     * - 기본 트랜잭션 (읽기/쓰기)
     * - save() 메서드 실행 시 INSERT 쿼리 실행
     * 
     * 중요: 세션의 User 객체는 detached 상태일 수 있으므로,
     * User ID를 받아서 현재 트랜잭션에서 User를 다시 조회합니다.
     * 
     * @param saveDTO 댓글 작성 DTO
     * @param userId 작성자 ID (세션에서 가져온 사용자 ID)
     * @return 저장된 댓글 엔티티
     * @throws Exception404 게시글이 없을 경우
     */
    @Transactional
    public Reply 댓글작성(ReplyRequest.SaveDTO saveDTO, Long userId) {
        // 유효성 검사
        saveDTO.validate();
        
        // 게시글 조회
        Board board = boardRepository.findById(saveDTO.getBoardId())
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다"));
        
        // User 조회 (현재 트랜잭션에서 조회하여 영속성 컨텍스트에 포함)
        // 세션의 User 객체는 detached 상태일 수 있으므로 다시 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));
        
        // 디버깅: 댓글 작성 시 사용자 정보 확인
        System.out.println("=== 댓글 작성 ===");
        System.out.println("사용자 ID: " + user.getId());
        System.out.println("사용자명: " + user.getUsername());
        System.out.println("================");
        
        // DTO를 엔티티로 변환
        Reply reply = saveDTO.toEntity(board, user);
        
        // JpaRepository의 save() 메서드
        // - ID가 null이면 INSERT
        // - ID가 있으면 UPDATE
        return replyRepository.save(reply);
    }

    /**
     * 댓글 조회 (삭제 시 게시글 ID를 얻기 위해 사용)
     * 
     * @param id 댓글 ID
     * @return 댓글 엔티티
     * @throws Exception404 댓글이 없을 경우
     */
    @Transactional(readOnly = true)
    public Reply 댓글조회(Long id) {
        return replyRepository.findByIdWithUser(id)
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다"));
    }

    /**
     * 댓글 삭제
     * 
     * 삭제 처리:
     * - deleteById() 메서드 사용
     * - 엔티티가 없으면 EmptyResultDataAccessException 발생
     * 
     * @param id 댓글 ID
     * @param userId 현재 로그인한 사용자 ID
     * @return 삭제된 댓글이 속한 게시글 ID (리다이렉트용)
     * @throws Exception404 댓글이 없을 경우
     * @throws Exception403 삭제 권한이 없을 경우
     */
    @Transactional
    public Long 댓글삭제(Long id, Long userId) {
        // 댓글 조회 (작성자 정보 및 게시글 정보 포함, JOIN FETCH 사용)
        Reply reply = replyRepository.findByIdWithUser(id)
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다"));

        // 디버깅: 권한 체크 전 값 확인
        Long replyUserId = reply.getUser() != null ? reply.getUser().getId() : null;
        System.out.println("=== 댓글 삭제 권한 체크 ===");
        System.out.println("댓글 ID: " + id);
        System.out.println("댓글 작성자 ID: " + replyUserId);
        System.out.println("세션 사용자 ID: " + userId);
        System.out.println("isOwner 결과: " + reply.isOwner(userId));
        System.out.println("=========================");

        // 인가 검사: 댓글 소유자인지 확인
        if (!reply.isOwner(userId)) {
            throw new Exception403("댓글 삭제 권한이 없습니다. (댓글 작성자 ID: " + replyUserId + ", 세션 사용자 ID: " + userId + ")");
        }

        // 게시글 ID 저장 (트랜잭션 내에서 조회)
        Long boardId = reply.getBoard().getId();

        // JpaRepository의 deleteById() 메서드
        // 엔티티가 없으면 EmptyResultDataAccessException 발생
        replyRepository.deleteById(id);
        
        // 게시글 ID 반환 (리다이렉트용)
        return boardId;
    }
}

