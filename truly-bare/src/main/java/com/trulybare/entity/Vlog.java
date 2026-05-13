package com.trulybare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "vlogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vlog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vlogId;

    private String title;
    private String tag;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String videoUrl;
    private String posterUrl;
    private String location;
    private String vlogDate; // e.g., "April 2024"
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;
    
    private LocalDateTime createdAt = LocalDateTime.now();
}
