package com.memvid.repository

import com.memvid.domain.VideoMemory
import com.memvid.domain.VideoMemoryStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface VideoMemoryRepository : JpaRepository<VideoMemory, Long> {
    
    fun findByVideoFilename(videoFilename: String): Optional<VideoMemory>
    
    fun findByIndexFilename(indexFilename: String): Optional<VideoMemory>
    
    fun findByStatus(status: VideoMemoryStatus): List<VideoMemory>
    
    fun findByStatusAndCreatedAtAfter(status: VideoMemoryStatus, createdAt: LocalDateTime): List<VideoMemory>
    
    fun findByTitleContainingIgnoreCase(title: String): List<VideoMemory>
    
    fun findByDescriptionContainingIgnoreCase(description: String): List<VideoMemory>
    
    @Query("SELECT vm FROM VideoMemory vm WHERE vm.title LIKE %:searchTerm% OR vm.description LIKE %:searchTerm%")
    fun searchByTitleOrDescription(@Param("searchTerm") searchTerm: String): List<VideoMemory>
    
    @Query("SELECT vm FROM VideoMemory vm WHERE vm.createdAt BETWEEN :startDate AND :endDate")
    fun findByDateRange(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<VideoMemory>
    
    @Query("SELECT COUNT(vm) FROM VideoMemory vm WHERE vm.status = :status")
    fun countByStatus(@Param("status") status: VideoMemoryStatus): Long
    
    @Query("SELECT vm FROM VideoMemory vm LEFT JOIN FETCH vm.textChunks WHERE vm.id = :id")
    fun findByIdWithChunks(@Param("id") id: Long): Optional<VideoMemory>
    
    @Query("SELECT vm FROM VideoMemory vm LEFT JOIN FETCH vm.memoryIndex WHERE vm.id = :id")
    fun findByIdWithIndex(@Param("id") id: Long): Optional<VideoMemory>
    
    @Query("SELECT vm FROM VideoMemory vm LEFT JOIN FETCH vm.textChunks LEFT JOIN FETCH vm.memoryIndex WHERE vm.id = :id")
    fun findByIdWithAll(@Param("id") id: Long): Optional<VideoMemory>
    
    fun findAllByOrderByCreatedAtDesc(): List<VideoMemory>
    
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<VideoMemory>
    
    @Query("SELECT DISTINCT vm FROM VideoMemory vm WHERE SIZE(vm.textChunks) > :minChunks")
    fun findMemoriesWithMinimumChunks(@Param("minChunks") minChunks: Int): List<VideoMemory>
} 