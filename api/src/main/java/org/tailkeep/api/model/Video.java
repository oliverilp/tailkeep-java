package org.tailkeep.api.model;

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
    
    private Integer duration;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    private String description;
    
    @Column(name = "view_count")
    private Integer viewCount;
    
    @Column(name = "comment_count")
    private Integer commentCount;
    
    private String filename;
    
    @OneToMany(mappedBy = "video")
    private List<DownloadProgress> progressList;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
