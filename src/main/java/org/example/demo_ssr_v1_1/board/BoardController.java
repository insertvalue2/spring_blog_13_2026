package org.example.demo_ssr_v1_1.board;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1.reply.ReplyService;
import org.example.demo_ssr_v1_1.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 게시글 Controller (표현 계층)
 * 
 * 핵심 개념:
 * 1. Controller의 역할:
 *    - HTTP 요청을 받아서 처리
 *    - 요청 데이터 검증 및 파라미터 바인딩
 *    - Service 레이어에 비즈니스 로직 위임
 *    - 응답 데이터를 View에 전달
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
 *    - @Autowired 대신 생성자 주입을 사용 (권장 방식)
 * 
 * 5. Controller의 책임:
 *    - HTTP 요청/응답 처리
 *    - 세션 관리
 *    - View 이름 반환
 *    - 비즈니스 로직은 Service에 위임
 */
@RequiredArgsConstructor // DI (의존성 주입)
@Controller // IoC (제어의 역전)
public class BoardController {

    // Service 레이어 주입
    // Controller는 비즈니스 로직을 직접 처리하지 않고 Service에 위임
    private final BoardService boardService;
    private final ReplyService replyService;

    /**
     * 게시글 수정 화면 요청
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - View에 데이터 전달
     * 
     * @param id 게시글 ID
     * @param model View에 전달할 데이터
     * @param session 세션 (로그인한 사용자 정보)
     * @return View 이름
     */
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. Service에 비즈니스 로직 위임
        // - 게시글 조회
        // - 인가 검사 (소유자 확인)
        // - ResponseDTO로 반환 (OSIV False 환경 대응)
        BoardResponse.UpdateFormDTO board = boardService.게시글수정화면(id, sessionUser.getId());

        // 3. View에 데이터 전달
        model.addAttribute("board", board);
        return "board/update-form";
    }

    /**
     * 게시글 수정 요청 기능
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - 리다이렉트 처리
     * 
     * @param id 게시글 ID
     * @param updateDTO 게시글 수정 DTO
     * @param session 세션 (로그인한 사용자 정보)
     * @return 리다이렉트 URL
     */
    @PostMapping("/board/{id}/update")
    public String updateProc(@PathVariable Long id,
                             BoardRequest.UpdateDTO updateDTO, HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. Service에 비즈니스 로직 위임
        // - 게시글 조회
        // - 인가 검사 (소유자 확인)
        // - 게시글 수정 (더티 체킹)
        boardService.게시글수정(id, updateDTO, sessionUser.getId());

        return "redirect:/board/list";
    }


    /**
     * 게시글 목록 화면 요청 (페이징)
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 페이징 파라미터 처리 (page, size)
     * - Service에 비즈니스 로직 위임
     * - View에 데이터 전달 (페이징 정보 포함)
     * 
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 5)
     * @param model View에 전달할 데이터
     * @return View 이름
     */
    @GetMapping({"/board/list", "/"})
    public String boardList(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "5") int size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            Model model) {
        // 페이지 번호 변환: 사용자는 1부터 시작하는 페이지 번호를 사용하지만,
        // Spring Data JPA의 Pageable은 0부터 시작하므로 1을 빼서 변환
        int pageIndex = Math.max(0, page - 1);
        
        // Service에 비즈니스 로직 위임
        // - 게시글 목록 조회 (페이징 처리, 검색 포함, 생성일 기준 내림차순 정렬)
        // - 페이징 정보가 포함된 PageDTO 반환 (OSIV False 환경 대응)
        BoardResponse.PageDTO boardPage = boardService.게시글목록조회(pageIndex, size, keyword);
        
        // View에 데이터 전달
        // boardPage 객체에 페이징 정보와 게시글 목록이 모두 포함되어 있음
        // keyword도 전달하여 검색어 유지 (페이지 이동 시 검색어 유지)
        // keyword가 null이면 빈 문자열로 전달 (Mustache 템플릿 오류 방지)
        model.addAttribute("boardPage", boardPage);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "board/list";
    }

    /**
     * 게시글 작성 화면 요청
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 인증 검사는 LoginInterceptor가 처리
     * - View 이름 반환
     * 
     * @param session 세션 (로그인한 사용자 정보)
     * @return View 이름
     */
    @GetMapping("/board/save")
    public String saveFrom(HttpSession session) {
        // 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        return "board/save-form";
    }

    /**
     * 게시글 작성 요청 기능
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - 리다이렉트 처리
     * 
     * @param saveDTO 게시글 작성 DTO
     * @param session 세션 (로그인한 사용자 정보)
     * @return 리다이렉트 URL
     */
    @PostMapping("/board/save")
    public String saveProc(BoardRequest.SaveDTO saveDTO, HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. Service에 비즈니스 로직 위임
        // - DTO를 엔티티로 변환
        // - 게시글 저장 (INSERT)
        boardService.게시글작성(saveDTO, sessionUser);

        return "redirect:/";
    }

    /**
     * 게시글 삭제 요청 기능
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - 리다이렉트 처리
     * 
     * @param id 게시글 ID
     * @param session 세션 (로그인한 사용자 정보)
     * @return 리다이렉트 URL
     */
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. Service에 비즈니스 로직 위임
        // - 게시글 조회
        // - 인가 검사 (소유자 확인)
        // - 게시글 삭제
        boardService.게시글삭제(id, sessionUser.getId());

        return "redirect:/";
    }

    /**
     * 게시글 상세 보기 화면 요청
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - Service에 비즈니스 로직 위임
     * - View에 데이터 전달 (ResponseDTO 사용)
     * 
     * @param id 게시글 ID
     * @param model View에 전달할 데이터
     * @param session 세션 (로그인한 사용자 정보)
     * @return View 이름
     */
    @GetMapping("/board/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        // Service에 비즈니스 로직 위임
        // - 게시글 조회
        // - ResponseDTO로 반환 (OSIV False 환경 대응)
        BoardResponse.DetailDTO board = boardService.게시글상세조회(id);

        // 세션에서 로그인 사용자 정보 조회 (없을 수도 있음)
        User sessionUser = (User) session.getAttribute("sessionUser");
        
        // 게시글 소유자 여부 확인
        // DetailDTO의 userId와 세션 사용자 ID를 비교
        boolean isOwner = false;
        if (sessionUser != null && board.getUserId() != null) {
            isOwner = board.getUserId().equals(sessionUser.getId());
        }

        // 댓글 목록 조회
        // Service에 비즈니스 로직 위임
        // - 댓글 목록 조회 (생성일 기준 오름차순)
        // - ResponseDTO로 반환 (OSIV False 환경 대응)
        Long sessionUserId = sessionUser != null ? sessionUser.getId() : null;
        List<org.example.demo_ssr_v1_1.reply.ReplyResponse.ListDTO> replyList = replyService.댓글목록조회(id, sessionUserId);

        // View에 데이터 전달
        model.addAttribute("board", board);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("replyList", replyList);
        return "board/detail";
    }

}
