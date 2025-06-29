package com.memvid.domain

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

@Entity
@Table(name = "memory_indexes")
data class MemoryIndex(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @field:NotBlank(message = "Index content cannot be blank")
    val indexContent: String, // JSON representation of the index
    
    @Column(nullable = false)
    val totalChunks: Int,
    
    @Column(nullable = false)
    val totalWords: Int,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(length = 1000)
    val summary: String? = null,
    
    @Column(columnDefinition = "TEXT")
    val keywords: String? = null, // JSON array of keywords
    
    @Column(columnDefinition = "TEXT")
    val topics: String? = null, // JSON array of topics
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_memory_id", nullable = false)
    @JsonBackReference
    var videoMemory: VideoMemory? = null
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
    
    fun getAverageWordsPerChunk(): Double = 
        if (totalChunks > 0) totalWords.toDouble() / totalChunks else 0.0
    
    fun isLargeIndex(): Boolean = totalChunks > 50
} 