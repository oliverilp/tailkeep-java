package org.tailkeep.adapter.database.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jwt_secret")
@Getter
@NoArgsConstructor
public class JwtSecretEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "secret_key", nullable = false)
    private String secretKey;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public JwtSecretEntity(String secretKey) {
        this.secretKey = secretKey;
    }
}
