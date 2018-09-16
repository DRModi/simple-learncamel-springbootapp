package com.learncamel.springboot.route;


import com.learncamel.springboot.processor.HealthCheckProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(CamelSpringBootRunner.class)
@ActiveProfiles("mock")
public class HealthCheckRouteMockTest extends CamelTestSupport {


    //in order to specific route to trigger

    @Override
    public RouteBuilder createRouteBuilder(){
        return new HealthCheckRoute();
    }

    @Autowired
    Environment environment;

    @Autowired
    CamelContext camelContext;

    @Autowired
    protected CamelContext createCamelContext(){
        return camelContext;
    }

    @Autowired
    ProducerTemplate producerTemplate;

    //it is required to add in test case which override camletestsupport setup method.
    @Override
    public void setUp(){

    }



    @Test
    public void healthRouteCheckTest(){

        String inputHealthCheckStatus = "{\"status\":\"DOWN\",\"camel\":{\"status\":\"UP\",\"name\":\"camel-1\",\"version\":\"2.20.1\",\"contextStatus\":\"Started\"}," +
                "\"camel-health-checks\":{\"status\":\"UP\",\"route:healthCheckRouteId\":\"UP\",\"route:mainRouteId\":\"UP\"}," +
                "\"mail\":{\"status\":\"UP\",\"location\":\"smtp.gmail.com:587\"}," +
                "\"diskSpace\":{\"status\":\"UP\",\"total\":499963170816,\"free\":372222734336,\"threshold\":10485760}," +
                "\"db\":{\"status\":\"DOWN\",\"error\":" +
                "\"org.springframework.jdbc.CannotGetJdbcConnectionException: Could not get JDBC Connection; " +
                "nested exception is org.postgresql.util.PSQLException: FATAL: database \\\"localdbDeleteMe\\\" does not exist\"}}";

        String expectedOutputErrorMessage = "status  :  component in the route is down!! \n" +
                "db  :  component in the route is down!! \n";

        String response = (String) producerTemplate.requestBodyAndHeader(environment.getProperty("healthCheckRoute"), inputHealthCheckStatus,
                "retrievedEnvProfile", environment.getProperty("spring.profiles.active"));

        System.out.println("**** Response : "+response);

        assertEquals(response,expectedOutputErrorMessage);
    }


}
