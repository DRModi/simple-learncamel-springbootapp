package com.learncamel.springboot.route;


import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("mock")
@SpringBootTest
@RunWith(CamelSpringBootRunner.class)
@DirtiesContext ( classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SimpleAppCamelRouteMockTest extends CamelTestSupport {


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


    @Test
    public void testFileMoveUsingMock() throws InterruptedException {

        String fileContent= "id,itemCode,itemName,itemPrice,itemQuantity\n" +
                "01,111,Apple IPhone,1000,5\n" +
                "02,211,Apple IPad,500,7\n" +
                "03,311,Apple TV,200,11\n" +
                "04,411,Apple AirPod,175,5\n" +
                "05,511,Apple iMac,2550,1";



        MockEndpoint mockEndpoint = getMockEndpoint(environment.getProperty("toRoute1"));
        mockEndpoint.expectedMessageCount(1);
        mockEndpoint.expectedBodiesReceived(fileContent);

        producerTemplate.sendBodyAndHeader(environment.getProperty("startRoute"), fileContent,
                "envProfileHeaderKeyValue", environment.getProperty("spring.profiles.active"));

        assertMockEndpointsSatisfied();

    }

    @Test
    public void testFileMoveDBRecordUsingMock() throws InterruptedException {

        String fileContent= "id,type,itemCode,itemName,itemPrice,itemQuantity\n" +
                "01,add,021114,Apple IPhone,1000,5" ;

        String dbINSERTSQL = "INSERT INTO ITEMS (SKU, ITEM_DESCRIPTION, PRICE) VALUES ('021114','Apple IPhone',1000.00)";

        String dbSuccessMessage = "Data update successfully!";

        MockEndpoint mockEndpoint = getMockEndpoint(environment.getProperty("toRoute1"));
        mockEndpoint.expectedMessageCount(1);
        mockEndpoint.expectedBodiesReceived(fileContent);

        MockEndpoint mockEndpoint2 = getMockEndpoint(environment.getProperty("tojdbcRoute"));
        mockEndpoint2.expectedMessageCount(1);
        mockEndpoint2.expectedBodiesReceived(dbINSERTSQL);

        MockEndpoint mockEndpoint3 = getMockEndpoint(environment.getProperty("toSuccessRoute"));
        mockEndpoint3.expectedMessageCount(1);
        mockEndpoint3.expectedBodiesReceived(dbSuccessMessage);

        producerTemplate.sendBodyAndHeader(environment.getProperty("startRoute"), fileContent,
                "envProfileHeaderKeyValue", environment.getProperty("spring.profiles.active"));

        assertMockEndpointsSatisfied();

    }
}
