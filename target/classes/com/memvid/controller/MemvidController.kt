package com.memvid.controller

import com.memvid.domain.VideoMemory
import com.memvid.domain.VideoMemoryStatus
import com.memvid.service.MemvidService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/memvid")
@CrossOrigin(origins = ["*"])
class MemvidController(
    private val memvidService: MemvidService
) {

    @PostMapping("/memories")
    fun createVideoMemory(@Valid @RequestBody request: CreateVideoMemoryRequest): ResponseEntity<VideoMemoryResponse> {
        val videoMemory = memvidService.createVideoMemory(request.title, request.description)
        return ResponseEntity.status(HttpStatus.CREATED).body(VideoMemoryResponse.from(videoMemory))
    }

    @PostMapping("/memories/{id}/chunks")
    fun addTextChunks(
        @PathVariable id: Long,
        @Valid @RequestBody request: AddChunksRequest
    ): ResponseEntity<VideoMemoryResponse> {
        val videoMemory = memvidService.addTextChunks(id, request.chunks)
        return ResponseEntity.ok(VideoMemoryResponse.from(videoMemory))
    }

    @PostMapping("/memories/{id}/build")
    fun buildVideoIndex(@PathVariable id: Long): ResponseEntity<VideoMemoryResponse> {
        val videoMemory = memvidService.buildVideoIndex(id)
        return ResponseEntity.ok(VideoMemoryResponse.from(videoMemory))
    }

    @PostMapping("/memories/{id}/chat")
    fun chatWithMemory(
        @PathVariable id: Long,
        @Valid @RequestBody request: ChatRequest
    ): ResponseEntity<ChatResponse> {
        val response = memvidService.chatWithMemory(id, request.question)
        return ResponseEntity.ok(ChatResponse(response))
    }

    @GetMapping("/memories")
    fun getAllVideoMemories(
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<VideoMemoryResponse>> {
        val memories = memvidService.getAllVideoMemories(pageable)
        val response = memories.map { VideoMemoryResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/memories/{id}")
    fun getVideoMemory(@PathVariable id: Long): ResponseEntity<VideoMemoryDetailResponse> {
        val videoMemory = memvidService.getVideoMemoryWithDetails(id)
        return ResponseEntity.ok(VideoMemoryDetailResponse.from(videoMemory))
    }

    @GetMapping("/memories/search")
    fun searchVideoMemories(
        @RequestParam query: String
    ): ResponseEntity<List<VideoMemoryResponse>> {
        val results = memvidService.searchVideoMemories(query)
        val response = results.map { VideoMemoryResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/memories/status/{status}")
    fun getVideoMemoriesByStatus(
        @PathVariable status: VideoMemoryStatus
    ): ResponseEntity<List<VideoMemoryResponse>> {
        val memories = memvidService.getVideoMemoriesByStatus(status)
        val response = memories.map { VideoMemoryResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/memories/{id}")
    fun deleteVideoMemory(@PathVariable id: Long): ResponseEntity<Void> {
        memvidService.deleteVideoMemory(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/status")
    fun getSystemStatus(): ResponseEntity<SystemStatusResponse> {
        val statusCounts = VideoMemoryStatus.values().associateWith { status ->
            memvidService.getVideoMemoriesByStatus(status).size
        }
        return ResponseEntity.ok(SystemStatusResponse(statusCounts))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(ErrorResponse(e.message ?: "Invalid argument"))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(ErrorResponse(e.message ?: "Invalid state"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("An unexpected error occurred: ${e.message}"))
    }
}

// Request DTOs
data class CreateVideoMemoryRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title cannot exceed 255 characters")
    val title: String,
    
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null
)

data class AddChunksRequest(
    @field:NotEmpty(message = "Chunks cannot be empty")
    val chunks: List<@NotBlank(message = "Chunk content cannot be blank") String>
)

data class ChatRequest(
    @field:NotBlank(message = "Question is required")
    val question: String
)

// Response DTOs
data class VideoMemoryResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val videoFilename: String,
    val indexFilename: String,
    val status: VideoMemoryStatus,
    val totalChunks: Int,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(videoMemory: VideoMemory): VideoMemoryResponse {
            return VideoMemoryResponse(
                id = videoMemory.id!!,
                title = videoMemory.title,
                description = videoMemory.description,
                videoFilename = videoMemory.videoFilename,
                indexFilename = videoMemory.indexFilename,
                status = videoMemory.status,
                totalChunks = videoMemory.getTotalChunks(),
                createdAt = videoMemory.createdAt.toString(),
                updatedAt = videoMemory.updatedAt.toString()
            )
        }
    }
}

data class VideoMemoryDetailResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val videoFilename: String,
    val indexFilename: String,
    val status: VideoMemoryStatus,
    val createdAt: String,
    val updatedAt: String,
    val textChunks: List<TextChunkResponse>,
    val memoryIndex: MemoryIndexResponse?
) {
    companion object {
        fun from(videoMemory: VideoMemory): VideoMemoryDetailResponse {
            return VideoMemoryDetailResponse(
                id = videoMemory.id!!,
                title = videoMemory.title,
                description = videoMemory.description,
                videoFilename = videoMemory.videoFilename,
                indexFilename = videoMemory.indexFilename,
                status = videoMemory.status,
                createdAt = videoMemory.createdAt.toString(),
                updatedAt = videoMemory.updatedAt.toString(),
                textChunks = videoMemory.textChunks.map { TextChunkResponse.from(it) },
                memoryIndex = videoMemory.memoryIndex?.let { MemoryIndexResponse.from(it) }
            )
        }
    }
}

data class TextChunkResponse(
    val id: Long,
    val content: String,
    val chunkIndex: Int,
    val summary: String?,
    val wordCount: Int,
    val chunkType: String,
    val createdAt: String
) {
    companion object {
        fun from(textChunk: com.memvid.domain.TextChunk): TextChunkResponse {
            return TextChunkResponse(
                id = textChunk.id!!,
                content = textChunk.content,
                chunkIndex = textChunk.chunkIndex,
                summary = textChunk.summary,
                wordCount = textChunk.wordCount,
                chunkType = textChunk.chunkType.name,
                createdAt = textChunk.createdAt.toString()
            )
        }
    }
}

data class MemoryIndexResponse(
    val id: Long,
    val totalChunks: Int,
    val totalWords: Int,
    val summary: String?,
    val keywords: String?,
    val topics: String?,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(memoryIndex: com.memvid.domain.MemoryIndex): MemoryIndexResponse {
            return MemoryIndexResponse(
                id = memoryIndex.id!!,
                totalChunks = memoryIndex.totalChunks,
                totalWords = memoryIndex.totalWords,
                summary = memoryIndex.summary,
                keywords = memoryIndex.keywords,
                topics = memoryIndex.topics,
                createdAt = memoryIndex.createdAt.toString(),
                updatedAt = memoryIndex.updatedAt.toString()
            )
        }
    }
}

data class ChatResponse(
    val response: String
)

data class SystemStatusResponse(
    val statusCounts: Map<VideoMemoryStatus, Int>
) {
    val totalMemories: Int = statusCounts.values.sum()
    val readyForChat: Int = statusCounts[VideoMemoryStatus.BUILT] ?: 0
    val processing: Int = statusCounts[VideoMemoryStatus.PROCESSING] ?: 0
}

data class ErrorResponse(
    val message: String
) 