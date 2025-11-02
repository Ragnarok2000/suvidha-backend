package com.suvidha.Modal;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id 
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String url;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne
    private User user;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    private String summaryLength; // "short", "medium", "long"
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        // Get current time in IST (India Standard Time)
        ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        createdAt = istTime.toLocalDateTime();
    }
}
