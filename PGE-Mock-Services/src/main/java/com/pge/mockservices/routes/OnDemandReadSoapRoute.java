package com.pge.mockservices.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock SOAP service for OnDemandRead (CM-GetMeterReadings) requests.
 *
 * Extracts correlationID and mRID from the incoming CIM request XML,
 * returns a hardcoded 44-reading response with those two fields mapped.
 *
 * Endpoint: POST http://localhost:7080/api/v1/ws/OnDemandRead
 */
@Component
public class OnDemandReadSoapRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(OnDemandReadSoapRoute.class);

    @Override
    public void configure() {
        rest()
            .post("/ws/OnDemandRead")
            .description("Mock SOAP OnDemandRead (CM-GetMeterReadings) — maps correlationID and mRID, returns 44 hardcoded readings")
            .bindingMode(RestBindingMode.off)
            .consumes("text/xml")
            .produces("text/xml")
            .to("direct:process-on-demand-read-soap");

        from("direct:process-on-demand-read-soap")
            .routeId("route-mock-on-demand-read-soap")
            .convertBodyTo(String.class)
            .process(exchange -> log.info("[ON-DEMAND-READ] Received request | contentLength={}",
                    exchange.getMessage().getBody(String.class).length()))

            // Extract correlationID and mRID using namespace-agnostic XPath
            .setProperty("correlationId",
                    xpath("//*[local-name()='correlationID']/text()", String.class))
            .setProperty("mrid",
                    xpath("//*[local-name()='mRID']/text()", String.class))

            .process(exchange -> {
                String correlationId = exchange.getProperty("correlationId", String.class);
                String mrid          = exchange.getProperty("mrid", String.class);

                log.info("[ON-DEMAND-READ] Parsed request | correlationId={} mrid={}", correlationId, mrid);

                exchange.getMessage().setBody(buildResponse(correlationId, mrid));
                exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "text/xml; charset=UTF-8");
                exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);

                log.info("[ON-DEMAND-READ] Returning mock response | correlationId={} mrid={} readings=44",
                        correlationId, mrid);
            });
    }

    private String buildResponse(String correlationId, String mrid) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <CM-GetMeterReadingsResp xmlns="http://ouaf.oracle.com/webservices/cm/CM-GetMeterReadingsResp"\
                 xmlns:jca="http://xmlns.oracle.com/pcbpel/wsdl/jca/"\
                 xmlns:ns2="http://iec.ch/TC57/2009/MeterReadings#"\
                 xmlns:ns1="http://xmlns.oracle.com/pcbpel/adapter/jms/Trilliant-DEV/OnDemandReadCallback/jmsService"\
                 xmlns:ns3="http://iec.ch/TC57/2011/EndDeviceEvents#"\
                 xmlns:tns="http://ouaf.oracle.com/webservices/cm/CM-GetMeterReadingsResp">
                  <tns:responseDetail>
                    <tns:header>
                      <tns:verb>REPLY</tns:verb>
                      <tns:noun>MeterReadings</tns:noun>
                      <tns:correlationID>%s</tns:correlationID>
                      <tns:timeStamp>2026-05-08T11:22:42.364Z</tns:timeStamp>
                    </tns:header>
                    <tns:payload>
                      <tns:meterReadings>
                        <tns:meterReading>
                          <tns:readings>
                            <tns:sequence>1</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.7.0.0.0.0.61.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>2</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.2.0.0.0.0.61.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>3</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>500.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>4</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.6.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>5</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.8.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>6</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>240.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.2.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>7</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.0.0.0.0.0.38.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>8</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>10.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.3.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>9</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.7.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>10</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.4.0.0.0.0.38.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>11</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.5.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>12</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.5.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>13</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>1.0</tns:value>
                            <tns:readingType>13.2.0.0.0.1.38.0.0.0.0.0.0.0.0.0.65.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>14</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.3.0.0.0.0.61.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>15</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.6.0.0.0.0.61.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>16</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>10368.0</tns:value>
                            <tns:readingType>13.0.0.0.0.1.11.0.0.0.0.0.0.0.0.0.159.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>17</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.5.0.0.0.0.38.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>18</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>19</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>910.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.4.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>20</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>1450.0</tns:value>
                            <tns:readingType>13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>21</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.1.0.0.0.0.38.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>22</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>390.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.1.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>23</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>570.0</tns:value>
                            <tns:readingType>13.0.0.1.15.1.12.0.0.0.0.0.0.0.0.3.73.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>24</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.6.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>25</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.7.0.0.0.0.38.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>26</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>27</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>1030.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.4.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>28</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.5.0.0.0.0.61.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>29</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.6.0.0.0.0.38.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>30</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.2.0.0.0.0.38.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>31</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.3.0.0.0.0.38.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>32</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.8.0.0.0.0.38.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>33</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>1810.0</tns:value>
                            <tns:readingType>13.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>34</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.1.0.0.0.0.61.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>35</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.8.0.0.0.0.61.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>36</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.1.18.1.12.0.0.0.0.0.0.0.0.3.73.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>37</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.7.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>38</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>40.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.3.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>39</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.4.0.0.0.0.61.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>40</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.8.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>41</tns:sequence>
                            <tns:timestamp>1970-01-01T05:30:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.8.0.6.1.1.8.0.0.0.0.0.0.0.0.0.61.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>42</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>140.0</tns:value>
                            <tns:readingType>13.0.0.9.1.1.12.0.0.0.0.2.0.0.0.3.72.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>43</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.71.0</tns:readingType>
                          </tns:readings>
                          <tns:readings>
                            <tns:sequence>44</tns:sequence>
                            <tns:timestamp>2026-05-08T16:49:00+05:30</tns:timestamp>
                            <tns:value>0.0</tns:value>
                            <tns:readingType>13.0.0.1.17.1.12.0.0.0.0.0.0.0.0.3.73.0</tns:readingType>
                          </tns:readings>
                          <tns:meterAsset>
                            <tns:mRID>%s</tns:mRID>
                          </tns:meterAsset>
                        </tns:meterReading>
                      </tns:meterReadings>
                    </tns:payload>
                    <tns:reply>
                      <tns:replyCode>0.0</tns:replyCode>
                    </tns:reply>
                  </tns:responseDetail>
                </CM-GetMeterReadingsResp>
                """.formatted(correlationId, mrid);
    }
}
