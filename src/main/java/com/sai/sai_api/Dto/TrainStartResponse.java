package com.sai.sai_api.Dto;

import java.util.List;

public record TrainStartResponse(Long sessionId, String context, List<MessageDto> messages) {

}
