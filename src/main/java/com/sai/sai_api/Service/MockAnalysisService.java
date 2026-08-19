package com.sai.sai_api.Service;

import com.sai.sai_api.Dto.AnalyzeRequest;
import com.sai.sai_api.Dto.AnalyzeResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
@Profile("mock")
@Service
public class MockAnalysisService implements  AnalysisService {

    @Override
    public AnalyzeResponse analyze(AnalyzeRequest req) {
        return new AnalyzeResponse(
                req.situation(), req.sender(), req.receiver(),
                "이 가격 조건은 ", "Regarding this price, ",
                List.of(new AnalyzeResponse.Risk(
                        "더 이상 양보하기 어렵습니다.",
                        "we cannot make any further concessions.",
                        "일방적 통보로 읽힐 수 있음",
                        "협상 여지를 완전히 닫는 표현으로 번역되어, 상대가 불안을 느낄 수 있습니다.",
                        new AnalyzeResponse.Fix(
                                "저희 쪽에서 조정 가능한 범위를 먼저 공유드려도 될까요?",
                                "could we first share what flexibility we do have on our side?")
                ))
        );

    }
}
