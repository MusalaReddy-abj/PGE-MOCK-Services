package com.pge.mockservices.models.rcdc.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestHeader {

    private String verb;
    private String noun;
    private String timestamp;
    private String source;
    private String replyAddress;
    private String correlationID;
}
