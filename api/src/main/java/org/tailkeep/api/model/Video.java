package org.tailkeep.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "video")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "youtube_id", unique = true)
    private String youtubeId;
    
    @ManyToOne
    @JoinColumn(name = "channel_id")
    private Channel channel;
    
    private String url;
    private String title;
    
    @Column(name = "duration_string")
    private String durationString;
    
    private Double duration;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "view_count")
    private Long viewCount;
    
    @Column(name = "comment_count")
    private Long commentCount;
    
    private String filename;
    
    @JsonIgnore
    @OneToMany(mappedBy = "video")
    private List<DownloadProgress> progressList;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
