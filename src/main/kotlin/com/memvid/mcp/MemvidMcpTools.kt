package com.memvid.mcp

import com.fasterxml.jackson.annotation.JsonProperty
import com.memvid.service.MemvidService
import com.memvid.domain.VideoMemoryStatus
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

/**
 * MCP (Model Context Protocol) server implementation for Memvid
 * Provides tools for managing video memories and text chunks
 */
@Component
@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = ["*"])
class MemvidMcpTools(
    private val memvidService: MemvidService
) {

    private val sseEmitters = ConcurrentHashMap<String, SseEmitter>()

    /**
     * MCP Server endpoint - Server-Sent Events for MCP communication
     */
    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun mcpServer(): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)
        val sessionId = java.util.UUID.randomUUID().toString()
        sseEmitters[sessionId] = emitter
        
        emitter.onCompletion { sseEmitters.remove(sessionId) }
        emitter.onTimeout { sseEmitters.remove(sessionId) }
        emitter.onError { sseEmitters.remove(sessionId) }
        
        try {
            // Send initial MCP server info
            val serverInfo = McpServerInfo(
                name = "memvid-mcp-server",
                version = "1.0.0",
                protocolVersion = "2024-11-05",
                capabilities = McpCapabilities(
                    tools = listOf(
                        McpTool("create_video_memory", "Create a new video memory"),
                        McpTool("add_chunks", "Add text chunks to a video memory"),
                        McpTool("build_video", "Build video memory with searchable index"),
                        McpTool("chat_with_memory", "Chat with video memory content"),
                        McpTool("list_video_memories", "List all video memories"),
                        McpTool("get_memory_details", "Get detailed memory information"),
                        McpTool("search_memories", "Search video memories"),
                        McpTool("get_memory_status", "Get memory status overview"),
                        McpTool("delete_memory", "Delete a video memory")
                    )
                )
            )
            emitter.send(SseEmitter.event().name("server_info").data(serverInfo))
        } catch (e: Exception) {
            emitter.completeWithError(e)
        }
        
        return emitter
    }

    /**
     * Handle MCP tool calls
     */
    @PostMapping("/tools/{toolName}")
    fun callTool(
        @PathVariable toolName: String,
        @RequestBody request: Map<String, Any>
    ): ResponseEntity<Any> {
        return try {
            val result = when (toolName) {
                "create_video_memory" -> createVideoMemory(request)
                "add_chunks" -> addChunks(request)
                "build_video" -> buildVideoMemory(request)
                "chat_with_memory" -> chatWithMemory(request)
                "list_video_memories" -> listVideoMemories(request)
                "get_memory_details" -> getMemoryDetails(request)
                "search_memories" -> searchMemories(request)
                "get_memory_status" -> getMemoryStatus(request)
                "delete_memory" -> deleteMemory(request)
                else -> mapOf("error" to "Unknown tool: $toolName")
            }
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    /**
     * Create a new video memory
     */
    private fun createVideoMemory(request: Map<String, Any>): Map<String, Any> {
        val title = request["title"] as? String ?: throw IllegalArgumentException("Title is required")
        val description = request["description"] as? String

        val videoMemory = memvidService.createVideoMemory(title, description)
        
        return mapOf(
            "success" to true,
            "videoMemoryId" to videoMemory.id!!,
            "title" to videoMemory.title,
            "description" to (videoMemory.description ?: ""),
            "status" to videoMemory.status.name,
            "message" to "Video memory '${videoMemory.title}' created successfully"
        )
    }

    /**
     * Add text chunks to a video memory
     */
    private fun addChunks(request: Map<String, Any>): Map<String, Any> {
        val videoMemoryId = (request["videoMemoryId"] as? Number)?.toLong() 
            ?: throw IllegalArgumentException("Video memory ID is required")
        
        @Suppress("UNCHECKED_CAST")
        val chunks = request["chunks"] as? List<String> 
            ?: throw IllegalArgumentException("Chunks list is required")

        val updatedVideoMemory = memvidService.addTextChunks(videoMemoryId, chunks)
        
        return mapOf(
            "success" to true,
            "videoMemoryId" to videoMemoryId,
            "chunksAdded" to chunks.size,
            "chunks" to updatedVideoMemory.textChunks.map { 
                mapOf(
                    "id" to it.id!!,
                    "content" to it.content,
                    "chunkIndex" to it.chunkIndex,
                    "wordCount" to it.wordCount
                )
            },
            "message" to "${chunks.size} chunks added successfully"
        )
    }

    /**
     * Build video memory with searchable index
     */
    private fun buildVideoMemory(request: Map<String, Any>): Map<String, Any> {
        val videoMemoryId = (request["videoMemoryId"] as? Number)?.toLong() 
            ?: throw IllegalArgumentException("Video memory ID is required")

        val updatedVideoMemory = memvidService.buildVideoIndex(videoMemoryId)
        val memoryIndex = updatedVideoMemory.memoryIndex
            ?: throw IllegalStateException("Memory index was not created")
        
        return mapOf(
            "success" to true,
            "videoMemoryId" to videoMemoryId,
            "indexId" to memoryIndex.id!!,
            "totalChunks" to memoryIndex.totalChunks,
            "totalWords" to memoryIndex.totalWords,
            "summary" to (memoryIndex.summary ?: ""),
            "message" to "Video memory built successfully with ${memoryIndex.totalChunks} chunks"
        )
    }

    /**
     * Chat with video memory content
     */
    private fun chatWithMemory(request: Map<String, Any>): Map<String, Any> {
        val videoMemoryId = (request["videoMemoryId"] as? Number)?.toLong() 
            ?: throw IllegalArgumentException("Video memory ID is required")
        val question = request["question"] as? String 
            ?: throw IllegalArgumentException("Question is required")

        val response = memvidService.chatWithMemory(videoMemoryId, question)
        
        return mapOf(
            "success" to true,
            "videoMemoryId" to videoMemoryId,
            "question" to question,
            "response" to response,
            "message" to "Chat response generated successfully"
        )
    }

    /**
     * List all video memories
     */
    private fun listVideoMemories(request: Map<String, Any>): Map<String, Any> {
        val limit = (request["limit"] as? Number)?.toInt() ?: 10
        val offset = (request["offset"] as? Number)?.toInt() ?: 0

        val pageable = PageRequest.of(offset / limit, limit)
        val memoriesPage = memvidService.getAllVideoMemories(pageable)
        
        return mapOf(
            "success" to true,
            "memories" to memoriesPage.content.map { 
                mapOf(
                    "id" to it.id!!,
                    "title" to it.title,
                    "description" to (it.description ?: ""),
                    "status" to it.status.name,
                    "chunkCount" to it.textChunks.size,
                    "createdAt" to it.createdAt.toString(),
                    "updatedAt" to it.updatedAt.toString()
                )
            },
            "total" to memoriesPage.totalElements,
            "limit" to limit,
            "offset" to offset,
            "message" to "${memoriesPage.content.size} video memories found"
        )
    }

    /**
     * Get detailed memory information
     */
    private fun getMemoryDetails(request: Map<String, Any>): Map<String, Any> {
        val videoMemoryId = (request["videoMemoryId"] as? Number)?.toLong() 
            ?: throw IllegalArgumentException("Video memory ID is required")

        val videoMemory = memvidService.getVideoMemoryWithDetails(videoMemoryId)
        
        return mapOf(
            "success" to true,
            "memory" to mapOf(
                "id" to videoMemory.id!!,
                "title" to videoMemory.title,
                "description" to (videoMemory.description ?: ""),
                "status" to videoMemory.status.name,
                "videoFilename" to (videoMemory.videoFilename ?: ""),
                "indexFilename" to (videoMemory.indexFilename ?: ""),
                "createdAt" to videoMemory.createdAt.toString(),
                "updatedAt" to videoMemory.updatedAt.toString()
            ),
            "chunks" to videoMemory.textChunks.map { 
                mapOf(
                    "id" to it.id!!,
                    "content" to it.content,
                    "chunkIndex" to it.chunkIndex,
                    "summary" to (it.summary ?: ""),
                    "wordCount" to it.wordCount,
                    "chunkType" to (it.chunkType?.name ?: "")
                )
            },
            "index" to (videoMemory.memoryIndex?.let { 
                mapOf(
                    "id" to it.id!!,
                    "totalChunks" to it.totalChunks,
                    "totalWords" to it.totalWords,
                    "summary" to (it.summary ?: ""),
                    "createdAt" to it.createdAt.toString()
                )
            } ?: emptyMap<String, Any>()),
            "message" to "Memory details retrieved successfully"
        )
    }

    /**
     * Search video memories
     */
    private fun searchMemories(request: Map<String, Any>): Map<String, Any> {
        val query = request["query"] as? String 
            ?: throw IllegalArgumentException("Search query is required")

        val results = memvidService.searchVideoMemories(query)
        
        return mapOf(
            "success" to true,
            "query" to query,
            "results" to results.map { 
                mapOf(
                    "id" to it.id!!,
                    "title" to it.title,
                    "description" to (it.description ?: ""),
                    "status" to it.status.name,
                    "chunkCount" to it.textChunks.size,
                    "createdAt" to it.createdAt.toString()
                )
            },
            "total" to results.size,
            "message" to "${results.size} memories found matching '$query'"
        )
    }

    /**
     * Get memory status overview
     */
    private fun getMemoryStatus(request: Map<String, Any>): Map<String, Any> {
        val pageable = PageRequest.of(0, 100) // Get up to 100 memories for status
        val allMemories = memvidService.getAllVideoMemories(pageable)
        
        val statusCounts = VideoMemoryStatus.values().associateWith { status ->
            allMemories.content.count { it.status == status }
        }
        
        val totalChunks = allMemories.content.sumOf { it.textChunks.size }
        
        return mapOf(
            "success" to true,
            "status" to mapOf(
                "totalMemories" to allMemories.totalElements,
                "totalChunks" to totalChunks,
                "statusBreakdown" to statusCounts.mapKeys { it.key.name },
                "memoryStats" to mapOf(
                    "avgChunksPerMemory" to if (allMemories.totalElements > 0) totalChunks.toDouble() / allMemories.totalElements else 0.0,
                    "builtMemories" to statusCounts[VideoMemoryStatus.BUILT]!!,
                    "processingMemories" to statusCounts[VideoMemoryStatus.PROCESSING]!!,
                    "errorMemories" to statusCounts[VideoMemoryStatus.ERROR]!!
                )
            ),
            "message" to "System status retrieved successfully"
        )
    }

    /**
     * Delete a video memory
     */
    private fun deleteMemory(request: Map<String, Any>): Map<String, Any> {
        val videoMemoryId = (request["videoMemoryId"] as? Number)?.toLong() 
            ?: throw IllegalArgumentException("Video memory ID is required")

        memvidService.deleteVideoMemory(videoMemoryId)
        
        return mapOf(
            "success" to true,
            "videoMemoryId" to videoMemoryId,
            "message" to "Video memory deleted successfully"
        )
    }
}

// Data classes for MCP protocol
data class McpServerInfo(
    val name: String,
    val version: String,
    @JsonProperty("protocol_version") val protocolVersion: String,
    val capabilities: McpCapabilities
)

data class McpCapabilities(
    val tools: List<McpTool>
)

data class McpTool(
    val name: String,
    val description: String
) 