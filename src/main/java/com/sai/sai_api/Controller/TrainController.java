package com.sai.sai_api.Controller;

import com.sai.sai_api.Dto.TrainSendRequest;
import com.sai.sai_api.Dto.TrainSendResponse;
import com.sai.sai_api.Dto.TrainStartRequest;
import com.sai.sai_api.Dto.TrainStartResponse;
import com.sai.sai_api.Service.TrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/train")
@RequiredArgsConstructor
public class TrainController {
    private final TrainService trainService;

    @PostMapping("/sessions")
    public TrainStartResponse start(@RequestBody TrainStartRequest request) {
        return trainService.start(request);
    }

    @PostMapping("/sessions/{id}/messages")
    public TrainSendResponse send(@PathVariable Long id, @RequestBody TrainSendRequest request) {
        return trainService.send(id, request);
    }
}
