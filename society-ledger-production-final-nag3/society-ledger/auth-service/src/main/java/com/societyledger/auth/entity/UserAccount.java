package com.societyledger.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "user_accounts",
    indexes = {
        @Index(name = "idx_user_society", columnList = "society_id"),
        @Index(name = "idx_user_flat", columnList = "flat_id"),
        @Index(name = "idx_user_email", columnList = "email")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "flat_id", nullable = false, unique = true)
    private Long flatId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum UserRole {
        ADMIN, RESIDENT
    }
}
