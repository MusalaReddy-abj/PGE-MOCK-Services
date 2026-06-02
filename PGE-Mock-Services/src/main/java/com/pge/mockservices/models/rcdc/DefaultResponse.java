package com.pge.mockservices.models.rcdc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultResponse {

    @JsonProperty("EndDeviceAsset")
    private EndDeviceAsset endDeviceAsset;
}
