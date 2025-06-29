package com.memvid.graphql

import com.memvid.domain.VideoMemory
import com.memvid.domain.VideoMemoryStatus
import com.memvid.service.MemvidService
import org.springframework.data.domain.PageRequest
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class MemvidGraphQLResolver(
    private val memvidService: MemvidService
) {

    @QueryMapping
    fun videoMemories(
        @Argument page: Int = 0,
        @Argument size: Int = 20
    ): VideoMemoryPage {
        val pageable = PageRequest.of(page, size)
        val pagedResult = memvidService.getAllVideoMemories(pageable)
        return VideoMemoryPage(
            content = pagedResult.content.map { VideoMemoryDTO.from(it) },
            totalElements = pagedResult.totalElements,
            totalPages = pagedResult.totalPages,
            currentPage = pagedResult.number,
            size = pagedResult.size
        )
    }

    @QueryMapping
    fun videoMemory(@Argument id: Long): VideoMemoryDetailDTO {
        val videoMemory = memvidService.getVideoMemoryWithDetails(id)
        return VideoMemoryDetailDTO.from(videoMemory)
    }

    @QueryMapping
    fun searchVideoMemories(@Argument query: String): List<VideoMemoryDTO> {
        return memvidService.searchVideoMemories(query).map { VideoMemoryDTO.from(it) }
    }

    @QueryMapping
    fun videoMemoriesByStatus(@Argument status: VideoMemoryStatus): List<VideoMemoryDTO> {
        return memvidService.getVideoMemoriesByStatus(status).map { VideoMemoryDTO.from(it) }
    }

    @QueryMapping
    fun systemStatus(): SystemStatusDTO {
        val statusCounts = VideoMemoryStatus.values().associateWith { status ->
            memvidService.getVideoMemoriesByStatus(status).size
        }
        
        val statusCountsDTO = StatusCountsDTO(
            CREATED = statusCounts[VideoMemoryStatus.CREATED] ?: 0,
            PROCESSING = statusCounts[VideoMemoryStatus.PROCESSING] ?: 0,
            BUILT = statusCounts[VideoMemoryStatus.BUILT] ?: 0,
            ERROR = statusCounts[VideoMemoryStatus.ERROR] ?: 0
        )
        
        return SystemStatusDTO(
            statusCounts = statusCountsDTO,
            totalMemories = statusCounts.values.sum(),
            readyForChat = statusCounts[VideoMemoryStatus.BUILT] ?: 0,
            processing = statusCounts[VideoMemoryStatus.PROCESSING] ?: 0,
            created = statusCounts[VideoMemoryStatus.CREATED] ?: 0,
            error = statusCounts[VideoMemoryStatus.ERROR] ?: 0
        )
    }

    @MutationMapping
    fun createVideoMemory(
        @Argument title: String,
        @Argument description: String?
    ): VideoMemoryDTO {
        val videoMemory = memvidService.createVideoMemory(title, description)
        return VideoMemoryDTO.from(videoMemory)
    }

    @MutationMapping
    fun addTextChunks(
        @Argument videoMemoryId: Long,
        @Argument chunks: List<String>
    ): VideoMemoryDTO {
        val videoMemory = memvidService.addTextChunks(videoMemoryId, chunks)
        return VideoMemoryDTO.from(videoMemory)
    }

    @MutationMapping
    fun buildVideoIndex(@Argument videoMemoryId: Long): VideoMemoryDTO {
        val videoMemory = memvidService.buildVideoIndex(videoMemoryId)
        return VideoMemoryDTO.from(videoMemory)
    }

    @MutationMapping
    fun chatWithMemory(
        @Argument videoMemoryId: Long,
        @Argument question: String
    ): ChatResponseDTO {
        val response = memvidService.chatWithMemory(videoMemoryId, question)
        return ChatResponseDTO(response)
    }

    @MutationMapping
    fun deleteVideoMemory(@Argument id: Long): Boolean {
        memvidService.deleteVideoMemory(id)
        return true
    }

    @SchemaMapping(typeName = "VideoMemory")
    fun textChunks(videoMemory: VideoMemoryDTO): List<TextChunkDTO> {
        val fullMemory = memvidService.getVideoMemoryWithDetails(videoMemory.id)
        return fullMemory.textChunks.map { TextChunkDTO.from(it) }
    }

    @SchemaMapping(typeName = "VideoMemory")
    fun memoryIndex(videoMemory: VideoMemoryDTO): MemoryIndexDTO? {
        val fullMemory = memvidService.getVideoMemoryWithDetails(videoMemory.id)
        return fullMemory.memoryIndex?.let { MemoryIndexDTO.from(it) }
    }
}

// GraphQL DTOs
data class VideoMemoryDTO(
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
        fun from(videoMemory: VideoMemory): VideoMemoryDTO {
            return VideoMemoryDTO(
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

data class VideoMemoryDetailDTO(
    val id: Long,
    val title: String,
    val description: String?,
    val videoFilename: String,
    val indexFilename: String,
    val status: VideoMemoryStatus,
    val createdAt: String,
    val updatedAt: String,
    val textChunks: List<TextChunkDTO>,
    val memoryIndex: MemoryIndexDTO?
) {
    companion object {
        fun from(videoMemory: VideoMemory): VideoMemoryDetailDTO {
            return VideoMemoryDetailDTO(
                id = videoMemory.id!!,
                title = videoMemory.title,
                description = videoMemory.description,
                videoFilename = videoMemory.videoFilename,
                indexFilename = videoMemory.indexFilename,
                status = videoMemory.status,
                createdAt = videoMemory.createdAt.toString(),
                updatedAt = videoMemory.updatedAt.toString(),
                textChunks = videoMemory.textChunks.map { TextChunkDTO.from(it) },
                memoryIndex = videoMemory.memoryIndex?.let { MemoryIndexDTO.from(it) }
            )
        }
    }
}

data class TextChunkDTO(
    val id: Long,
    val content: String,
    val chunkIndex: Int,
    val summary: String?,
    val wordCount: Int,
    val chunkType: String,
    val createdAt: String
) {
    companion object {
        fun from(textChunk: com.memvid.domain.TextChunk): TextChunkDTO {
            return TextChunkDTO(
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

data class MemoryIndexDTO(
    val id: Long,
    val totalChunks: Int,
    val totalWords: Int,
    val summary: String?,
    val keywords: List<String>,
    val topics: List<String>,
    val averageWordsPerChunk: Double,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(memoryIndex: com.memvid.domain.MemoryIndex): MemoryIndexDTO {
            val keywords = try {
                com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                    .readValue(memoryIndex.keywords ?: "[]", List::class.java)
                    .map { it.toString() }
            } catch (e: Exception) {
                emptyList<String>()
            }

            val topics = try {
                com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                    .readValue(memoryIndex.topics ?: "[]", List::class.java)
                    .map { it.toString() }
            } catch (e: Exception) {
                emptyList<String>()
            }

            return MemoryIndexDTO(
                id = memoryIndex.id!!,
                totalChunks = memoryIndex.totalChunks,
                totalWords = memoryIndex.totalWords,
                summary = memoryIndex.summary,
                keywords = keywords,
                topics = topics,
                averageWordsPerChunk = memoryIndex.getAverageWordsPerChunk(),
                createdAt = memoryIndex.createdAt.toString(),
                updatedAt = memoryIndex.updatedAt.toString()
            )
        }
    }
}

data class VideoMemoryPage(
    val content: List<VideoMemoryDTO>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val size: Int
)

data class SystemStatusDTO(
    val statusCounts: StatusCountsDTO,
    val totalMemories: Int,
    val readyForChat: Int,
    val processing: Int,
    val created: Int,
    val error: Int
)

data class StatusCountsDTO(
    val CREATED: Int,
    val PROCESSING: Int,
    val BUILT: Int,
    val ERROR: Int
)

data class ChatResponseDTO(
    val response: String
) 