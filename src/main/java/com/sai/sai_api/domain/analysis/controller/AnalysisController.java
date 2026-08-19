package com.sai.sai_api.domain.analysis.controller;

import com.sai.sai_api.domain.analysis.dto.AnalysisRequestDto;
import com.sai.sai_api.domain.analysis.dto.AnalysisResponseDto;
import com.sai.sai_api.domain.analysis.service.AiPipelineService;
import com.sai.sai_api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final AiPipelineService aiPipelineService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<AnalysisResponseDto>> runAnalysis(
            @Valid @RequestBody AnalysisRequestDto request
    ) {
        AnalysisResponseDto result = aiPipelineService.runPipeline(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}