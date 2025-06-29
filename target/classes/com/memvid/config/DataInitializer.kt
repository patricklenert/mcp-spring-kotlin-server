package com.memvid.config

import com.memvid.service.MemvidService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val memvidService: MemvidService
) : CommandLineRunner {
    
    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)
    
    override fun run(vararg args: String?) {
        logger.info("Initializing sample data...")
        
        try {
            // Create sample video memories
            val springBootMemory = memvidService.createVideoMemory(
                title = "Spring Boot Fundamentals",
                description = "Comprehensive guide to Spring Boot development with best practices"
            )
            
            val springBootChunks = listOf(
                "Spring Boot is an opinionated framework that simplifies Spring application development by providing auto-configuration and embedded servers.",
                "Dependency injection is a core principle of Spring, allowing for loose coupling and easier testing of components.",
                "Spring Data JPA provides a powerful abstraction over database operations, offering repository patterns and query methods.",
                "Spring Boot Actuator provides production-ready features like health checks, metrics, and monitoring endpoints.",
                "Auto-configuration in Spring Boot automatically configures your application based on the dependencies present in the classpath."
            )
            
            memvidService.addTextChunks(springBootMemory.id!!, springBootChunks)
            memvidService.buildVideoIndex(springBootMemory.id)
            
            val aiMemory = memvidService.createVideoMemory(
                title = "Artificial Intelligence Overview",
                description = "Introduction to AI, machine learning, and their applications in modern technology"
            )
            
            val aiChunks = listOf(
                "Artificial Intelligence (AI) refers to the simulation of human intelligence in machines programmed to think and learn.",
                "Machine Learning is a subset of AI that enables computers to learn and improve from experience without being explicitly programmed.",
                "Deep Learning uses neural networks with multiple layers to model and understand complex patterns in data.",
                "Natural Language Processing (NLP) enables machines to understand, interpret, and generate human language.",
                "Computer Vision allows machines to interpret and understand visual information from the world around them.",
                "AI applications include autonomous vehicles, medical diagnosis, recommendation systems, and voice assistants."
            )
            
            memvidService.addTextChunks(aiMemory.id!!, aiChunks)
            memvidService.buildVideoIndex(aiMemory.id)
            
            val mcpMemory = memvidService.createVideoMemory(
                title = "Model Context Protocol Explained",
                description = "Understanding MCP and its role in AI application development"
            )
            
            val mcpChunks = listOf(
                "Model Context Protocol (MCP) is an open standard for connecting AI models with external tools and data sources.",
                "MCP enables secure and controlled access to resources, allowing AI models to interact with databases, APIs, and files.",
                "The protocol supports multiple transport mechanisms including STDIO, HTTP Server-Sent Events, and WebSocket connections.",
                "MCP tools are functions that AI models can discover and execute to perform specific tasks or retrieve information.",
                "Resource management in MCP allows AI models to access and read various types of content through URI-based addressing.",
                "Spring AI provides comprehensive MCP integration through boot starters and auto-configuration for easy setup."
            )
            
            memvidService.addTextChunks(mcpMemory.id!!, mcpChunks)
            memvidService.buildVideoIndex(mcpMemory.id)
            
            // Create a memory without building index to show different states
            val kotlinMemory = memvidService.createVideoMemory(
                title = "Kotlin for Spring Development",
                description = "Leveraging Kotlin's features for better Spring Boot applications"
            )
            
            val kotlinChunks = listOf(
                "Kotlin provides excellent interoperability with Java, making it perfect for Spring development.",
                "Data classes in Kotlin reduce boilerplate code for DTOs and entity classes.",
                "Null safety in Kotlin helps prevent NullPointerException at compile time.",
                "Extension functions allow adding functionality to existing classes without inheritance."
            )
            
            memvidService.addTextChunks(kotlinMemory.id!!, kotlinChunks)
            // Note: Not building index for this one to demonstrate different states
            
            logger.info("Sample data initialization completed successfully!")
            logger.info("Created video memories:")
            logger.info("1. ${springBootMemory.title} (BUILT)")
            logger.info("2. ${aiMemory.title} (BUILT)")
            logger.info("3. ${mcpMemory.title} (BUILT)")
            logger.info("4. ${kotlinMemory.title} (CREATED - not built)")
            
        } catch (e: Exception) {
            logger.error("Error during data initialization", e)
        }
    }
} 