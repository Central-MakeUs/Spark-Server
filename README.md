<img alt="Spark" src="https://github.com/user-attachments/assets/725353af-5f5a-4a9f-9722-c325ba213f9b" width="440px"><br><br>
<!-- <div align="center"> -->

![SpringBoot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![AWS](https://img.shields.io/badge/Amazon_AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)


# Spark

Spark는 사용자 데이터를 기반으로 SNS 성장 전략을 제안하는 서비스입니다.

## Project Structure

``` 
Spark/
├── src/main/java/com/example/spark/
│   ├── domain/
│   │   ├── meta/           # Instagram (Meta) API
│   │   │   ├── api/        # MetaController, MetaStatisticsController
│   │   │   ├── dto/        # MetaStatsDto, MetaAnalysisResultDto
│   │   │   └── service/    # MetaService, MetaStatisticsService
│   │   ├── youtube/        # YouTube API
│   │   │   ├── api/        # YouTubeController, YouTubeStatisticsController
│   │   │   ├── dto/        # YouTubeCombinedStatsDto, YouTubeAnalysisResultDto
│   │   │   └── service/    # YouTubeService, YouTubeStatisticsService
│   │   ├── strategy/       # AI 전략 제안
│   │   │   ├── api/        # PineconeController
│   │   │   └── service/    # ChatGPTService, PineconeService
│   │   └── flask/          # Flask API (Deprecated)
│   │       └── api/        # FlaskController
│   └── global/
│       ├── config/         # AppConfig, SecurityConfig
│       ├── error/          # CustomException, GlobalExceptionHandler
│       └── response/       # SuccessResponse, ErrorResponse
└── resources/
    └── guides/             # 가이드 문서
```

<!-- 여기에 개발한 기능들 적어주세요!! -->

## Spark Developers
- 서버 개발 담당 박상돈  sky980221@gmail.com
<!-- 추가 섹션 작성해주세요!! -->
