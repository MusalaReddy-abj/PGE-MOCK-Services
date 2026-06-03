package com.pge.mockservices.routes;

import com.pge.mockservices.mappers.RCDCRequestMapper;
import com.pge.mockservices.models.rcdc.RCDCRequestResponse;
import com.pge.mockservices.models.rcdc.request.RCDCRequest;
import com.pge.mockservices.security.JwtTokenProvider;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RCDCRequestRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(RCDCRequestRoute.class);

    @Value("${external-services.rcdc-response.url:http://localhost:9080/api/v1/rcdc/response}")
    private String rcdcResponseUrl;

    private final RCDCRequestMapper mapper;
    private final JwtTokenProvider jwtTokenProvider;

    public RCDCRequestRoute(RCDCRequestMapper mapper, JwtTokenProvider jwtTokenProvider) {
        this.mapper = mapper;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void configure() {
        rest()
            .post("/rcdcrequest")
            .description("Mock RCDC Request — maps payload, calls RCDC response service, returns mapped reply")
            .consumes("application/json")
            .produces("application/json")
            .type(RCDCRequest.class)
            .to("direct:process-rcdc-request");

        from("direct:process-rcdc-request")
            .routeId("route-mock-rcdc-request")

            // Step 1: Map incoming request → response
            .process(exchange -> {
                RCDCRequest request = exchange.getMessage().getBody(RCDCRequest.class);
                String correlationId = request.getHeader() != null ? request.getHeader().getCorrelationID() : "N/A";
                String mrid = request.getPayload() != null
                        && request.getPayload().getRcdSwitchState() != null
                        && request.getPayload().getRcdSwitchState().getEndDeviceAsset() != null
                        ? request.getPayload().getRcdSwitchState().getEndDeviceAsset().getMrid() : "N/A";

                log.info("[RCDC-REQUEST] Received | correlationId={} mrid={} state={}",
                        correlationId, mrid,
                        request.getPayload() != null && request.getPayload().getRcdSwitchState() != null
                                ? request.getPayload().getRcdSwitchState().getState() : "N/A");

                RCDCRequestResponse response = mapper.toResponse(request);
                exchange.setProperty("rcdc.mappedResponse", response);
                exchange.getMessage().setBody(response);
            })

            // Step 2: Forward mapped payload to external service via Kong (JWT auth)
            .marshal().json()
            .removeHeaders("CamelHttp*")
            .process(exchange -> {
                exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
                exchange.getMessage().setHeader(Exchange.HTTP_METHOD, "POST");
                exchange.getMessage().setHeader("Authorization", jwtTokenProvider.bearerToken());
                log.info("[RCDC-REQUEST] Forwarding to external service | url={}", rcdcResponseUrl);
                log.debug("[RCDC-REQUEST] Authorization header set (Bearer JWT)");
            })
            .doTry()
                .to(rcdcResponseUrl + "?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .process(exchange -> {
                    int statusCode = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
                    log.info("[RCDC-REQUEST] External service responded | httpStatus={}", statusCode);
                })
            .doCatch(Exception.class)
                .process(exchange -> {
                    Exception ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    log.error("[RCDC-REQUEST] External service call failed | error={}", ex.getMessage());
                })
            .end()

            // Step 3: Always return the pre-mapped response (ReplyCode always 0)
            .process(exchange -> {
                RCDCRequestResponse response = exchange.getProperty("rcdc.mappedResponse", RCDCRequestResponse.class);
                log.info("[RCDC-REQUEST] Returning mock response | correlationId={} replyCode={}",
                        response.getHeader().getCorrelationId(),
                        response.getReply().getReplyCode());
                exchange.getMessage().setBody(response);
            });
    }
}
