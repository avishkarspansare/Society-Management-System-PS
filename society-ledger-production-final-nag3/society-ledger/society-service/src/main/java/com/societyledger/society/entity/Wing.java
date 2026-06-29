package com.societyledger.society.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wings",
    indexes = @Index(name = "idx_wings_society", columnList = "society_id"),
    uniqueConstraints = @UniqueConstraint(name = "uq_wing_name", columnNames = {"society_id", "wing_name"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Wing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @Column(name = "wing_name", nullable = false, length = 50)
    private String wingName;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "wing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Flat> flats = new ArrayList<>();
}
