package org.example.demo_ssr_v1_1.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자와 역할(ROLE)을 연결하는 엔티티.
 *
 * - user_role_tb 테이블에 user_id + role 을 유니크하게 저장합니다.
 * - User 엔티티에서 단방향 @OneToMany 로 관리하며,
 *   FK 컬럼(user_id)은 user_role_tb 테이블에 생성됩니다.
 */
@Table(
        name = "user_role_tb",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role"})
        }
)
@Entity
@NoArgsConstructor
@Getter
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Builder
    public UserRole(Long id, Role role) {
        this.id = id;
        this.role = role;
    }
}


