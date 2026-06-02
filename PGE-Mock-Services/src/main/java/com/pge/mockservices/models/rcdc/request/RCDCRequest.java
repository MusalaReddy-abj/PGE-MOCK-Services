package com.pge.mockservices.models.rcdc.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RCDCRequest {

    private RequestHeader header;
    private RequestPayload payload;
}
