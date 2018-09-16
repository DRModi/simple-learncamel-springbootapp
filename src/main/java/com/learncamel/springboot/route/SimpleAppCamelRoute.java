package com.learncamel.springboot.route;

import com.learncamel.springboot.domain.Item;
import com.learncamel.springboot.exception.DataException;
import com.learncamel.springboot.processor.BuildSQLProcessor;
import com.learncamel.springboot.processor.MailProcessor;
import com.learncamel.springboot.processor.SuccessProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.apache.camel.spi.DataFormat;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Slf4j
public class SimpleAppCamelRoute extends RouteBuilder {


    @Autowired
    Environment environment;

    @Qualifier("myDataSource")
    @Autowired
    DataSource myDataSource;

    @Autowired
    BuildSQLProcessor buildSQLProcessor;

    @Autowired
    SuccessProcessor successProcessor;

    @Autowired
    MailProcessor mailProcessor;


    @Override
    public void configure() throws Exception {

        //no need to create LoggerFactory b'coz we are using @Sl4j lombok (see additional info)
        log.info("Before Route Starting - CAMEL ROUTE");

        /*from("timer:filePollTimer?period=10s")
                .log("File Poll Timer Invoked : Body is ${body}")
                .pollEnrich("file:data/input?delete=true&readLock=none")
                .to("file:data/output");*/


        /*from("{{startRoute}}")
                .log("File Poll Timer Invoked : Environment is " + environment.getProperty("message"))
                .pollEnrich("{{fromRoute}}")
                .to("{{toRoute1}}");*/





        //Create BindyFormat
        DataFormat bindyCsvDataFormat = new BindyCsvDataFormat(Item.class);

        //DeadLetter Channel Used - Define the URI of JMS/Active-MQ or Kafka In order to persist the error.
        // Flow continues in case of redeliveryDelay, maximumRedeliveries and BackOfMultiplier are not present.

        errorHandler(deadLetterChannel("log:errorInRoute?level=ERROR&showProperties=true")
                                            .maximumRedeliveries(4)
                                            .redeliveryDelay(1000)
                                            .backOffMultiplier(2)
                                            .retryAttemptedLogLevel(LoggingLevel.ERROR));


        //this will be raised in DB connectivity issue - so retry make sense
        onException(PSQLException.class).log(LoggingLevel.ERROR, "PSQL Exception in the route and Body is ${body}")
                .maximumRedeliveries(3).redeliveryDelay(3000).backOffMultiplier(2).retryAttemptedLogLevel(LoggingLevel.ERROR);


        //Basically no need of retry since it is data issue and in this case it is going to be same exception in all retry.
        onException(DataException.class).log(LoggingLevel.ERROR, "DataException in Route and Body is ${body}")
                .process(mailProcessor);


        //Using predicates and content based routing - achieving the mock testing
        from("{{startRoute}}").routeId("mainRouteId")
                .log("File Poll Timer Invoked : Environment is " + environment.getProperty("message"))
                .choice()
                    .when((header("envProfileHeaderKeyValue").isNotEqualTo("mock")))
                        .pollEnrich("{{fromRoute}}")
                    .otherwise()
                        .log("Mock environment flow activated and body is ${body}")
                .end() //if you don't end() then in case of no mock - it wont find the header env
                           // key value pair and never called the .to() below route
                .to("{{toRoute1}}")
                .unmarshal(bindyCsvDataFormat)
                .log("***** Unmarshaled Object list is ${body}")
                .split(body())
                    .log("##### Individual Record is ${body}")
                    .process(buildSQLProcessor) //.process(new BuildSQLProcessor())
                    .to("{{tojdbcRoute}}")//.to("jdbc:myDataSource")
                .end()
                .process(successProcessor)
                .to("{{toSuccessRoute}}");




        log.info("After Route Completing - CAMEL ROUTE");
    }
}
