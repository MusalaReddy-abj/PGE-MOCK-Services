package com.pge.mockservices.routes;

import com.pge.mockservices.mappers.RCDCRequestMapper;
import com.pge.mockservices.models.rcdc.RCDCRequestResponse;
import com.pge.mockservices.models.rcdc.request.RCDCRequest;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RCDCRequestRoute extends RouteBuilder {

    @Value("${external-services.rcdc-response.url:http://localhost:9080/api/v1/rcdc/response}")
    private String rcdcResponseUrl;

    private final RCDCRequestMapper mapper;

    public RCDCRequestRoute(RCDCRequestMapper mapper) {
        this.mapper = mapper;
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
            // Step 1: Map incoming request → response; save for final return
            .process(exchange -> {
                RCDCRequest request = exchange.getMessage().getBody(RCDCRequest.class);
                RCDCRequestResponse response = mapper.toResponse(request);
                exchange.setProperty("rcdc.mappedResponse", response);
                exchange.getMessage().setBody(response);
            })
            // Step 2: Forward mapped payload to external service
            .marshal().json()
            .removeHeaders("CamelHttp*")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .doTry()
                .to(rcdcResponseUrl + "?bridgeEndpoint=true&throwExceptionOnFailure=false")
            .doCatch(Exception.class)
                .log("External RCDC response service unavailable: ${exception.message}")
            .end()
            // Step 3: Always return the pre-mapped response (ReplyCode always 0)
            .process(exchange -> exchange.getMessage().setBody(exchange.getProperty("rcdc.mappedResponse")));
    }
}
