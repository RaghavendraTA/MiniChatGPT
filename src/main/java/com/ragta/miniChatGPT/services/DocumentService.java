package com.ragta.miniChatGPT.services;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.ragta.miniChatGPT.llmconfig.EmbeddingProviderFactory;
import com.ragta.miniChatGPT.parser.PDFParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.elasticsearch.ElasticsearchContentRetriever;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationKnn;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public DocumentService(EmbeddingProviderFactory factory, @Value("${llm.embedding.providers:ollama}") String llmEmbeddingProviders) {

        this.client = ElasticsearchClient.of(ec -> ec
                .host("http://localhost:9200")
                .usernameAndPassword("elastic", "password"));

        this.store = ElasticsearchEmbeddingStore.builder()
                .client(client)
                .configuration(ElasticsearchConfigurationKnn.builder().build())
                .indexName("pdf_chunks")
                .build();

        // `llm.embedding.providers` can be a comma-separated list; pick the first available provider
        String providerKey = "ollama";
        try {
            if (llmEmbeddingProviders != null && !llmEmbeddingProviders.isBlank()) {
                providerKey = llmEmbeddingProviders.split(",")[0].trim().toLowerCase();
            }
        } catch (Exception ignored) {
        }

        var provider = factory.get(providerKey);
        if (provider == null) {
            provider = factory.get("ollama");
        }
        this.embeddingModel = provider.provideEmbeddingModel();

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
