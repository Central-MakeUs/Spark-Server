package com.example.spark.domain.meta.api;

import com.example.spark.domain.meta.dto.MetaProfileDto;
import com.example.spark.domain.meta.dto.MetaContentDto;
import com.example.spark.domain.meta.service.MetaService;
import com.example.spark.global.error.CustomException;
import com.example.spark.global.error.ErrorCode;
import com.example.spark.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Meta API", description = "Meta(Instagram) 데이터를 관리하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/meta")
public class MetaController {

    private final MetaService metaService;

    @Operation(
            summary = "채널 정보 조회",
            description = """
                    연동된 채널의 프로필을 조회합니다.
                                          
                    **요청값**
                    - `accessToken`: Meta API 인증에 필요한 액세스 토큰
                    - `instagram_business_account_id`: Instagram 비즈니스 계정 ID  
                                        
                    **응답값**
                    - 유저명
                    - 프로필 URL
                    - 팔로워 수 
                    - 팔로잉 수 
                    - 게시글 수
                    """
    )
    @GetMapping("/account-profile")
    public SuccessResponse<MetaProfileDto> getAccountProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        // "Bearer " 제거 후 액세스 토큰만 추출
        String accessToken = authorizationHeader.substring(7);

        try {
            MetaProfileDto profile = metaService.getAccountProfile(accessToken);
            return SuccessResponse.success(profile);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        }
    }

    @Operation(
            summary = "조회수 상위 컨텐츠 조회",
            description = """
                    내 인스타그램에서 조회수 기준 상위 3개 컨텐츠를 가져옵니다.
                                              
                    **요청값**
                    - `accessToken`: Meta API에 접근하기 위한 액세스 토큰
                    - `instagramBusinessAccountId`: 조회할 Instagram 비즈니스 계정 ID
                                              
                    **응답값**
                    - 상위 3개 컨텐츠의 정보 (ID, 캡션, 게시일, 미디어 타입, 컨텐츠 URL, 조회수)
                    """
    )
    /**
     * 조회수 상위 Instagram 컨텐츠 조회
     */
    @GetMapping("/top-contents")
    public SuccessResponse<List<MetaContentDto>> getTopContents(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String instagramBusinessAccountId) {

        String accessToken = authorizationHeader.substring(7);

        try {
            List<MetaContentDto> topContents = metaService.getTopContents(accessToken, instagramBusinessAccountId);
            return SuccessResponse.success(topContents);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        }
    }

}
