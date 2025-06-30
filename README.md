# MCP Spring Server

A comprehensive **Model Context Protocol (MCP) Server** built with **Spring Boot** and **Kotlin** that demonstrates video memory management functionality similar to the `memvid` concept. This project showcases key Spring Boot concepts including **JPA**, **WebMVC**, **Repositories**, **GraphQL**, and **MCP integration** using the official Spring AI MCP library.

## 🚀 Features

### Core Technologies
- **Spring Boot 3.3.0** with Kotlin
- **Spring AI MCP Integration** (Official MCP Library)
- **JPA** with Hibernate for data persistence
- **Spring WebMVC** for REST APIs
- **GraphQL** for flexible query interface
- **H2 Database** for demonstration
- **Spring Actuator** for monitoring

### MCP Tools Available
- `create_video_memory` - Create new video memories
- `add_chunks` - Add text chunks to video memories
- `build_video` - Build searchable indexes
- `chat_with_memory` - Chat with video memory content
- `list_video_memories` - List all video memories
- `get_memory_details` - Get detailed information
- `search_memories` - Search by title/description
- `get_memory_status` - System status overview
- `delete_memory` - Remove video memories

### API Endpoints
- **MCP Server**: `/mcp` (Server-Sent Events)
- **REST API**: `/api/v1/memvid/**`
- **GraphQL**: `/graphql`
- **GraphiQL**: `/graphiql` (Interactive GraphQL playground)
- **H2 Console**: `/h2-console`
- **Health Check**: `/actuator/health`

## 🏗️ Architecture

The application follows a layered architecture demonstrating Spring Boot best practices:

```
┌─────────────────────┐
│   MCP Tools Layer   │  ← @McpServerTool annotations
├─────────────────────┤
│ Controllers/GraphQL │  ← @RestController, @Controller
├─────────────────────┤
│   Service Layer     │  ← @Service, @Transactional
├─────────────────────┤
│ Repository Layer    │  ← @Repository, Spring Data JPA
├─────────────────────┤
│   Domain Layer      │  ← @Entity, JPA annotations
└─────────────────────┘
```

## 🛠️ Setup & Installation

### Prerequisites
- Java 17+
- Maven 3.6+

### Run the Application

1. **Clone and build**:
```bash
git clone <repository-url>
cd mcp-spring-server
mvn clean install
```

2. **Start the server**:
```bash
mvn spring-boot:run
```

3. **Access the application**:
- Application: http://localhost:8080
- GraphiQL: http://localhost:8080/graphiql
- H2 Console: http://localhost:8080/h2-console
- Health: http://localhost:8080/actuator/health

## 📝 Usage Examples

### Using as MCP Server

Connect any MCP client to: `http://localhost:8080/mcp`

Example MCP client usage (Python):
```python
# This is how the MCP tools would be used from a client

# Create video memory
create_video_memory(title="Important Lecture", description="AI and Machine Learning basics")

# Add text chunks  
add_chunks(video_memory_id=1, chunks=[
    "Introduction to artificial intelligence and its applications",
    "Machine learning algorithms and their use cases", 
    "Deep learning and neural networks overview"
])

# Build the video index
build_video(video_memory_id=1)

# Chat with the memory
chat_with_memory(video_memory_id=1, question="What are the main AI applications discussed?")
```

### REST API Examples

```bash
# Create a video memory
curl -X POST http://localhost:8080/api/v1/memvid/memories \
  -H "Content-Type: application/json" \
  -d '{"title": "Spring Boot Tutorial", "description": "Comprehensive Spring Boot guide"}'

# Add text chunks
curl -X POST http://localhost:8080/api/v1/memvid/memories/1/chunks \
  -H "Content-Type: application/json" \
  -d '{"chunks": ["Spring Boot basics", "Dependency injection", "Data JPA usage"]}'

# Build video index
curl -X POST http://localhost:8080/api/v1/memvid/memories/1/build

# Chat with memory
curl -X POST http://localhost:8080/api/v1/memvid/memories/1/chat \
  -H "Content-Type: application/json" \
  -d '{"question": "What is dependency injection?"}'

# List all memories
curl http://localhost:8080/api/v1/memvid/memories

# Search memories
curl "http://localhost:8080/api/v1/memvid/memories/search?query=Spring"
```

### GraphQL Examples

Access GraphiQL at http://localhost:8080/graphiql

```graphql
# Create a video memory
mutation {
  createVideoMemory(title: "GraphQL Tutorial", description: "Learning GraphQL with Spring") {
    id
    title
    status
    videoFilename
  }
}

# Query video memories with pagination
query {
  videoMemories(page: 0, size: 10) {
    content {
      id
      title
      status
      totalChunks
    }
    totalElements
    totalPages
  }
}

# Get detailed memory information
query {
  videoMemory(id: "1") {
    id
    title
    description
    status
    textChunks {
      content
      wordCount
      summary
    }
    memoryIndex {
      totalWords
      keywords
      topics
    }
  }
}

# Chat with memory
mutation {
  chatWithMemory(videoMemoryId: "1", question: "Explain GraphQL benefits") {
    response
  }
}

# System status
query {
  systemStatus {
    totalMemories
    readyForChat
    processing
    statusCounts {
      CREATED
      BUILT
      PROCESSING
      ERROR
    }
  }
}
```

## 🏛️ Spring Boot Concepts Demonstrated

### 1. **JPA & Repositories**
- Entity relationships (`@OneToMany`, `@OneToOne`)
- Custom query methods
- `@Query` annotations for complex queries
- Transaction management with `@Transactional`

### 2. **WebMVC**
- RESTful controllers with `@RestController`
- Request/Response DTOs with validation
- Exception handling with `@ExceptionHandler`
- CORS configuration

### 3. **GraphQL Integration**
- Schema-first approach
- Query and Mutation resolvers
- Field-level resolvers with `@SchemaMapping`
- Pagination support

### 4. **Spring AI MCP**
- `@McpServerTool` annotations for tool registration
- Automatic MCP server setup via Spring Boot starter
- HTTP Server-Sent Events transport
- Tool parameter validation and error handling

### 5. **Configuration Management**
- `application.yml` configuration
- Profile-specific settings
- Auto-configuration with Spring Boot starters

## 🔧 Configuration

### Application Properties (application.yml)
```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: memvid-server
        capabilities:
          tools: true
          resources: true
          prompts: true
        webmvc:
          enabled: true
          endpoint: "/mcp"
```

### Key Dependencies
```xml
<!-- Spring AI MCP Integration -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-server-spring-boot-starter</artifactId>
</dependency>

<!-- GraphQL Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>

<!-- JPA & Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

## 📊 Database Schema

The application uses three main entities:

- **VideoMemory**: Main entity storing video memory metadata
- **TextChunk**: Individual text segments with indexing
- **MemoryIndex**: Built indexes for efficient searching

## 🧪 Testing

Access the H2 console at http://localhost:8080/h2-console:
- JDBC URL: `jdbc:h2:mem:memvid`
- Username: `sa`
- Password: `password`

## 🔍 Monitoring

Spring Actuator endpoints available:
- `/actuator/health` - Application health
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

## 🤝 MCP Integration

This server implements the Model Context Protocol (MCP) specification using Spring AI's official MCP library. It can be used with any MCP-compatible client, including:

- Claude Desktop
- Custom MCP clients
- AI development tools
- Chat applications

The server exposes its functionality through MCP tools, making it easy for AI models to interact with video memory management capabilities.

## 📚 Learning Outcomes

This project demonstrates:

1. **Modern Spring Boot Architecture**: Clean separation of concerns
2. **MCP Integration**: Official Spring AI MCP library usage  
3. **Multi-API Support**: REST, GraphQL, and MCP in one application
4. **JPA Best Practices**: Entity modeling and repository patterns
5. **Kotlin with Spring**: Leveraging Kotlin's features with Spring Framework
6. **API Design**: RESTful principles and GraphQL schema design
7. **Configuration Management**: Spring Boot auto-configuration and properties

## 📄 License

This project is created for educational purposes to demonstrate Spring Boot and MCP integration concepts. 