package org.example.demo_ssr_v1_1.user;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception404;
import org.example.demo_ssr_v1_1._core.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * 사용자 서비스 레이어 (Service Layer)
 * 
 * 핵심 개념:
 * 1. 서비스 레이어의 역할:
 *    - 비즈니스 로직을 처리하는 계층
 *    - Controller와 Repository 사이의 중간 계층
 *    - 트랜잭션 관리 (@Transactional)
 *    - 여러 Repository를 조합하여 복잡한 비즈니스 로직 처리
 * 
 * 2. 계층 구조 (3-Tier Architecture):
 *    Controller (표현 계층) 
 *      ↓ 요청
 *    Service (비즈니스 계층) ← 현재 위치
 *      ↓ 요청
 *    Repository (데이터 접근 계층)
 *      ↓
 *    Database
 * 
 * 3. @Service:
 *    - Spring이 이 클래스를 서비스 빈으로 등록
 *    - @Component의 특수한 형태
 *    - 비즈니스 로직을 담당하는 클래스임을 명시
 * 
 * 4. @RequiredArgsConstructor:
 *    - final 필드에 대한 생성자를 자동 생성
 *    - 의존성 주입(DI)을 위한 생성자 주입 방식
 *    - @Autowired 대신 생성자 주입을 사용 (권장 방식)
 * 
 * 5. @Transactional:
 *    - 메서드 실행 시 트랜잭션을 시작하고 종료 시 커밋
 *    - 예외 발생 시 자동 롤백
 *    - 더티 체킹(Dirty Checking) 활성화
 *    - 여러 DB 작업을 하나의 트랜잭션으로 묶어 일관성 보장
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원가입 처리 (프로필 이미지 포함)
     * 
     * 비즈니스 로직:
     * 1. 유효성 검사 (DTO에서 처리)
     * 2. 사용자명 중복 체크
     * 3. 프로필 이미지 저장 (선택사항)
     * 4. 기본 권한(USER) 추가
     * 5. 엔티티 저장
     * 
     * 트랜잭션:
     * - 기본 트랜잭션 (읽기/쓰기)
     * - save() 메서드 실행 시 INSERT 쿼리 실행
     * 
     * @param joinDTO 회원가입 DTO (프로필 이미지 포함)
     * @return 저장된 사용자 엔티티
     * @throws Exception400 사용자명이 이미 존재할 경우 또는 파일 저장 실패 시
     */
    @Transactional
    public User 회원가입(UserRequest.JoinDTO joinDTO) {
        // 1. 유효성 검사
        joinDTO.validate();

        // 2. 사용자명 중복 체크
        // Optional의 isPresent(): 값이 있으면 true, 없으면 false
        if (userRepository.findByUsername(joinDTO.getUsername()).isPresent()) {
            throw new Exception400("이미 존재하는 사용자 이름입니다");
        }

        // 3. 프로필 이미지 저장 (선택사항)
        // 중요: 프로필 이미지는 필수가 아닌 선택사항입니다!
        // 사용자가 이미지를 업로드하지 않아도 회원가입은 정상적으로 진행됩니다.
        // 
        // 파일 업로드 처리 흐름:
        // 1) joinDTO.getProfileImage()가 null이거나 비어있으면 → 이미지 없이 회원가입 진행
        // 2) 파일이 있으면 → 파일 검증 → 파일 저장 → 파일명을 DB에 저장
        String profileImageFilename = null;  // 초기값은 null (이미지 없음)
        
        // 파일이 업로드되었는지 확인
        // MultipartFile의 isEmpty() 메서드: 파일이 없거나 크기가 0이면 true
        if (joinDTO.getProfileImage() != null && !joinDTO.getProfileImage().isEmpty()) {
            try {
                // 3-1. 이미지 파일인지 검증
                // Content-Type이 "image/"로 시작하는지 확인 (예: image/jpeg, image/png)
                if (!FileUtil.isImageFile(joinDTO.getProfileImage())) {
                    throw new Exception400("이미지 파일만 업로드 가능합니다");
                }
                
                // 3-2. 파일을 서버 디스크에 저장
                // FileUtil.saveFile() 메서드가 하는 일:
                // - UUID를 사용하여 고유한 파일명 생성 (중복 방지)
                // - "images/" 디렉토리에 파일 저장
                // - 저장된 파일명 반환 (예: "abc123-456-789-profile.jpg")
                profileImageFilename = FileUtil.saveFile(joinDTO.getProfileImage(), FileUtil.IMAGES_DIR);
                
                // 3-3. profileImageFilename에는 저장된 파일명이 들어있음
                // 이 파일명을 DB의 user_tb.profile_image 컬럼에 저장할 예정
            } catch (IOException e) {
                // 파일 저장 중 오류 발생 시 (예: 디스크 공간 부족, 권한 없음)
                throw new Exception400("파일 저장에 실패했습니다: " + e.getMessage());
            }
        }
        // 파일이 없으면 profileImageFilename은 null로 유지됨
        // → DB에 null로 저장되어 "프로필 이미지 없음" 상태가 됨

        // 4. DTO를 엔티티로 변환 (파일명 포함)
        User user = joinDTO.toEntity(profileImageFilename);

        // 5. 기본 권한 추가 (일반 사용자)
        // 회원가입 시 기본적으로 USER 역할을 부여합니다.
        user.addRole(Role.USER);

        // 6. JpaRepository의 save() 메서드: 엔티티 저장 (INSERT)
        return userRepository.save(user);
    }

    /**
     * 로그인 처리
     * 
     * 비즈니스 로직:
     * 1. 유효성 검사 (DTO에서 처리)
     * 2. 사용자명과 비밀번호로 사용자 조회
     * 3. 로그인 성공/실패 처리
     * 
     * 트랜잭션:
     * - 읽기 전용 트랜잭션 (readOnly = true)
     * - 조회만 하므로 읽기 전용으로 설정
     * 
     * @param loginDTO 로그인 DTO
     * @return 로그인한 사용자 엔티티
     * @throws Exception400 로그인 실패 시 (사용자명 또는 비밀번호 불일치)
     */
    @Transactional(readOnly = true)
    public User 로그인(UserRequest.LoginDTO loginDTO) {
        // 1. 유효성 검사
        loginDTO.validate();

        // 2. 사용자명과 비밀번호로 사용자 조회 (+ 역할 정보까지 함께 조회)
        //    findByUsernameAndPasswordWithRoles():
        //    - User 엔티티와 roles 컬렉션을 LEFT JOIN FETCH로 한 번에 가져옵니다.
        //    - 세션에 저장된 User에서 isAdmin(), getRoleDisplay() 등을 사용할 수 있습니다.
        User sessionUser = userRepository.findByUsernameAndPasswordWithRoles(
                        loginDTO.getUsername(),
                        loginDTO.getPassword())
                .orElse(null); // 로그인 실패 시 null 반환

        // 3. 로그인 실패 처리
        if (sessionUser == null) {
            throw new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다");
        }

        return sessionUser;
    }

    /**
     * 회원정보 수정 화면용 조회 (인가 검사 포함)
     * 
     * 인가 검사:
     * - 자기 자신의 정보만 조회 가능
     * - isOwner() 메서드로 소유자 확인
     * 
     * @param userId 현재 로그인한 사용자 ID
     * @return 사용자 엔티티
     * @throws Exception404 사용자가 없을 경우
     * @throws Exception403 수정 권한이 없을 경우
     */
    @Transactional(readOnly = true)
    public User 회원정보수정화면(Long userId) {
        // 세션의 사용자 ID로 회원정보 조회
        // JpaRepository의 findById()는 Optional<User>를 반환
        // orElseThrow(): Optional이 비어있으면 예외 발생, 있으면 User 객체 반환
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));

        // 자기 자신의 정보만 수정 가능한지 확인
        if (!user.isOwner(userId)) {
            throw new Exception403("회원정보 수정 권한이 없습니다");
        }

        return user;
    }

    /**
     * 회원정보 수정 처리 (프로필 이미지 포함)
     * 
     * 더티 체킹 (Dirty Checking):
     * - 엔티티를 조회한 후 필드 값을 변경
     * - 트랜잭션이 끝날 때 자동으로 UPDATE 쿼리 실행
     * - save()를 호출해도 되지만, @Transactional이 있으면 자동으로 UPDATE 됨
     * 
     * 세션 갱신:
     * - 수정된 사용자 정보를 세션에 다시 저장
     * - Controller에서 처리하도록 엔티티 반환
     * 
     * @param updateDTO 회원정보 수정 DTO (프로필 이미지 포함)
     * @param userId 현재 로그인한 사용자 ID
     * @return 수정된 사용자 엔티티
     * @throws Exception400 파일 저장 실패 시
     * @throws Exception404 사용자가 없을 경우
     * @throws Exception403 수정 권한이 없을 경우
     */
    @Transactional
    public User 회원정보수정(UserRequest.UpdateDTO updateDTO, Long userId) {
        // 1. 수정하려는 회원정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));

        // 2. 인가 검사: 자기 자신의 정보만 수정 가능한지 확인
        if (!user.isOwner(userId)) {
            throw new Exception403("회원정보 수정 권한이 없습니다");
        }

        // 3. 유효성 검사
        updateDTO.validate();

        // 4. 프로필 이미지 처리 (새로운 이미지가 업로드된 경우)
        // 중요: 프로필 이미지 수정도 선택사항입니다!
        // 사용자가 새 이미지를 업로드하지 않으면 기존 이미지를 유지합니다.
        //
        // 프로필 이미지 수정 시나리오:
        // 1) 새 이미지 업로드 → 새 이미지 저장 → 기존 이미지 삭제 → DB 업데이트
        // 2) 이미지 업로드 안 함 → 기존 이미지 유지 → DB 변경 없음
        
        String oldProfileImage = user.getProfileImage();  // 기존 이미지 파일명 저장 (나중에 삭제하기 위해)
        
        // 새 이미지가 업로드되었는지 확인
        if (updateDTO.getProfileImage() != null && !updateDTO.getProfileImage().isEmpty()) {
            try {
                // 4-1. 이미지 파일인지 검증
                if (!FileUtil.isImageFile(updateDTO.getProfileImage())) {
                    throw new Exception400("이미지 파일만 업로드 가능합니다");
                }
                
                // 4-2. 새 이미지를 서버 디스크에 저장
                // UUID를 사용하여 고유한 파일명으로 저장
                String newProfileImageFilename = FileUtil.saveFile(updateDTO.getProfileImage(), FileUtil.IMAGES_DIR);
                
                // 4-3. DTO에 새 파일명 설정 (나중에 엔티티 업데이트 시 사용)
                updateDTO.setProfileImageFilename(newProfileImageFilename);
                
                // 4-4. 기존 이미지 파일 삭제 (디스크 공간 절약)
                // 주의: DB의 파일명만 삭제하는 것이 아니라 실제 파일도 삭제해야 함!
                // 그렇지 않으면 디스크에 사용하지 않는 파일이 계속 쌓임
                if (oldProfileImage != null && !oldProfileImage.isEmpty()) {
                    FileUtil.deleteFile(oldProfileImage, FileUtil.IMAGES_DIR);
                }
            } catch (IOException e) {
                throw new Exception400("파일 저장에 실패했습니다: " + e.getMessage());
            }
        } else {
            // 새 이미지가 업로드되지 않았으면 기존 이미지 파일명 유지
            // → DB의 profile_image 컬럼 값이 변경되지 않음
            updateDTO.setProfileImageFilename(oldProfileImage);
        }

        // 5. 더티 체킹을 활용한 수정 처리
        // 엔티티의 상태 값 변경
        user.update(updateDTO);

        // 6. 변경된 엔티티 저장 (더티 체킹)
        // 참고: @Transactional이 있으면 save() 없이도 자동으로 UPDATE 됨
        // 하지만 명시적으로 save()를 호출하는 것이 더 명확함
        User updateUser = userRepository.save(user);

        // 7. 수정된 사용자 정보 반환 (Controller에서 세션 갱신용)
        return updateUser;
    }

    /**
     * 프로필 이미지 삭제 처리
     * 
     * 비즈니스 로직:
     * 1. 회원정보 조회
     * 2. 인가 검사 (소유자 확인)
     * 3. 프로필 이미지 파일 삭제
     * 4. DB에서 프로필 이미지 필드 null로 업데이트
     * 
     * @param userId 현재 로그인한 사용자 ID
     * @return 프로필 이미지가 삭제된 사용자 엔티티
     * @throws Exception404 사용자가 없을 경우
     * @throws Exception403 삭제 권한이 없을 경우
     */
    @Transactional
    public User 프로필이미지삭제(Long userId) {
        // 1. 회원정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));

        // 2. 인가 검사: 자기 자신의 정보만 삭제 가능한지 확인
        if (!user.isOwner(userId)) {
            throw new Exception403("프로필 이미지 삭제 권한이 없습니다");
        }

        // 3. 프로필 이미지 파일 삭제 (있으면)
        String profileImage = user.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                FileUtil.deleteFile(profileImage, FileUtil.IMAGES_DIR);
            } catch (IOException e) {
                // 파일 삭제 실패해도 DB는 업데이트 (파일이 이미 없을 수도 있음)
                // 로그만 남기고 계속 진행
                System.err.println("프로필 이미지 파일 삭제 실패: " + e.getMessage());
            }
        }

        // 4. DB에서 프로필 이미지 필드 null로 업데이트
        user.setProfileImage(null);
        
        // 5. 변경된 엔티티 저장 (더티 체킹)
        return userRepository.save(user);
    }
}

