package com.memvid.controller

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class GraphiQLController {

    @GetMapping("/graphiql", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun graphiql(): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>GraphQL Playground - MCP Spring Server</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=JetBrains+Mono:wght@400;500;600&display=swap" rel="stylesheet">
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                
                body {
                    min-height: 100vh;
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                    background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);
                    background-attachment: fixed;
                    color: #ffffff;
                    overflow-y: auto;
                }
                
                .container {
                    min-height: 100vh;
                    display: flex;
                    flex-direction: column;
                    max-width: 1400px;
                    margin: 0 auto;
                    padding: 0 24px 40px;
                }
                
                .header {
                    padding: 32px 0;
                    text-align: center;
                    background: transparent;
                }
                
                .header h1 {
                    font-size: 36px;
                    font-weight: 800;
                    background: linear-gradient(135deg, #60a5fa 0%, #a78bfa 50%, #f472b6 100%);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                    background-clip: text;
                    margin-bottom: 12px;
                    letter-spacing: -0.02em;
                }
                
                .header p {
                    color: #94a3b8;
                    font-size: 16px;
                    font-weight: 400;
                    opacity: 0.8;
                }
                
                .main-content {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 24px;
                    margin-bottom: 24px;
                    min-height: 600px;
                }
                
                .panel {
                    background: rgba(255, 255, 255, 0.05);
                    border: 1px solid rgba(255, 255, 255, 0.1);
                    border-radius: 20px;
                    backdrop-filter: blur(20px);
                    display: flex;
                    flex-direction: column;
                    overflow: hidden;
                }
                
                .panel-header {
                    padding: 24px 28px 20px;
                    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
                    background: rgba(255, 255, 255, 0.02);
                }
                
                .panel-title {
                    font-size: 20px;
                    font-weight: 700;
                    color: #ffffff;
                    margin-bottom: 16px;
                    display: flex;
                    align-items: center;
                    gap: 8px;
                }
                
                .panel-icon {
                    font-size: 24px;
                }
                
                .examples {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 12px;
                }
                
                .example-btn {
                    background: linear-gradient(135deg, rgba(96, 165, 250, 0.2) 0%, rgba(167, 139, 250, 0.2) 100%);
                    border: 1px solid rgba(96, 165, 250, 0.3);
                    color: #60a5fa;
                    padding: 12px 16px;
                    border-radius: 12px;
                    font-size: 13px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    backdrop-filter: blur(10px);
                    text-align: center;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 6px;
                }
                
                .example-btn:hover {
                    background: linear-gradient(135deg, rgba(96, 165, 250, 0.3) 0%, rgba(167, 139, 250, 0.3) 100%);
                    border-color: rgba(96, 165, 250, 0.5);
                    transform: translateY(-2px);
                    box-shadow: 0 8px 25px rgba(96, 165, 250, 0.2);
                }
                
                .example-btn:active {
                    transform: translateY(0);
                }
                
                .query-input-container {
                    flex: 1;
                    padding: 28px;
                    display: flex;
                    flex-direction: column;
                    gap: 20px;
                }
                
                .query-textarea {
                    flex: 1;
                    min-height: 700px;
                    max-height: none;
                    padding: 20px;
                    background: rgba(0, 0, 0, 0.4);
                    border: 1px solid rgba(255, 255, 255, 0.1);
                    border-radius: 16px;
                    font-family: 'JetBrains Mono', 'SF Mono', 'Monaco', 'Consolas', monospace;
                    font-size: 14px;
                    line-height: 1.7;
                    resize: vertical;
                    outline: none;
                    transition: all 0.3s ease;
                    color: #e2e8f0;
                    backdrop-filter: blur(10px);
                    overflow: visible;
                }
                
                .query-textarea:focus {
                    border-color: rgba(96, 165, 250, 0.5);
                    box-shadow: 0 0 0 1px rgba(96, 165, 250, 0.3);
                    background: rgba(0, 0, 0, 0.6);
                }
                
                .query-textarea::placeholder {
                    color: #64748b;
                }
                
                .action-buttons {
                    display: flex;
                    gap: 16px;
                }
                
                .btn-primary {
                    background: linear-gradient(135deg, #60a5fa 0%, #a78bfa 100%);
                    color: white;
                    border: none;
                    padding: 16px 32px;
                    border-radius: 14px;
                    font-size: 15px;
                    font-weight: 700;
                    cursor: pointer;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    flex: 1;
                    box-shadow: 0 4px 20px rgba(96, 165, 250, 0.3);
                    position: relative;
                    overflow: hidden;
                }
                
                .btn-primary:before {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: -100%;
                    width: 100%;
                    height: 100%;
                    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
                    transition: left 0.5s;
                }
                
                .btn-primary:hover:before {
                    left: 100%;
                }
                
                .btn-primary:hover {
                    transform: translateY(-3px);
                    box-shadow: 0 8px 30px rgba(96, 165, 250, 0.4);
                }
                
                .btn-secondary {
                    background: rgba(255, 255, 255, 0.05);
                    color: #94a3b8;
                    border: 1px solid rgba(255, 255, 255, 0.1);
                    padding: 16px 32px;
                    border-radius: 14px;
                    font-size: 15px;
                    font-weight: 600;
                    cursor: pointer;
                    transition: all 0.3s ease;
                    flex: 1;
                    backdrop-filter: blur(10px);
                }
                
                .btn-secondary:hover {
                    background: rgba(255, 255, 255, 0.1);
                    border-color: rgba(255, 255, 255, 0.2);
                    color: #ffffff;
                    transform: translateY(-2px);
                }
                
                .result-container {
                    flex: 1;
                    padding: 28px;
                    display: flex;
                    flex-direction: column;
                }
                
                .result-display {
                    flex: 1;
                    background: rgba(0, 0, 0, 0.6);
                    border: 1px solid rgba(255, 255, 255, 0.1);
                    border-radius: 16px;
                    padding: 24px;
                    font-family: 'JetBrains Mono', 'SF Mono', 'Monaco', 'Consolas', monospace;
                    font-size: 13px;
                    line-height: 1.7;
                    white-space: pre-wrap;
                    overflow: visible;
                    color: #e2e8f0;
                    position: relative;
                    backdrop-filter: blur(20px);
                    min-height: 700px;
                    word-wrap: break-word;
                }
                
                .result-display.loading {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: #60a5fa;
                    font-style: italic;
                    font-size: 14px;
                }
                
                .result-display.error {
                    color: #f87171;
                    border-color: rgba(248, 113, 113, 0.3);
                }
                
                .result-display.success {
                    color: #34d399;
                    border-color: rgba(52, 211, 153, 0.3);
                }
                
                /* Enhanced page scrollbar */
                body::-webkit-scrollbar {
                    width: 8px;
                }
                
                body::-webkit-scrollbar-track {
                    background: rgba(15, 15, 35, 0.8);
                }
                
                body::-webkit-scrollbar-thumb {
                    background: rgba(96, 165, 250, 0.4);
                    border-radius: 4px;
                }
                
                body::-webkit-scrollbar-thumb:hover {
                    background: rgba(96, 165, 250, 0.6);
                }
                
                /* Loading animation */
                .loading-dots {
                    display: inline-block;
                }
                
                .loading-dots::after {
                    content: '';
                    animation: dots 1.5s infinite;
                }
                
                @keyframes dots {
                    0%, 20% { content: ''; }
                    40% { content: '.'; }
                    60% { content: '..'; }
                    80%, 100% { content: '...'; }
                }
                
                /* Floating keyboard hint */
                .shortcut-hint {
                    position: absolute;
                    bottom: 16px;
                    right: 20px;
                    font-size: 11px;
                    color: #64748b;
                    background: rgba(0, 0, 0, 0.8);
                    padding: 6px 10px;
                    border-radius: 8px;
                    pointer-events: none;
                    border: 1px solid rgba(255, 255, 255, 0.1);
                    backdrop-filter: blur(10px);
                }
                
                /* Responsive design */
                @media (max-width: 768px) {
                    .container {
                        padding: 0 16px;
                    }
                    
                    .main-content {
                        grid-template-columns: 1fr;
                        gap: 16px;
                    }
                    
                    .header h1 {
                        font-size: 28px;
                    }
                    
                    .examples {
                        grid-template-columns: 1fr;
                        gap: 8px;
                    }
                    
                    .action-buttons {
                        flex-direction: column;
                    }
                }
                
                /* Subtle animations */
                @keyframes fadeInUp {
                    from {
                        opacity: 0;
                        transform: translateY(20px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
                
                .panel {
                    animation: fadeInUp 0.6s ease-out;
                }
                
                .panel:nth-child(2) {
                    animation-delay: 0.1s;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>🚀 GraphQL Playground</h1>
                    <p>Explore the MCP Spring Server Video Memory Management API</p>
                </div>
                
                <div class="main-content">
                    <div class="panel">
                        <div class="panel-header">
                            <div class="panel-title">
                                <span class="panel-icon">⚡</span>
                                Query Editor
                            </div>
                            <div class="examples">
                                <button class="example-btn" onclick="loadExample('memories')" title="Get all video memories">
                                    📚 Get Memories
                                </button>
                                <button class="example-btn" onclick="loadExample('search')" title="Search memories by keyword">
                                    🔍 Search Memories
                                </button>
                                <button class="example-btn" onclick="loadExample('detail')" title="Get detailed memory information">
                                    📖 Get Detail
                                </button>
                                <button class="example-btn" onclick="loadExample('create')" title="Create a new video memory">
                                    ➕ Create Memory
                                </button>
                            </div>
                        </div>
                        
                        <div class="query-input-container">
                            <textarea 
                                id="query" 
                                class="query-textarea"
                                placeholder="✨ Enter your GraphQL query here and explore the API..."
                                spellcheck="false"
                            ># 🎯 Welcome to GraphQL Playground
# 
# This is your interactive GraphQL IDE for the MCP Spring Server.
# Click the example buttons above or write your own queries below.
#
# 💡 Tip: Use Ctrl+Enter to execute queries quickly!

query GetAllVideoMemories {
  videoMemories {
    content {
      id
      title
      description
      status
      totalChunks
      createdAt
    }
    totalElements
    totalPages
  }
}</textarea>
                            
                            <div class="action-buttons">
                                <button class="btn-primary" onclick="executeQuery()" title="Execute the GraphQL query">
                                    ▶️ Execute Query
                                </button>
                                <button class="btn-secondary" onclick="clearQuery()" title="Clear the query editor">
                                    🗑️ Clear
                                </button>
                            </div>
                        </div>
                    </div>
                    
                    <div class="panel">
                        <div class="panel-header">
                            <div class="panel-title">
                                <span class="panel-icon">📊</span>
                                Response
                            </div>
                        </div>
                        
                        <div class="result-container">
                            <div id="result" class="result-display">
{
  "status": "🚀 Ready to execute queries",
  "message": "Welcome to the GraphQL Playground!",
  "instructions": [
    "Click 'Execute Query' to run your GraphQL query",
    "Try the example buttons for quick demos", 
    "Use Ctrl+Enter as a keyboard shortcut",
    "Explore the full API schema and capabilities"
  ],
  "endpoints": {
    "graphql": "/graphql",
    "playground": "/graphiql", 
    "rest_api": "/api/v1/memvid"
  }
}
                            </div>
                            <div class="shortcut-hint">⌨️ Ctrl+Enter to execute</div>
                        </div>
                    </div>
                </div>
            </div>

            <script>
                const examples = {
                    memories: `# 📚 Get All Video Memories
# Retrieves a paginated list of all video memories with basic information

query GetAllVideoMemories {
  videoMemories {
    content {
      id
      title
      description
      status
      totalChunks
      createdAt
    }
    totalElements
    totalPages
  }
}`,
                    search: `# 🔍 Search Video Memories  
# Search for video memories containing specific keywords

query SearchMemories {
  searchVideoMemories(query: "Spring") {
    id
    title
    description
    status
  }
}`,
                    detail: `# 📖 Get Detailed Video Memory Information
# Retrieve complete details including text chunks and memory index

query GetVideoMemoryDetail {
  videoMemory(id: "1") {
    id
    title
    description
    status
    textChunks {
      id
      content
      summary
      wordCount
    }
    memoryIndex {
      totalChunks
      totalWords
      summary
      keywords
      topics
    }
  }
}`,
                    create: `# ➕ Create New Video Memory
# Create a new video memory with title and description

mutation CreateVideoMemory {
  createVideoMemory(
    title: "My New Video Memory"
    description: "A test video memory created via GraphQL"
  ) {
    id
    title
    status
    createdAt
  }
}`
                };

                function loadExample(type) {
                    const textarea = document.getElementById('query');
                    textarea.value = examples[type];
                    
                    // Add visual feedback
                    textarea.style.borderColor = '#10b981';
                    setTimeout(() => {
                        textarea.style.borderColor = '#e2e8f0';
                    }, 300);
                }

                function clearQuery() {
                    const textarea = document.getElementById('query');
                    const resultDiv = document.getElementById('result');
                    
                    textarea.value = '';
                    resultDiv.className = 'result-display';
                    resultDiv.textContent = `{
  "message": "✨ Query cleared successfully!",
  "instruction": "Enter a new GraphQL query and click 'Execute Query' to see results.",
  "tip": "💡 Try the example buttons for quick demos"
}`;
                }

                async function executeQuery() {
                    const query = document.getElementById('query').value.trim();
                    const resultDiv = document.getElementById('result');
                    
                    if (!query) {
                        resultDiv.className = 'result-display error';
                        resultDiv.textContent = `{
  "error": "❌ No query provided",
  "message": "Please enter a GraphQL query before executing.",
  "suggestion": "💡 Try clicking one of the example buttons above"
}`;
                        return;
                    }
                    
                    // Show loading state
                    resultDiv.className = 'result-display loading';
                    resultDiv.innerHTML = `⏳ Executing query<span class="loading-dots"></span>`;
                    
                    try {
                        const startTime = Date.now();
                        const response = await fetch('/graphql', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'Accept': 'application/json'
                            },
                            body: JSON.stringify({
                                query: query
                            })
                        });
                        
                        const result = await response.json();
                        const executionTime = Date.now() - startTime;
                        
                        if (result.errors) {
                            resultDiv.className = 'result-display error';
                            resultDiv.textContent = JSON.stringify({
                                "status": "❌ GraphQL Error",
                                "errors": result.errors,
                                "executionTime": executionTime + "ms"
                            }, null, 2);
                        } else {
                            resultDiv.className = 'result-display success';
                            const formattedResult = {
                                "status": "✅ Query executed successfully",
                                "executionTime": executionTime + "ms",
                                "data": result.data
                            };
                            resultDiv.textContent = JSON.stringify(formattedResult, null, 2);
                        }
                        
                    } catch (error) {
                        resultDiv.className = 'result-display error';
                        resultDiv.textContent = JSON.stringify({
                            "status": "❌ Network Error",
                            "error": error.message,
                            "suggestion": "Check if the GraphQL server is running and accessible"
                        }, null, 2);
                    }
                }

                // Enhanced keyboard shortcuts
                document.getElementById('query').addEventListener('keydown', function(e) {
                    // Ctrl+Enter to execute
                    if (e.ctrlKey && e.key === 'Enter') {
                        e.preventDefault();
                        executeQuery();
                    }
                    
                    // Ctrl+L to clear (like terminal)
                    if (e.ctrlKey && e.key.toLowerCase() === 'l') {
                        e.preventDefault();
                        clearQuery();
                    }
                    
                    // Tab for better indentation
                    if (e.key === 'Tab') {
                        e.preventDefault();
                        const start = this.selectionStart;
                        const end = this.selectionEnd;
                        const value = this.value;
                        this.value = value.substring(0, start) + '  ' + value.substring(end);
                        this.selectionStart = this.selectionEnd = start + 2;
                    }
                });

                // Auto-resize textarea based on content
                const textarea = document.getElementById('query');
                textarea.addEventListener('input', function() {
                    this.style.height = 'auto';
                    this.style.height = Math.min(this.scrollHeight, 400) + 'px';
                });

                // Add welcome animation
                document.addEventListener('DOMContentLoaded', function() {
                    const header = document.querySelector('.header');
                    header.style.opacity = '0';
                    header.style.transform = 'translateY(-20px)';
                    
                    setTimeout(() => {
                        header.style.transition = 'all 0.6s ease';
                        header.style.opacity = '1';
                        header.style.transform = 'translateY(0)';
                    }, 100);
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }
} 