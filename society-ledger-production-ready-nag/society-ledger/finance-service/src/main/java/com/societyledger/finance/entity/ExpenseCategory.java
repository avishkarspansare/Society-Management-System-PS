package com.societyledger.finance.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "expense_categories",
    uniqueConstraints = @UniqueConstraint(name = "uq_category_name", columnNames = {"society_id", "name"})
)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExpenseCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Column(name = "society_id", nullable = false)
    private Long societyId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "created_at") @Builder.Default
    private Instant createdAt = Instant.now();
}
