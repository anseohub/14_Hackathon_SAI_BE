package com.sai.sai_api.Service;

import com.sai.sai_api.Dto.AnalyzeRequest;
import com.sai.sai_api.Dto.AnalyzeResponse;

public interface AnalysisService {
    AnalyzeResponse analyze(AnalyzeRequest request);
}
