package com.pge.mockservices.models.rcdc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Header {

    @JsonProperty("Verb")
    private String verb;

    @JsonProperty("Noun")
    private String noun;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("CorrelationID")
    private String correlationId;
}
