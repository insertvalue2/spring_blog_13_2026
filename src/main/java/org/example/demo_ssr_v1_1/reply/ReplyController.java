package org.example.demo_ssr_v1_1.reply;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 댓글 Controller (표현 계층)
 * 
 * 핵심 개념:
 * 1. Controller의 역할:
 *    - HTTP 요청을 받아서 처리
 *    - 요청 데이터 검증 및 파라미터 바인딩
 *    - Service 레이어에 비즈니스 로직 위임
 *    - 리다이렉트 처리
 * 
 * 2. 계층 구조 (3-Tier Architecture):
 *    Controller (표현 계층) ← 현재 위치
 *      ↓ 요청
 *    Service (비즈니스 계층)
 *      ↓ 요청
 *    Repository (데이터 접근 계층)
 * 
 * 3. @Controller:
 *    - Spring MVC의 컨트롤러로 등록
 *    - @Component의 특수한 형태
 *    - HTTP 요청을 처리하는 클래스임을 명시
 * 
 * 4. @RequiredArgsConstructor:
 *    - final 필드에 대한 생성자를 자동 생성
 *    - 의존성 주입(DI)을 위한 생성자 주입 방식
 * 
 * 5. Controller의 책임:
 *    - HTTP 요청/응답 처리
 *    - 세션 관리
 *    - 리다이렉트 처리
 *    - 비즈니스 로직은 Service에 위임
 */
@RequiredArgsConstructor // DI (의존성 주입)
@Controller // IoC (제어의 역전)
public class ReplyController {

    // Service 레이어 주입
    // Controller는 비즈니스 로직을 직접 처리하지 않고 Service에 위임
    private final ReplyService replyService;

    /**
     * 댓글 작성 요청 기능
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - 리다이렉트 처리
     * 
     * @param saveDTO 댓글 작성 DTO
     * @param session 세션 (로그인한 사용자 정보)
     * @return 리다이렉트 URL
     */
    @PostMapping("/reply/save")
    public String saveProc(ReplyRequest.SaveDTO saveDTO, HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. Service에 비즈니스 로직 위임
        // - 유효성 검사
        // - 게시글 조회
        // - User 조회 (현재 트랜잭션에서 조회하여 영속성 컨텍스트에 포함)
        // - DTO를 엔티티로 변환
        // - 댓글 저장 (INSERT)
        // 중요: 세션의 User 객체는 detached 상태일 수 있으므로 User ID만 전달
        replyService.댓글작성(saveDTO, sessionUser.getId());

        // 3. 댓글 작성 후 게시글 상세보기로 리다이렉트
        return "redirect:/board/" + saveDTO.getBoardId();
    }

    /**
     * 댓글 삭제 요청 기능
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - 리다이렉트 처리
     * 
     * @param id 댓글 ID
     * @param session 세션 (로그인한 사용자 정보)
     * @return 리다이렉트 URL
     */
    @PostMapping("/reply/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. Service에 비즈니스 로직 위임
        // - 댓글 조회 (JOIN FETCH로 작성자 및 게시글 정보 포함)
        // - 인가 검사 (소유자 확인)
        // - 댓글 삭제
        // - 게시글 ID 반환 (트랜잭션 내에서 조회)
        Long boardId = replyService.댓글삭제(id, sessionUser.getId());

        // 3. 댓글 삭제 후 게시글 상세보기로 리다이렉트
        return "redirect:/board/" + boardId;
    }
}

