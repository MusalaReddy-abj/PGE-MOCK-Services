package com.pge.mockservices.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mock SOAP listener for RCDSwitchState requests.
 *
 * Accepts the CIM/IEC RequestMessage XML, extracts CorrelationID and mRID,
 * and immediately replies with a SUCCESS acknowledgement (ReplyCode 0).
 *
 * Endpoint: POST http://localhost:7080/api/v1/ws/RCDSwitchState
 */
@Component
public class RCDCSoapRoute extends RouteBuilder {

    @Override
    public void configure() {
        rest()
            .post("/ws/RCDSwitchState")
            .description("Mock SOAP RCDSwitchState listener — returns immediate SUCCESS acknowledgement")
            .bindingMode(RestBindingMode.off)
            .consumes("text/xml")
            .produces("text/xml")
            .to("direct:process-rcdc-soap");

        from("direct:process-rcdc-soap")
            .routeId("route-mock-rcdc-soap")
            // Ensure the body is a String before XPath extraction
            .convertBodyTo(String.class)
            // Extract fields from the incoming XML using namespace-agnostic XPath
            .setProperty("correlationId",
                xpath("//*[local-name()='CorrelationID']/text()", String.class))
            .setProperty("mrid",
                xpath("//*[local-name()='mRID']/text()", String.class))
            .process(exchange -> {
                String correlationId = exchange.getProperty("correlationId", String.class);
                String mrid          = exchange.getProperty("mrid", String.class);

                exchange.getMessage().setBody(buildAcknowledgement(correlationId, mrid));
                exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "text/xml; charset=UTF-8");
                exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            });
    }

    private String buildAcknowledgement(String correlationId, String mrid) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <ResponseMessage xmlns="http://www.iec.ch/TC57/2008/schema/message">
                  <Header>
                    <Verb>Reply</Verb>
                    <Noun>RCDSwitchState</Noun>
                    <Timestamp>%s</Timestamp>
                    <CorrelationID>%s</CorrelationID>
                  </Header>
                  <Reply>
                    <ReplyCode>0</ReplyCode>
                    <Result>SUCCESS</Result>
                  </Reply>
                  <Payload>
                    <EndDeviceAsset>
                      <mRID>%s</mRID>
                    </EndDeviceAsset>
                  </Payload>
                </ResponseMessage>
                """.formatted(Instant.now().toString(), correlationId, mrid);
    }
}
