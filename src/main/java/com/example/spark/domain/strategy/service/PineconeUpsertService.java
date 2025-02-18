package com.example.spark.domain.strategy.service;

import com.google.protobuf.Struct;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

//PineconeUpsertService는 Pinecone 벡터 업서트 & 존재 확인을 담당하는 클래스
@Service
public class PineconeUpsertService {
    private Pinecone pc;
    private Index index;

    @Value("${pinecone.api-key}")
    private String apiKey;

    @Value("${pinecone.index-name}")
    private String indexName;

    @PostConstruct
    public void init() {
        if (apiKey == null || indexName == null || apiKey.isBlank() || indexName.isBlank()) {
            throw new IllegalStateException("🚨 Pinecone 설정이 누락되었습니다.");
        }

        // Pinecone 클라이언트 초기화
        this.pc = new Pinecone.Builder(apiKey).build();
        this.index = pc.getIndexConnection(indexName);

        System.out.println("📢 Pinecone 클라이언트 초기화 완료: " + indexName);
    }

    public boolean isVectorStored(String id) {
        try {
            DescribeIndexStatsResponse indexStatsResponse = index.describeIndexStats(null);
            System.out.println("📌 Pinecone Index Stats: " + indexStatsResponse);
            return indexStatsResponse.getNamespacesMap().containsKey(id);
        } catch (Exception e) {
            System.err.println("🚨 Pinecone 벡터 조회 실패: " + e.getMessage());
            return false;
        }
    }

    public void upsertVector(String id, List<Float> embedding, String content) {
        if (isVectorStored(id)) {
            System.out.println("✅ ID " + id + "는 이미 저장되어 있음. 저장하지 않음.");
            return;
        }

        Struct metadata = Struct.newBuilder()
                .putFields("content", com.google.protobuf.Value.newBuilder().setStringValue(content).build())
                .build();


        index.upsert(id, embedding, null, null, metadata, "default");

        System.out.println("✅ ID " + id + " 저장 완료.");
    }
}
