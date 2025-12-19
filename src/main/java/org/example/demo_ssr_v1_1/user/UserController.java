package org.example.demo_ssr_v1_1.user;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 사용자 Controller (표현 계층)
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
public class UserController {

    // Service 레이어 주입
    // Controller는 비즈니스 로직을 직접 처리하지 않고 Service에 위임
    private final UserService userService;

    /**
     * 회원정보 보기 화면 요청
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - View에 데이터 전달
     * 
     * @param model View에 전달할 데이터
     * @param session 세션 (로그인한 사용자 정보)
     * @return View 이름
     */
    @GetMapping("/user/detail")
    public String detailForm(Model model, HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. Service에 비즈니스 로직 위임
        // - 회원정보 조회
        // - 인가 검사 (소유자 확인)
        User user = userService.회원정보수정화면(sessionUser.getId());

        // 3. View에 데이터 전달
        model.addAttribute("user", user);
        return "user/detail";
    }

    /**
     * 회원 정보 수정 화면 요청
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - View에 데이터 전달
     * 
     * @param model View에 전달할 데이터
     * @param session 세션 (로그인한 사용자 정보)
     * @return View 이름
     */
    @GetMapping("/user/update")
    public String updateForm(Model model, HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        // 2. Service에 비즈니스 로직 위임
        // - 회원정보 조회
        // - 인가 검사 (소유자 확인)
        User user = userService.회원정보수정화면(sessionUser.getId());

        // 3. View에 데이터 전달
        model.addAttribute("user", user);
        return "user/update-form";
    }


    /**
     * 회원정보 수정 기능 요청
     * 
     * 프로필 이미지 수정 처리:
     * - 새 이미지 업로드: 기존 이미지 삭제 → 새 이미지 저장 → DB 업데이트
     * - 이미지 업로드 안 함: 기존 이미지 유지 → DB 변경 없음
     * 
     * Controller의 역할:
     * - HTTP 요청 처리 (프로필 이미지 포함, 선택사항)
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - 세션 갱신 (수정된 사용자 정보 반영)
     * - 리다이렉트 처리
     * 
     * @param updateDTO 회원정보 수정 DTO (프로필 이미지 포함, 선택사항)
     *                  - profileImage 필드에 MultipartFile 객체가 자동 바인딩됨
     * @param session 세션 (로그인한 사용자 정보)
     * @return 리다이렉트 URL
     */
    @PostMapping("/user/update")
    public String updateProc(UserRequest.UpdateDTO updateDTO, HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        try {
            // 2. Service에 비즈니스 로직 위임
            // - 회원정보 조회
            // - 인가 검사 (소유자 확인)
            // - 유효성 검사
            // - 회원정보 수정 (더티 체킹)
            User updateUser = userService.회원정보수정(updateDTO, sessionUser.getId());

            // 3. 세션에 정보 갱신
            // 수정된 사용자 정보를 세션에 다시 저장
            session.setAttribute("sessionUser", updateUser);

            // 4. 수정 후 리다이렉트 처리 - 회원정보 보기 화면으로 이동
            return "redirect:/user/detail";
        } catch (Exception e) {
            // 예외 발생 시 수정 화면으로 다시 이동
            return "user/update-form";
        }
    }

    /**
     * 프로필 이미지 삭제 기능 요청
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션에서 사용자 정보 추출
     * - Service에 비즈니스 로직 위임
     * - 세션 갱신
     * - 리다이렉트 처리
     * 
     * @param session 세션 (로그인한 사용자 정보)
     * @return 리다이렉트 URL
     */
    @PostMapping("/user/profile-image/delete")
    public String deleteProfileImage(HttpSession session) {
        // 1. 인증 검사: LoginInterceptor가 처리 (인터셉터를 통과했다는 것은 로그인된 사용자임)
        User sessionUser = (User) session.getAttribute("sessionUser");

        try {
            // 2. Service에 비즈니스 로직 위임
            // - 회원정보 조회
            // - 인가 검사 (소유자 확인)
            // - 프로필 이미지 파일 삭제
            // - DB에서 프로필 이미지 필드 null로 업데이트
            User updateUser = userService.프로필이미지삭제(sessionUser.getId());

            // 3. 세션에 정보 갱신
            // 프로필 이미지가 삭제된 사용자 정보를 세션에 다시 저장
            session.setAttribute("sessionUser", updateUser);

            // 4. 삭제 후 리다이렉트 처리 - 회원정보 보기 화면으로 이동
            return "redirect:/user/detail";
        } catch (Exception e) {
            // 예외 발생 시 회원정보 보기 화면으로 다시 이동
            return "redirect:/user/detail";
        }
    }



    /**
     * 로그아웃 기능 요청
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - 세션 무효화
     * - 리다이렉트 처리
     * 
     * @param session 세션
     * @return 리다이렉트 URL
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 무효화
        // 세션에 저장된 모든 정보 삭제
        session.invalidate();
        return "redirect:/";
    }

    /**
     * 로그인 화면 요청
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - View 이름 반환
     * 
     * @return View 이름
     */
    @GetMapping("/login")
    public String loginForm() {
        return "user/login-form";
    }

    /**
     * 로그인 기능 요청
     * 
     * 세션 기반 인증 처리:
     * - JWT 토큰 기반 인증이 아닌 세션 기반 인증 사용
     * - 로그인 성공 시 사용자 정보를 세션에 저장
     * - 다음 요청부터 세션에서 사용자 정보 확인
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - Service에 비즈니스 로직 위임
     * - 세션에 사용자 정보 저장
     * - 리다이렉트 처리
     * 
     * @param loginDTO 로그인 DTO
     * @param session 세션
     * @return 리다이렉트 URL 또는 View 이름
     */
    @PostMapping("/login")
    public String loginProc(UserRequest.LoginDTO loginDTO, HttpSession session) {
        try {
            // 1. Service에 비즈니스 로직 위임
            // - 유효성 검사
            // - 사용자명과 비밀번호로 사용자 조회
            // - 로그인 성공/실패 처리
            User sessionUser = userService.로그인(loginDTO);

            // 2. 세션에 사용자 정보 저장
            // 웹 서버는 상태를 유지하지 않으므로 세션에 사용자 정보를 저장해야
            // 다음 요청에서 사용자를 식별할 수 있음
            session.setAttribute("sessionUser", sessionUser);

            return "redirect:/";
        } catch (Exception e) {
            // 로그인 실패 시 다시 로그인 화면으로 처리
            return "user/login-form";
        }
    }




    /**
     * 회원가입 화면 요청
     * 
     * Controller의 역할:
     * - HTTP 요청 처리
     * - View 이름 반환
     * 
     * @return View 이름
     */
    @GetMapping("/join")
    public String joinFrom() {
        return "user/join-form";
    }

    /**
     * 회원가입 기능 요청
     * 
     * 파일 업로드 처리 흐름:
     * 1. 브라우저에서 form 태그로 파일과 함께 POST 요청
     * 2. Spring이 enctype="multipart/form-data"를 자동으로 파싱
     * 3. MultipartFile 객체로 파일 데이터를 joinDTO에 자동 바인딩
     * 4. Service에서 파일 저장 및 DB 저장 처리
     * 
     * Controller의 역할:
     * - HTTP 요청 처리 (파일 포함)
     * - Service에 비즈니스 로직 위임
     * - 리다이렉트 처리
     * 
     * @param joinDTO 회원가입 DTO (프로필 이미지 포함, 선택사항)
     *                - profileImage 필드에 MultipartFile 객체가 자동 바인딩됨
     *                - 파일이 없으면 null 또는 empty 상태
     * @return 리다이렉트 URL
     */
    @PostMapping("/join")
    public String joinProc(UserRequest.JoinDTO joinDTO) {
        // Service에 비즈니스 로직 위임
        // - 유효성 검사
        // - 사용자명 중복 체크
        // - 프로필 이미지 저장 (선택사항, 파일이 있으면)
        // - 회원정보 저장 (INSERT)
        userService.회원가입(joinDTO);

        return "redirect:/login";
    }

}
