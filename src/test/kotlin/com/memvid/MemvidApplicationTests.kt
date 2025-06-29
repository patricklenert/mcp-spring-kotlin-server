package com.memvid

import com.memvid.domain.VideoMemoryStatus
import com.memvid.service.MemvidService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@TestPropertySource(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
class MemvidApplicationTests {

    @Autowired
    private lateinit var memvidService: MemvidService

    @Test
    fun contextLoads() {
        // Test that the Spring context loads successfully
        assertNotNull(memvidService)
    }

    @Test
    fun testVideoMemoryCreation() {
        // Test creating a video memory
        val title = "Test Video Memory"
        val description = "This is a test description"
        
        val videoMemory = memvidService.createVideoMemory(title, description)
        
        assertNotNull(videoMemory.id)
        assertEquals(title, videoMemory.title)
        assertEquals(description, videoMemory.description)
        assertEquals(VideoMemoryStatus.CREATED, videoMemory.status)
        assertTrue(videoMemory.videoFilename.contains("test_video_memory"))
        assertTrue(videoMemory.indexFilename.contains("test_video_memory_index"))
    }

    @Test
    fun testTextChunkAddition() {
        // Test adding text chunks to a video memory
        val videoMemory = memvidService.createVideoMemory("Test Memory", "Test Description")
        val chunks = listOf(
            "This is the first chunk of text.",
            "This is the second chunk of text.",
            "This is the third chunk of text."
        )
        
        val updatedMemory = memvidService.addTextChunks(videoMemory.id!!, chunks)
        
        assertEquals(3, updatedMemory.getTotalChunks())
        assertEquals(VideoMemoryStatus.CREATED, updatedMemory.status)
    }

    @Test
    fun testVideoIndexBuilding() {
        // Test building a video index
        val videoMemory = memvidService.createVideoMemory("Index Test", "Test building index")
        val chunks = listOf(
            "Artificial intelligence is transforming the world.",
            "Machine learning algorithms can learn from data.",
            "Deep learning uses neural networks for complex patterns."
        )
        
        memvidService.addTextChunks(videoMemory.id!!, chunks)
        val builtMemory = memvidService.buildVideoIndex(videoMemory.id)
        
        assertEquals(VideoMemoryStatus.BUILT, builtMemory.status)
        assertNotNull(builtMemory.memoryIndex)
        assertEquals(3, builtMemory.memoryIndex?.totalChunks)
        assertTrue((builtMemory.memoryIndex?.totalWords ?: 0) > 0)
    }

    @Test
    fun testChatWithMemory() {
        // Test chatting with a built video memory
        val videoMemory = memvidService.createVideoMemory("Chat Test", "Test chatting functionality")
        val chunks = listOf(
            "Spring Boot is a Java framework for building applications.",
            "It provides auto-configuration and embedded servers.",
            "Spring Boot makes it easy to create stand-alone applications."
        )
        
        memvidService.addTextChunks(videoMemory.id!!, chunks)
        memvidService.buildVideoIndex(videoMemory.id)
        
        val response = memvidService.chatWithMemory(videoMemory.id, "What is Spring Boot?")
        
        assertNotNull(response)
        assertTrue(response.contains("Spring Boot"))
    }

    @Test
    fun testSearchVideoMemories() {
        // Test searching video memories
        memvidService.createVideoMemory("Spring Framework Tutorial", "Learn Spring Framework")
        memvidService.createVideoMemory("React Development Guide", "Learn React development")
        
        val springResults = memvidService.searchVideoMemories("Spring")
        val reactResults = memvidService.searchVideoMemories("React")
        
        assertTrue(springResults.isNotEmpty())
        assertTrue(reactResults.isNotEmpty())
        assertTrue(springResults.any { it.title.contains("Spring") })
        assertTrue(reactResults.any { it.title.contains("React") })
    }

    @Test
    fun testGetVideoMemoriesByStatus() {
        // Test getting video memories by status
        val memory1 = memvidService.createVideoMemory("Memory 1", "First memory")
        val memory2 = memvidService.createVideoMemory("Memory 2", "Second memory")
        
        memvidService.addTextChunks(memory1.id!!, listOf("Content 1"))
        memvidService.buildVideoIndex(memory1.id)
        
        memvidService.addTextChunks(memory2.id!!, listOf("Content 2"))
        // Don't build index for memory2
        
        val createdMemories = memvidService.getVideoMemoriesByStatus(VideoMemoryStatus.CREATED)
        val builtMemories = memvidService.getVideoMemoriesByStatus(VideoMemoryStatus.BUILT)
        
        assertTrue(createdMemories.isNotEmpty())
        assertTrue(builtMemories.isNotEmpty())
    }
} 