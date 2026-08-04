# MiniChatGPT

MiniChatGPT is a Spring Boot application that lets you chat with your own PDFs using local LLMs. It combines:

- Spring Boot and Java 21
- LangChain4j for LLM orchestration
- Ollama for local chat and embedding models
- Elasticsearch for vector search over uploaded PDF content
- PDF ingestion and chunking for retrieval-augmented chat

## What this project does

The app currently supports:

- Uploading PDF documents through a REST endpoint
- Splitting uploaded PDFs into text chunks
- Generating embeddings locally with Ollama
- Storing embeddings in Elasticsearch
- Retrieving relevant document chunks during chat
- Streaming chat responses from Ollama

## Tech stack

- Java 21
- Spring Boot 4.1.0
- Maven Wrapper
- LangChain4j
- Ollama
- Elasticsearch 8.x
- MongoDB and Redis (included as dependencies, but the current README focuses on the local LLM + vector-search flow)

## Prerequisites

Before running the project locally, make sure you have:

- Java 21 installed
- Maven or the provided Maven wrapper
- Docker Desktop (recommended for local Elasticsearch and Ollama)
- At least 4 GB RAM available for local containers

## Local setup

### 1. Start Elasticsearch locally with vector search support

The project is wired to connect to Elasticsearch at http://localhost:9200 using the credentials defined in the app configuration.

Run Elasticsearch in Docker:

```bash
docker run -d --name elasticsearch \
  -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=true" \
  -e "xpack.security.http.ssl.enabled=false" \
  -e "ELASTIC_PASSWORD=password" \
  -e "xpack.license.self_generated.type=trial" \
  docker.elastic.co/elasticsearch/elasticsearch:8.15.0
```

or install using windows installer

Verify it is reachable:

```bash
curl -u elastic:password http://localhost:9200
```

If you prefer to run with security disabled for a purely local dev environment, you can do that too, but the application configuration currently expects the username/password above.

### 2. Start Ollama locally

Install Ollama in your machine:

Pull one embedding model and one chat/thinking model:

```bash
docker exec -it ollama ollama pull qwen3-embedding:0.6b
docker exec -it ollama ollama pull qwen3:4b
```

Notes:
- `qwen3-embedding:0.6b` is used for embeddings.
- `qwen3:4b` is a good local chat/thinking-style model for interactive use.
- If you want to keep the current defaults from the code, the repo is already configured to use `qwen3-embedding:0.6b` and `gemma4:e4b`.

### 3. Configure the application

The current application configuration is in:

```properties
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.username=elastic
spring.elasticsearch.password=password

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.model=qwen3-embedding:0.6b
spring.ai.ollama.chat.model=gemma4:e4b
```

If you decide to use the Ollama models above, update the model names in the code or configuration to match your local pull.

### 4. Build and run the app

From the project root:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

The app should start on port 8080 by default.

## API usage

### Upload a PDF

```bash
curl -X POST http://localhost:8080/api/document \
  -F "file=@/path/to/your.pdf"
```

The server will process the document and store its embeddings in Elasticsearch.

### Chat with the uploaded document

```bash
curl -N "http://localhost:8080/api/chat?chatId=1&userQuery=Summarize%20the%20uploaded%20document"
```

The endpoint streams responses as Server-Sent Events (SSE).

## Project structure

```text
src/main/java
  - controllers/        REST endpoints
  - services/           chat and document processing logic
  - parser/             PDF parsing helpers
  - dtos/               request and response models

src/main/resources
  - application.properties   local runtime configuration

chatTester/
  - test.html             simple browser-based test page
```

## Useful development commands

```bash
./mvnw test
./mvnw clean package
```

## Troubleshooting

- If Elasticsearch is not reachable, confirm the container is running and that the credentials match the app configuration.
- If Ollama fails to respond, make sure the model was pulled successfully with `ollama pull`.
- If vector search does not return results, verify that the PDF upload completed successfully and that embeddings were written to Elasticsearch.
- If you change the Ollama model names, update them consistently in the application configuration and service code.

## Notes

This project is a local-first RAG-style chat application. It is designed for experimentation and personal use rather than production deployment.
