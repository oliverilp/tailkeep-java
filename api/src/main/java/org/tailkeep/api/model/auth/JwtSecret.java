package org.tailkeep.api.model.auth;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_secret")
@Getter
@NoArgsConstructor
public class JwtSecret {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String secretKey;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public JwtSecret(String secretKey) {
        this.secretKey = secretKey;
    }
}
