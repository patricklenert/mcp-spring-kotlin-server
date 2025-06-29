package com.memvid.repository

import com.memvid.domain.ChunkType
import com.memvid.domain.TextChunk
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TextChunkRepository : JpaRepository<TextChunk, Long> {
    
    fun findByVideoMemoryId(videoMemoryId: Long): List<TextChunk>
    
    fun findByVideoMemoryIdOrderByChunkIndex(videoMemoryId: Long): List<TextChunk>
    
    fun findByChunkType(chunkType: ChunkType): List<TextChunk>
    
    fun findByVideoMemoryIdAndChunkType(videoMemoryId: Long, chunkType: ChunkType): List<TextChunk>
    
    @Query("SELECT tc FROM TextChunk tc WHERE tc.videoMemory.id = :videoMemoryId AND tc.wordCount > :minWords")
    fun findLongChunksByVideoMemory(@Param("videoMemoryId") videoMemoryId: Long, @Param("minWords") minWords: Int): List<TextChunk>
    
    @Query("SELECT tc FROM TextChunk tc WHERE tc.content LIKE %:searchTerm% OR tc.summary LIKE %:searchTerm%")
    fun searchByContentOrSummary(@Param("searchTerm") searchTerm: String): List<TextChunk>
    
    @Query("SELECT COUNT(tc) FROM TextChunk tc WHERE tc.videoMemory.id = :videoMemoryId")
    fun countByVideoMemoryId(@Param("videoMemoryId") videoMemoryId: Long): Long
    
    @Query("SELECT SUM(tc.wordCount) FROM TextChunk tc WHERE tc.videoMemory.id = :videoMemoryId")
    fun getTotalWordCountByVideoMemoryId(@Param("videoMemoryId") videoMemoryId: Long): Long?
    
    @Query("SELECT AVG(tc.wordCount) FROM TextChunk tc WHERE tc.videoMemory.id = :videoMemoryId")
    fun getAverageWordCountByVideoMemoryId(@Param("videoMemoryId") videoMemoryId: Long): Double?
    
    fun findTopChunkByVideoMemoryIdOrderByChunkIndexDesc(videoMemoryId: Long): TextChunk?
} 