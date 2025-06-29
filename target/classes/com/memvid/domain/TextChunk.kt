package com.memvid.domain

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
@Table(name = "text_chunks")
data class TextChunk(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    @field:NotBlank(message = "Content cannot be blank")
    val content: String,
    
    @Column(nullable = false)
    val chunkIndex: Int,
    
    @Column(length = 500)
    @field:Size(max = 500, message = "Summary cannot exceed 500 characters")
    val summary: String? = null,
    
    @Column(nullable = false)
    val wordCount: Int,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val chunkType: ChunkType = ChunkType.TEXT,
    
    @Column
    val metadata: String? = null, // JSON string for additional metadata
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_memory_id", nullable = false)
    @JsonBackReference
    var videoMemory: VideoMemory? = null
) {
    fun getPreview(length: Int = 100): String = 
        if (content.length <= length) content 
        else "${content.substring(0, length)}..."
    
    fun isLongChunk(): Boolean = wordCount > 100
}

enum class ChunkType {
    TEXT,
    SUMMARY,
    METADATA,
    INSTRUCTION
} 