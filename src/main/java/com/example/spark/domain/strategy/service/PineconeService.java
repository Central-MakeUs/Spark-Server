package com.example.spark.domain.strategy.service;
import org.springframework.beans.factory.annotation.Value;
import com.google.protobuf.Struct;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PineconeService {
    private Pinecone pc;
    private Index index;

    @Value("${pinecone.api-key}")
    private String apiKey;

    @Value("${pinecone.index-name}")
    private String indexName;

    @PostConstruct
    private void init() {
        if (apiKey == null || indexName == null) {
            throw new IllegalStateException("🚨 Pinecone API Key 또는 Index 이름이 설정되지 않았습니다.");
        }

        this.pc = new Pinecone.Builder(apiKey).build();
        this.index = pc.getIndexConnection(indexName);
    }

    public List<String> findMostRelevantGuides(List<Float> userEmbedding) {
        try {
            Struct filter = Struct.newBuilder()
                    .putFields("content", com.google.protobuf.Value.newBuilder()
                            .setStructValue(Struct.newBuilder()
                                    .putFields("$exists", com.google.protobuf.Value.newBuilder().setBoolValue(true).build())
                                    .build())
                            .build())
                    .build();

            QueryResponseWithUnsignedIndices queryResponse = index.query(
                    3, userEmbedding, null, null, null, "default",
                    filter, false, true
            );
            //System.out.println("🔍 Pinecone 검색 결과: " + queryResponse.getMatchesList());
            return queryResponse.getMatchesList().stream()
                    .map(match -> match.getMetadata().getFieldsMap().get("content").getStringValue())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("🚨 Pinecone 검색 실패: " + e.getMessage());
            throw new RuntimeException("Pinecone 검색 실패", e);
        }
    }
}
