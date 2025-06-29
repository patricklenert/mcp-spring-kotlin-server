package com.memvid.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.memvid.domain.*
import com.memvid.repository.MemoryIndexRepository
import com.memvid.repository.TextChunkRepository
import com.memvid.repository.VideoMemoryRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class MemvidService(
    private val videoMemoryRepository: VideoMemoryRepository,
    private val textChunkRepository: TextChunkRepository,
    private val memoryIndexRepository: MemoryIndexRepository,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(MemvidService::class.java)
    
    @Transactional
    fun createVideoMemory(title: String, description: String? = null): VideoMemory {
        val videoFilename = generateVideoFilename(title)
        val indexFilename = generateIndexFilename(title)
        
        val videoMemory = VideoMemory(
            videoFilename = videoFilename,
            indexFilename = indexFilename,
            title = title,
            description = description,
            status = VideoMemoryStatus.CREATED
        )
        
        logger.info("Creating video memory: $title")
        return videoMemoryRepository.save(videoMemory)
    }
    
    @Transactional
    fun addTextChunks(videoMemoryId: Long, chunks: List<String>): VideoMemory {
        val videoMemory = videoMemoryRepository.findById(videoMemoryId)
            .orElseThrow { IllegalArgumentException("Video memory not found with id: $videoMemoryId") }
        
        val existingChunkCount = textChunkRepository.countByVideoMemoryId(videoMemoryId).toInt()
        
        chunks.forEachIndexed { index, content ->
            val textChunk = TextChunk(
                content = content,
                chunkIndex = existingChunkCount + index,
                wordCount = countWords(content),
                summary = generateSummary(content),
                chunkType = ChunkType.TEXT,
                videoMemory = videoMemory
            )
            videoMemory.addTextChunk(textChunk)
        }
        
        logger.info("Added ${chunks.size} text chunks to video memory: ${videoMemory.title}")
        return videoMemoryRepository.save(videoMemory)
    }
    
    @Transactional
    fun buildVideoIndex(videoMemoryId: Long): VideoMemory {
        val videoMemory = videoMemoryRepository.findByIdWithChunks(videoMemoryId)
            .orElseThrow { IllegalArgumentException("Video memory not found with id: $videoMemoryId") }
        
        if (videoMemory.textChunks.isEmpty()) {
            throw IllegalStateException("Cannot build index: no text chunks found")
        }
        
        // Update status to processing
        val updatedVideoMemory = videoMemory.copy(status = VideoMemoryStatus.PROCESSING)
        videoMemoryRepository.save(updatedVideoMemory)
        
        try {
            val indexData = buildIndexData(updatedVideoMemory.textChunks)
            val totalWords = updatedVideoMemory.textChunks.sumOf { it.wordCount }
            
            val memoryIndex = MemoryIndex(
                indexContent = objectMapper.writeValueAsString(indexData),
                totalChunks = updatedVideoMemory.textChunks.size,
                totalWords = totalWords,
                summary = generateOverallSummary(updatedVideoMemory.textChunks),
                keywords = objectMapper.writeValueAsString(extractKeywords(updatedVideoMemory.textChunks)),
                topics = objectMapper.writeValueAsString(extractTopics(updatedVideoMemory.textChunks)),
                videoMemory = updatedVideoMemory
            )
            
            updatedVideoMemory.memoryIndex = memoryIndex
            
            val finalVideoMemory = updatedVideoMemory.copy(status = VideoMemoryStatus.BUILT)
            logger.info("Built video index for: ${finalVideoMemory.title}")
            return videoMemoryRepository.save(finalVideoMemory)
            
        } catch (e: Exception) {
            logger.error("Failed to build video index for: ${updatedVideoMemory.title}", e)
            val errorVideoMemory = updatedVideoMemory.copy(status = VideoMemoryStatus.ERROR)
            videoMemoryRepository.save(errorVideoMemory)
            throw e
        }
    }
    
    @Transactional(readOnly = true)
    fun searchVideoMemories(query: String): List<VideoMemory> {
        return videoMemoryRepository.searchByTitleOrDescription(query)
    }
    
    @Transactional(readOnly = true)
    fun getVideoMemoryWithDetails(id: Long): VideoMemory {
        return videoMemoryRepository.findByIdWithAll(id)
            .orElseThrow { IllegalArgumentException("Video memory not found with id: $id") }
    }
    
    @Transactional(readOnly = true)
    fun getAllVideoMemories(pageable: Pageable): Page<VideoMemory> {
        return videoMemoryRepository.findAllByOrderByCreatedAtDesc(pageable)
    }
    
    @Transactional(readOnly = true)
    fun getVideoMemoriesByStatus(status: VideoMemoryStatus): List<VideoMemory> {
        return videoMemoryRepository.findByStatus(status)
    }
    
    @Transactional(readOnly = true)
    fun chatWithMemory(videoMemoryId: Long, question: String): String {
        val videoMemory = videoMemoryRepository.findByIdWithAll(videoMemoryId)
            .orElseThrow { IllegalArgumentException("Video memory not found with id: $videoMemoryId") }
        
        if (!videoMemory.isBuilt()) {
            throw IllegalStateException("Video memory index not built yet")
        }
        
        return generateResponseFromMemory(videoMemory, question)
    }
    
    @Transactional
    fun deleteVideoMemory(id: Long) {
        if (!videoMemoryRepository.existsById(id)) {
            throw IllegalArgumentException("Video memory not found with id: $id")
        }
        
        logger.info("Deleting video memory with id: $id")
        videoMemoryRepository.deleteById(id)
    }
    
    private fun generateVideoFilename(title: String): String {
        val sanitized = title.replace(Regex("[^A-Za-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "_")
            .lowercase()
        return "${sanitized}_${System.currentTimeMillis()}.mp4"
    }
    
    private fun generateIndexFilename(title: String): String {
        val sanitized = title.replace(Regex("[^A-Za-z0-9\\s]"), "")
            .replace(Regex("\\s+"), "_")
            .lowercase()
        return "${sanitized}_index_${System.currentTimeMillis()}.json"
    }
    
    private fun countWords(text: String): Int {
        return text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    
    private fun generateSummary(content: String, maxLength: Int = 150): String {
        return if (content.length <= maxLength) {
            content
        } else {
            val truncated = content.substring(0, maxLength)
            val lastSpace = truncated.lastIndexOf(' ')
            if (lastSpace > 0) truncated.substring(0, lastSpace) + "..." else truncated + "..."
        }
    }
    
    private fun buildIndexData(chunks: List<TextChunk>): Map<String, Any> {
        return mapOf(
            "chunks" to chunks.map { chunk ->
                mapOf(
                    "index" to chunk.chunkIndex,
                    "content" to chunk.content,
                    "summary" to chunk.summary,
                    "wordCount" to chunk.wordCount,
                    "type" to chunk.chunkType.name
                )
            },
            "metadata" to mapOf(
                "totalChunks" to chunks.size,
                "totalWords" to chunks.sumOf { it.wordCount },
                "averageWordsPerChunk" to chunks.map { it.wordCount }.average(),
                "createdAt" to LocalDateTime.now().toString()
            )
        )
    }
    
    private fun generateOverallSummary(chunks: List<TextChunk>): String {
        val allContent = chunks.joinToString(" ") { it.content }
        return generateSummary(allContent, 500)
    }
    
    private fun extractKeywords(chunks: List<TextChunk>): List<String> {
        val allWords = chunks.flatMap { chunk ->
            chunk.content.lowercase()
                .split(Regex("\\W+"))
                .filter { it.length > 3 }
        }
        
        return allWords.groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(20)
            .map { it.first }
    }
    
    private fun extractTopics(chunks: List<TextChunk>): List<String> {
        // Simple topic extraction based on common phrases
        val topics = mutableSetOf<String>()
        
        chunks.forEach { chunk ->
            val sentences = chunk.content.split(Regex("[.!?]+"))
            sentences.forEach { sentence ->
                if (sentence.length > 20) {
                    // Extract potential topics from longer sentences
                    val words = sentence.trim().split(Regex("\\s+"))
                    if (words.size >= 3) {
                        topics.add(words.take(3).joinToString(" ").lowercase())
                    }
                }
            }
        }
        
        return topics.take(10).toList()
    }
    
    private fun generateResponseFromMemory(videoMemory: VideoMemory, question: String): String {
        val relevantChunks = findRelevantChunks(videoMemory.textChunks, question)
        
        if (relevantChunks.isEmpty()) {
            return "I couldn't find relevant information about '$question' in this video memory."
        }
        
        val context = relevantChunks.joinToString("\n\n") { 
            "Chunk ${it.chunkIndex}: ${it.content}" 
        }
        
        return "Based on the video memory '${videoMemory.title}', here's what I found:\n\n$context"
    }
    
    private fun findRelevantChunks(chunks: List<TextChunk>, question: String): List<TextChunk> {
        val questionWords = question.lowercase().split(Regex("\\W+")).filter { it.length > 2 }
        
        return chunks.filter { chunk ->
            val chunkWords = chunk.content.lowercase().split(Regex("\\W+"))
            questionWords.any { questionWord ->
                chunkWords.any { it.contains(questionWord) }
            }
        }.take(3)  // Return top 3 relevant chunks
    }
} 