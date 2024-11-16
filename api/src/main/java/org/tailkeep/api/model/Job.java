package org.tailkeep.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    @NotNull
    private String inputUrl;
    
    @ManyToOne
    @JoinColumn(name = "video_id")
    private Video video;

    @JsonIgnore
    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL)
    private DownloadProgress downloadProgress;
}
