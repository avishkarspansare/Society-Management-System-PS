package com.societyledger.society.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flats",
    indexes = {
        @Index(name = "idx_flats_society", columnList = "society_id"),
        @Index(name = "idx_flats_wing", columnList = "wing_id"),
        @Index(name = "idx_flats_ref_code", columnList = "payment_reference_code", unique = true)
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uq_flat_number",
        columnNames = {"society_id", "wing_id", "flat_number"}
    )
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Flat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wing_id", nullable = false)
    private Wing wing;

    @Column(name = "flat_number", nullable = false, length = 20)
    private String flatNumber;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "area_sqft", precision = 8, scale = 2)
    private BigDecimal areaSqft;

    @Column(name = "payment_reference_code", nullable = false, unique = true, length = 50)
    private String paymentReferenceCode;

    @Column(name = "is_occupied")
    @Builder.Default
    private Boolean isOccupied = true;

    @OneToMany(mappedBy = "flat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FamilyMember> familyMembers = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
