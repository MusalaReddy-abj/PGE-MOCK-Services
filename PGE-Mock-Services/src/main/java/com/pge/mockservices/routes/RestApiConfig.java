package com.pge.mockservices.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RestApiConfig extends RouteBuilder {

    @Value("${rest.host:0.0.0.0}")
    private String restHost;

    @Value("${rest.port:8080}")
    private int restPort;

    @Override
    public void configure() {
        restConfiguration()
            .component("undertow")
            .host(restHost)
            .port(restPort)
            .bindingMode(RestBindingMode.json)
            .dataFormatProperty("prettyPrint", "true")
            .enableCORS(true)
            .corsHeaderProperty("Access-Control-Allow-Origin", "*")
            .corsHeaderProperty("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS")
            .corsHeaderProperty("Access-Control-Allow-Headers", "Content-Type,Authorization")
            .contextPath("/api/v1")
            .apiContextPath("/api-doc")
            .apiProperty("api.title", "PGE Mock Services API")
            .apiProperty("api.version", "1.0.0");
    }
}
