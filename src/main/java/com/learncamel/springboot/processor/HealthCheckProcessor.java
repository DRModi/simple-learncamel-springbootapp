package com.learncamel.springboot.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@Slf4j
public class HealthCheckProcessor implements org.apache.camel.Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        String strHealthCheckStatus = exchange.getIn().getBody(String.class);

        log.info("Received Status Message : " + strHealthCheckStatus);


        //covert/Map status JSON message to object using Jackson databind package
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(strHealthCheckStatus, new TypeReference<Map<String, Object>>() {
        });

        //print the map
        log.info("Map Read is : " + map);

        //Multiple component can be down - so retrieve all of them
        StringBuilder downComponentsStrBuilder = null;

        for (String key : map.keySet()) {
            //get the each key value and check if any component status contain "DOWN"
            if (map.get(key).toString().contains("DOWN")) {

                if (downComponentsStrBuilder == null) {
                    downComponentsStrBuilder = new StringBuilder();
                }

                downComponentsStrBuilder.append(key + "  :  component in the route is down!! \n");
            }
        }

        if(downComponentsStrBuilder!=null){
          log.info("Exception Message is : "+ downComponentsStrBuilder.toString());


          exchange.getIn().setHeader("componentDownFound",true);
          exchange.getIn().setBody(downComponentsStrBuilder.toString());

          //setting up the property to call the mailprocessor for alerts
          exchange.setProperty(Exchange.EXCEPTION_CAUGHT,downComponentsStrBuilder.toString());

        }

    }
}
