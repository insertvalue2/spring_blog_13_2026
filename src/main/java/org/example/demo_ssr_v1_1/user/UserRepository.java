package org.example.demo_ssr_v1_1.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JpaRepository를 상속받는 Repository 인터페이스
 * 
 * 핵심 개념:
 * 1. JpaRepository<User, Long>: 
 *    - User 엔티티를 관리하는 Repository
 *    - 기본키 타입은 Long
 * 
 * 2. 쿼리 메서드 (Query Methods):
 *    - 메서드 이름만으로 쿼리를 자동 생성
 *    - Spring Data JPA가 메서드 이름을 분석하여 SQL 쿼리 생성
 * 
 * 3. 쿼리 메서드 네이밍 규칙:
 *    - findBy + 필드명: WHERE 조건
 *    - And: AND 조건 연결
 *    - Or: OR 조건 연결
 *    - Optional<T> 반환: 결과가 없을 수 있음을 명시
 * 
 * 예시:
 * - findByUsername(String username)
 *   -> SELECT * FROM user_tb WHERE username = ?
 * 
 * - findByUsernameAndPassword(String username, String password)
 *   -> SELECT * FROM user_tb WHERE username = ? AND password = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 사용자명으로 사용자 조회
     * 
     * 쿼리 메서드 네이밍:
     * - findBy: 조회 시작
     * - Username: 엔티티의 username 필드명과 일치
     * - Optional<User>: 결과가 없을 수 있으므로 Optional로 반환
     * 
     * 자동 생성되는 SQL:
     * SELECT * FROM user_tb WHERE username = ?
     * 
     * 사용 예시:
     * Optional<User> user = userRepository.findByUsername("test");
     * if (user.isPresent()) {
     *     // 사용자 존재
     * } else {
     *     // 사용자 없음
     * }
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 사용자명과 비밀번호로 사용자 조회 (로그인용)
     * 
     * 쿼리 메서드 네이밍:
     * - findBy: 조회 시작
     * - Username: 첫 번째 조건 필드
     * - And: AND 조건
     * - Password: 두 번째 조건 필드
     * 
     * 자동 생성되는 SQL:
     * SELECT * FROM user_tb WHERE username = ? AND password = ?
     * 
     * Optional 반환 이유:
     * - 로그인 실패 시 null 대신 Optional.empty() 반환
     * - null 안전성 보장
     */
    Optional<User> findByUsernameAndPassword(String username, String password);

    /**
     * 로그인 시 역할(ROLE) 정보까지 함께 조회하는 메서드.
     *
     * - LEFT JOIN FETCH 를 사용하여 user_role_tb 를 한 번에 로딩합니다.
     * - 세션에 저장된 User 객체에서 isAdmin(), getRoleDisplay() 등을 사용할 수 있습니다.
     */
    @Query("select distinct u from User u left join fetch u.roles r " +
           "where u.username = :username and u.password = :password")
    Optional<User> findByUsernameAndPasswordWithRoles(@Param("username") String username,
                                                      @Param("password") String password);
    
    /**
     * JpaRepository에서 자동 제공되는 메서드들:
     * 
     * 1. <S extends User> S save(S entity):
     *    - 엔티티 저장 (INSERT 또는 UPDATE)
     *    - ID가 null이면 INSERT, 있으면 UPDATE
     * 
     * 2. Optional<User> findById(Long id):
     *    - ID로 엔티티 조회
     *    - Optional로 반환하여 null 안전성 보장
     * 
     * 3. void deleteById(Long id):
     *    - ID로 엔티티 삭제
     * 
     * 4. List<User> findAll():
     *    - 모든 엔티티 조회
     * 
     * 더티 체킹 활용:
     * - 엔티티를 조회한 후 필드 값을 변경하면
     * - 트랜잭션이 끝날 때 자동으로 UPDATE 쿼리 실행
     * - 별도의 update 메서드가 필요 없음
     * 
     * 예시:
     * User user = userRepository.findById(id).orElseThrow(...);
     * user.update(updateDTO); // 필드 값 변경
     * // 트랜잭션 종료 시 자동으로 UPDATE 쿼리 실행 (더티 체킹)
     */
}
