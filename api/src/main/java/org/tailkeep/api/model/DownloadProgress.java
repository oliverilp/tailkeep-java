package org.tailkeep.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadProgress {
    @Id
    private String id;
    
    @JsonIgnore
    @OneToOne
    @MapsId  // Uses the same ID as the Job
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;
    
    private String status;
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
}
