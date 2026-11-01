package org.tailkeep.adapter.database.download;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.tailkeep.adapter.database.video.VideoEntity;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "download_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadProgressEntity {
    @Id
    private UUID id;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private VideoEntity video;
    
    private String status;

    @Column(nullable = false)
    private boolean hasEnded;

    private double progress;

    private String size;

    private String speed;

    private String eta;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
