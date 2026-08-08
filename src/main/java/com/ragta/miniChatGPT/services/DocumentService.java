package com.ragta.miniChatGPT.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.ragta.miniChatGPT.llmconfig.LLMProviderFactory;
import com.ragta.miniChatGPT.parser.PDFParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.rag.content.retriever.elasticsearch.ElasticsearchContentRetriever;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationKnn;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentService {

    private final EmbeddingModel embeddingModel;
    private final ElasticsearchClient client;
    private final ElasticsearchEmbeddingStore store;

    @Getter
    private final ElasticsearchContentRetriever contentRetriever;

    @Autowired
    public DocumentService(LLMProviderFactory factory) {

        this.client = ElasticsearchClient.of(ec -> ec
                .host("http://localhost:9200")
                .usernameAndPassword("elastic", "password"));

        this.store = ElasticsearchEmbeddingStore.builder()
                .client(client)
                .configuration(ElasticsearchConfigurationKnn.builder().build())
                .indexName("pdf_chunks")
                .build();

        this.embeddingModel = factory.get("ollama").provideEmbeddingModel();

        this.contentRetriever = ElasticsearchContentRetriever.builder()
                .client(client)
                .embeddingModel(embeddingModel)
                .configuration(ElasticsearchConfigurationKnn.builder().build())
                .indexName("pdf_chunks")
                .maxResults(10)
                .minScore(0.0)
                .filter(null)
                .build();
    }

    public boolean processFile(MultipartFile file) {
        try {
            if (file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")) {
                return false;
            }

            List<TextSegment> segments = new PDFParser(file).getSegments();

            for (TextSegment segment : segments) {
                Embedding embed = embeddingModel.embed(segment.text()).content();
                store.add(embed, segment);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return true;
    }
}
