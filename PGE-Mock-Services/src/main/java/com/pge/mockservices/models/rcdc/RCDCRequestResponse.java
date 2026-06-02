package com.pge.mockservices.models.rcdc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RCDCRequestResponse {

    @JsonProperty("Header")
    private Header header;

    @JsonProperty("Reply")
    private Reply reply;

    @JsonProperty("Payload")
    private Payload payload;
}
