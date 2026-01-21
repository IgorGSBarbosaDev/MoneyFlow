package br.com.moneyflow.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity(name = "Category")
@Table(name="categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @NotBlank
    @Column(nullable = false, length = 80)
    private String name;
    @Column(length = 200)
    private String description;
    @Enumerated(EnumType.STRING)
    private CategoryType type;
    @NotBlank
    @Column(nullable = false, length = 20)
    private String color;
    @NotBlank
    @Column(nullable = false, length = 200)
    private String icon;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_category_user"))
    private User userId;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
