package com.memvid.domain

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
@Table(name = "video_memories")
data class VideoMemory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    @field:NotBlank(message = "Video filename cannot be blank")
    val videoFilename: String,
    
    @Column(nullable = false, unique = true)
    @field:NotBlank(message = "Index filename cannot be blank")
    val indexFilename: String,
    
    @Column(nullable = false)
    @field:NotBlank(message = "Title cannot be blank")
    @field:Size(max = 255, message = "Title cannot exceed 255 characters")
    val title: String,
    
    @Column(length = 1000)
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: VideoMemoryStatus = VideoMemoryStatus.CREATED,
    
    @OneToMany(mappedBy = "videoMemory", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonManagedReference
    val textChunks: MutableList<TextChunk> = mutableListOf(),
    
    @OneToOne(mappedBy = "videoMemory", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonManagedReference
    var memoryIndex: MemoryIndex? = null
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = LocalDateTime.now()
    }
    
    fun addTextChunk(chunk: TextChunk) {
        textChunks.add(chunk)
        chunk.videoMemory = this
    }
    
    fun getTotalChunks(): Int = textChunks.size
    
    fun isBuilt(): Boolean = status == VideoMemoryStatus.BUILT
}

enum class VideoMemoryStatus {
    CREATED,
    PROCESSING,
    BUILT,
    ERROR
} 