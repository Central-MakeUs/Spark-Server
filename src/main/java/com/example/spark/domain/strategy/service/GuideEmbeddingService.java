package com.example.spark.domain.strategy.service;

import com.example.spark.global.util.TxtFileReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

//TXT 파일을 Pinecone에 저장하는 역할을 하는 클래스
@Service
public class GuideEmbeddingService {
    private final OpenAIEmbeddingService openAIEmbeddingService;
    private final PineconeUpsertService pineconeUpsertService;
    private final ResourcePatternResolver resourcePatternResolver;

    public GuideEmbeddingService(OpenAIEmbeddingService openAIEmbeddingService, PineconeUpsertService pineconeUpsertService, ResourcePatternResolver resourcePatternResolver) {
        this.openAIEmbeddingService = openAIEmbeddingService;
        this.pineconeUpsertService = pineconeUpsertService;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public void storeTxtGuidesInPinecone(String namespace, String resourceDir) throws IOException {
        // 📌 지정된 디렉터리의 모든 `.txt` 파일을 가져옴
        System.out.println("🚀 Pinecone 업로드 시작 | namespace=" + namespace + ", dir=" + resourceDir);
        Resource[] resources = resourcePatternResolver.getResources("classpath:" + resourceDir + "/*.txt");
        System.out.println("🔎 파일 검색 완료 | count=" + resources.length);

        if (resources.length == 0) {
            System.out.println("⚠️ 업로드할 TXT 파일이 없습니다. 디렉터리를 확인하세요: classpath:" + resourceDir);
        }

        int processedFiles = 0;
        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName == null) continue;

            String guideId = fileName.replace(".txt", ""); // 파일명을 ID로 사용
            System.out.println("📌 파일 처리 시작 | guideId=" + guideId);

            // ✅ TXT 파일 읽기
            String fullText = TxtFileReader.readTxtFile(resourceDir + "/" + fileName);

// ✅ 두 줄 띄우기 (`\n\n`) 기준으로 나누기
            List<String> chunks = List.of(fullText.split("\n\n"));

// ✅ 각 Chunk에 대해 Embedding 생성 및 Pinecone 저장
            System.out.println("✂️ 청크 분할 | 총 청크 수=" + chunks.size());
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i).trim(); // 공백 제거
                if (chunk.isEmpty()) continue; // 빈 문단은 스킵

                String chunkId = guideId + "_part" + i; // 고유 ID 생성

                // ✅ Embedding 생성
                List<Float> embedding = openAIEmbeddingService.getEmbedding(chunk);

                // ✅ Pinecone에 저장
                pineconeUpsertService.upsertVector(chunkId, embedding, chunk, namespace);

                System.out.println("✅ 업서트 완료 | chunkId=" + chunkId);
            }
            processedFiles++;
        }
        System.out.println("🎉 업로드 완료 | 처리한 파일 수=" + processedFiles + ", namespace=" + namespace);
    }

    public void storeTxtGuidesInPinecone(String namespace) throws IOException {
        storeTxtGuidesInPinecone(namespace, "guides");
    }
}
