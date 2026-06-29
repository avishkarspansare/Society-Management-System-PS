package com.societyledger.society.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wings", uniqueConstraints =
    @UniqueConstraint(name = "uq_wing_society_name", columnNames = {"society_id", "wing_name"}))
@EntityListeners(AuditingEntityListener.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Wing {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "society_id", nullable = false)
    private Long societyId;

    @Column(name = "wing_name", nullable = false, length = 100)
    private String wingName;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @OneToMany(mappedBy = "wing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Flat> flats = new ArrayList<>();

    @CreatedDate @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate @Column(name = "updated_at")
    private Instant updatedAt;
}
