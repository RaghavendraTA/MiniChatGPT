package com.ragta.miniChatGPT.parser;

import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

public interface IDocumentParser {

    static int chunkSize = 400;

    List<String> getChunks();

    List<TextSegment> getSegments();
}
