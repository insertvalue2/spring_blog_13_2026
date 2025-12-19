package org.example.demo_ssr_v1_1.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor // DI (의존성 주입)
@Controller // IoC (제어의 역전)
public class UserController {

    // Service 레이어 주입
    // Controller는 비즈니스 로직을 직접 처리하지 않고 Service에 위임
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Value("${oauth.kakao.client-id}")
    private String kakao_client_id;

    @Value("${tenco.key}") // 임시 비밀번호 키 주입
    private String tencoKey;


    /**
     * 카카오 로그인 콜백 처리
     * [흐름] 1. 인가 코드 받기 -> 2. 토큰 발급 요청 -> 3. 사용자 정보 요청 -> 4. 로그인/회원가입 처리
     */
    @GetMapping("/user/kakao")
    //@ResponseBody
    public String kakaoCallback(@RequestParam(name = "code") String code, HttpSession session) {

        // 1. 인가 코드 확인 (디버깅용)
        System.out.println("1. Kakao 인가 코드 수신 완료: " + code);

        // -----------------------------------------------------------
        // 2. 토큰 발급 요청 (POST: https://kauth.kakao.com/oauth/token)
        // -----------------------------------------------------------
        RestTemplate tokenRt = new RestTemplate();

        // 2-1. 헤더 생성 (MIME 타입 설정)
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        // application/x-www-form-urlencoded 것은 데이터를 key1=value1&key2=value2 형태(HTML Form 태그 방식)로 보내겠다 의미 입니다.

        // 2-2. 바디 생성 (MultiValueMap 사용)
        // RestTemplate은 바디(Body)에 MultiValueMap 타입의 객체가 들어오면, "아! 이걸 폼 데이터(key=value) 형식으로 변환해서 보내야겠구나"라고 인식하고 자동으로 변환
        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
        tokenParams.add("grant_type", "authorization_code");
        tokenParams.add("client_id", kakao_client_id);
        tokenParams.add("redirect_uri", "http://localhost:8080/user/kakao");
        tokenParams.add("code", code);
        // !!! 시크릿키 비활성화 했을 경우 !!!
        tokenParams.add("client_secret", "AhbShsedonaVOqoMyRgl0sjX6ZXNsbHU");

        // 2-3. 헤더 + 바디 결합
        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);

        // 2-4. 요청 및 응답 받기
        ResponseEntity<UserResponse.OAuthToken> tokenResponse = tokenRt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                tokenRequest,
                UserResponse.OAuthToken.class
        );

        UserResponse.OAuthToken oauthToken = tokenResponse.getBody();
        System.out.println("2. Access Token 발급 완료: " + oauthToken.getAccessToken());

        // -----------------------------------------------------------
        // 3. 사용자 정보 요청 (POST: https://kapi.kakao.com/v2/user/me)
        // -----------------------------------------------------------
        RestTemplate profileRt = new RestTemplate();

        // 3-1. 헤더 생성 (Bearer + Access Token)
        HttpHeaders profileHeaders = new HttpHeaders();
        profileHeaders.add("Authorization", "Bearer " + oauthToken.getAccessToken());
        profileHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 3-2. 요청 엔티티 생성 (바디 없음)
        HttpEntity<Void> profileRequest = new HttpEntity<>(profileHeaders);

        // 3-3. 요청 및 응답 받기
        ResponseEntity<UserResponse.KakaoProfile> profileResponse = profileRt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                profileRequest,
                UserResponse.KakaoProfile.class
        );

        UserResponse.KakaoProfile kakaoProfile = profileResponse.getBody();
        System.out.println("3. 카카오 프로필 정보 수신 완료: " + kakaoProfile);

        // -----------------------------------------------------------
        // 4. 강제 회원가입 및 로그인 처리
        // -----------------------------------------------------------

        // 4-1. 고유한 username 생성 (중복 방지용: 닉네임_카카오ID)
        String username = kakaoProfile.getProperties().getNickname() + "_" + kakaoProfile.getId();
        System.out.println("4. 고유한 username : " + username);

        // 4-2. 회원 가입 여부 확인 (UserService 활용)
        User userOrigin = userService.사용자이름조회(username);

        if (userOrigin == null) {
            System.out.println("4. 기존 회원이 아니므로 자동 회원가입을 진행합니다.");

            // 회원가입용 엔티티 생성
            User newUser = User.builder()
                    .username(username)
                    .password(tencoKey) // 임시 비밀번호 (DB Not Null 제약 대응)
                    .email(username + "@kakao.com") // 임의의 이메일 (선택사항)
                    .provider(OAuthProvider.KAKAO) // ★ 로그인 경로 설정
                    .build();

            // 프로필 이미지가 있다면 설정
            String profileImage = kakaoProfile.getProperties().getThumbnailImage();
            if (profileImage != null && !profileImage.isEmpty()) {
                newUser.setProfileImage(profileImage); // URL 그대로 저장 (외부 링크)
            }

            userService.소셜회원가입(newUser);
            userOrigin = newUser; // !! 필수
        } else {
            System.out.println("4. 이미 가입된 회원입니다. 로그인을 진행합니다.");
        }

        // 4-3. 세션 등록 (로그인 처리)
        session.setAttribute("sessionUser", userOrigin);

        return "redirect:/";
    }

//
//    @GetMapping("/user/kakao")
//    @ResponseBody
//    public String kakaoCallbackCode(@RequestParam(name = "code") String code) {
//        // /user/kakao - 로그인 인터셉터 제외 처리
//        String authorize_code = code;
//        // 1. 인가 코드 확인 (디버깅용)
//        System.out.println("1. Kakao 인가 코드 수신 완료: " + code);
//        // -----------------------------------------------------------
//        // 2. 토큰 발급 요청 (POST: https://kauth.kakao.com/oauth/token)
//        // -----------------------------------------------------------
//        RestTemplate tokenRt = new RestTemplate();
//        // 2-1. 헤더 생성 (MIME 타입 설정)
//        HttpHeaders tokenHeaders = new HttpHeaders();
//        tokenHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//        // application/x-www-form-urlencoded 것은 데이터를 key1=value1&key2=value2 형태(HTML Form 태그 방식)로 보내겠다 의미 입니다.
//
//        // 2-2. 바디 생성 (MultiValueMap 사용)
//        // RestTemplate은 바디(Body)에 MultiValueMap 타입의 객체가 들어오면, "아! 이걸 폼 데이터(key=value) 형식으로 변환해서 보내야겠구나"라고 인식하고 자동으로 변환
//        MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
//        tokenParams.add("grant_type", "authorization_code");
//        tokenParams.add("client_id", kakao_client_id);
//        tokenParams.add("redirect_uri", "http://localhost:8080/user/kakao");
//        tokenParams.add("code", code);
//        // 시크릿키 비활성화 했을 경우
//        tokenParams.add("client_secret", "AhbShsedonaVOqoMyRgl0sjX6ZXNsbHU");
//
//        // 2-3. 헤더 + 바디 결합
//        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
//
//        // 2-4. 요청 및 응답 받기 (카카오 서비스 서버에 요청>
//        ResponseEntity<String> tokenResponse = tokenRt.exchange(
//                "https://kauth.kakao.com/oauth/token",
//                HttpMethod.POST,
//                tokenRequest,
//                String.class
//        );
//
//        String jsonBody = tokenResponse.getBody();
//
//        try {
//            // 3. JSON 문자열을 JsonNode 트리 구조로 변환
//            JsonNode jsonNode = objectMapper.readTree(jsonBody);
//
//            // 4. 값 추출 (필드명으로 찾아서 문자열로 반환)
//            String tokenType = jsonNode.get("token_type").asText();
//            String accessToken = jsonNode.get("access_token").asText();
//
//            // 5. 출력 확인
//            System.out.println("토큰 타입: " + tokenType);
//            System.out.println("액세스 토큰: " + accessToken);
//
//
//
//        } catch (JsonProcessingException e) {
//            e.printStackTrace(); // 파싱 중 에러 처리
//        }
//        return tokenResponse.toString();
//     }

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
