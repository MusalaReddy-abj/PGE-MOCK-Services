package com.pge.mockservices.mappers;

import com.pge.mockservices.models.rcdc.DefaultResponse;
import com.pge.mockservices.models.rcdc.EndDeviceAsset;
import com.pge.mockservices.models.rcdc.Header;
import com.pge.mockservices.models.rcdc.Payload;
import com.pge.mockservices.models.rcdc.RCDCRequestResponse;
import com.pge.mockservices.models.rcdc.Reply;
import com.pge.mockservices.models.rcdc.request.RCDCRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RCDCRequestMapper {

    public RCDCRequestResponse toResponse(RCDCRequest request) {
        String correlationId = Optional.ofNullable(request.getHeader())
                .map(h -> h.getCorrelationID())
                .orElse("");

        String timestamp = Optional.ofNullable(request.getHeader())
                .map(h -> h.getTimestamp())
                .orElse("");

        String mrid = Optional.ofNullable(request.getPayload())
                .map(p -> p.getRcdSwitchState())
                .map(s -> s.getEndDeviceAsset())
                .map(e -> e.getMrid())
                .orElse("");

        return new RCDCRequestResponse(
                new Header("Reply", "RCDSwitchState", timestamp, correlationId),
                new Reply("0.0"),
                new Payload(new DefaultResponse(new EndDeviceAsset(mrid)))
        );
    }
}
