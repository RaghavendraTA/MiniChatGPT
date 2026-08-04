package com.ragta.miniChatGPT.parser;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class PDFParser implements IDocumentParser {

    private final MultipartFile file;
    private final DocumentParser parser = new ApachePdfBoxDocumentParser();

    public PDFParser(MultipartFile file) {
        this.file = file;
    }

    @Override
    public List<String> getChunks() {
        List<TextSegment> segments = getSegments();

        return segments.stream()
                .map(TextSegment::text)
                .toList();
    }

    @Override
    public List<TextSegment> getSegments() {
        try {
            Document document = parser.parse(file.getInputStream());
            int overlap = (int) (chunkSize * 0.20);

            DocumentSplitter splitter = new DocumentByParagraphSplitter(chunkSize, overlap);
            return splitter.split(document);

        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return List.of();
    }
}
