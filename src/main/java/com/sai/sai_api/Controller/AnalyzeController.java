package com.sai.sai_api.Controller;

import com.sai.sai_api.Dto.AnalyzeRequest;
import com.sai.sai_api.Dto.AnalyzeResponse;
import com.sai.sai_api.Service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyzeController {
    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    public AnalyzeResponse analyze(@RequestBody AnalyzeRequest request) {
        return analysisService.analyze(request);
    }

}
