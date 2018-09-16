package com.learncamel.springboot.route;

import com.learncamel.springboot.processor.HealthCheckProcessor;
import com.learncamel.springboot.processor.MailProcessor;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.AsEndpointUri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckRoute extends RouteBuilder {

    @Autowired
    MailProcessor mailProcessor;

    @Autowired
    HealthCheckProcessor healthCheckProcessor;

    //it is like condition check, will use to validate the mock profile to validate the test
    Predicate isNotMockProfile = header("retrievedEnvProfile").isNotEqualTo("mock");


    @Override
    public void configure() throws Exception {

        from("{{healthCheckRoute}}").routeId("healthCheckRouteId")
                .choice()
                    .when(isNotMockProfile)
                        .pollEnrich("http://localhost:8080/health")
                .end()
                //.process(new HealthCheckProcessor())
                .process(healthCheckProcessor)
                .choice()
                    .when((header("componentDownFound").isEqualTo("true")))
                    .choice()
                        .when(isNotMockProfile)
                            .process(mailProcessor)
                    .end()
                .end();
    }
}
