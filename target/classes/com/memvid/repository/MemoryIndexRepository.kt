package com.memvid.repository

import com.memvid.domain.MemoryIndex
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MemoryIndexRepository : JpaRepository<MemoryIndex, Long> {
    
    fun findByVideoMemoryId(videoMemoryId: Long): Optional<MemoryIndex>
    
    @Query("SELECT mi FROM MemoryIndex mi WHERE mi.totalChunks > :minChunks")
    fun findLargeIndexes(@Param("minChunks") minChunks: Int): List<MemoryIndex>
    
    @Query("SELECT mi FROM MemoryIndex mi WHERE mi.totalWords > :minWords")
    fun findIndexesWithMinWords(@Param("minWords") minWords: Int): List<MemoryIndex>
    
    @Query("SELECT mi FROM MemoryIndex mi WHERE mi.summary LIKE %:searchTerm% OR mi.keywords LIKE %:searchTerm%")
    fun searchBySummaryOrKeywords(@Param("searchTerm") searchTerm: String): List<MemoryIndex>
    
    @Query("SELECT AVG(mi.totalChunks) FROM MemoryIndex mi")
    fun getAverageTotalChunks(): Double?
    
    @Query("SELECT AVG(mi.totalWords) FROM MemoryIndex mi")
    fun getAverageTotalWords(): Double?
    
    fun findAllByOrderByTotalChunksDesc(): List<MemoryIndex>
    
    fun findAllByOrderByTotalWordsDesc(): List<MemoryIndex>
} 