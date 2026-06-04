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

import java.time.Instant;

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
            .description("Mock RCDC Request — maps payload, calls RCDC response service, returns CM-ChangeRCDSwitchStateResp XML")
            .consumes("application/json")
            .produces("text/xml")
            .type(RCDCRequest.class)
            .to("direct:process-rcdc-request");

        from("direct:process-rcdc-request")
            .routeId("route-mock-rcdc-request")

            // Step 1: Extract fields, build JSON payload for external service call
            .process(exchange -> {
                RCDCRequest request = exchange.getMessage().getBody(RCDCRequest.class);
                String correlationId = request.getHeader() != null ? request.getHeader().getCorrelationID() : "";
                String mrid = request.getPayload() != null
                        && request.getPayload().getRcdSwitchState() != null
                        && request.getPayload().getRcdSwitchState().getEndDeviceAsset() != null
                        ? request.getPayload().getRcdSwitchState().getEndDeviceAsset().getMrid() : "";

                log.info("[RCDC-REQUEST] Received | correlationId={} mrid={} state={}",
                        correlationId, mrid,
                        request.getPayload() != null && request.getPayload().getRcdSwitchState() != null
                                ? request.getPayload().getRcdSwitchState().getState() : "N/A");

                exchange.setProperty("rcdc.correlationId", correlationId);

                RCDCRequestResponse outboundPayload = mapper.toResponse(request);
                exchange.getMessage().setBody(outboundPayload);
            })

            // Step 2: Forward mapped JSON payload to external service via Kong (JWT auth)
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

            // Step 3: Always return CM-ChangeRCDSwitchStateResp XML (replyCode always 0.0)
            .process(exchange -> {
                String correlationId = exchange.getProperty("rcdc.correlationId", String.class);
                String xml = buildXmlResponse(correlationId);

                log.info("[RCDC-REQUEST] Returning XML response | correlationId={} replyCode=0.0", correlationId);

                exchange.getMessage().setBody(xml);
                exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "text/xml; charset=UTF-8");
            });
    }

    private String buildXmlResponse(String correlationId) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <CM-ChangeRCDSwitchStateResp\
                 xmlns="http://ouaf.oracle.com/webservices/cm/CM-ChangeRCDSwitchStateResp"\
                 xmlns:jca="http://xmlns.oracle.com/pcbpel/wsdl/jca/"\
                 xmlns:ns2="http://iec.ch/TC57/2009/EndDeviceAssets#"\
                 xmlns:ns1="http://xmlns.oracle.com/pcbpel/adapter/jms/TrilliantSOAApplication/TrilliantRCDCAsyncRespFromHESToMDM/ConsumeResponseFromTrilliant"\
                 xmlns:ns4="http://www.trilliantinc.com/SEAL/1.0/BasicTypes"\
                 xmlns:ns3="http://www.trilliantinc.com/SEAL/1.0/dt025pvvnl"\
                 xmlns:tns="http://ouaf.oracle.com/webservices/cm/CM-ChangeRCDSwitchStateResp">
                      <tns:transactionId>%s</tns:transactionId>
                      <tns:responseDetail>
                        <tns:header>
                          <tns:verb>REPLY</tns:verb>
                          <tns:noun>DefaultResponse</tns:noun>
                          <tns:correlationID>%s</tns:correlationID>
                          <tns:timeStamp>%s</tns:timeStamp>
                        </tns:header>
                        <tns:reply>
                          <tns:replyCode>0.0</tns:replyCode>
                        </tns:reply>
                        <tns:payLoad/>
                      </tns:responseDetail>
                    </CM-ChangeRCDSwitchStateResp>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(correlationId, correlationId, Instant.now().toString());
    }
}
