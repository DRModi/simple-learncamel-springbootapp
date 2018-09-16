package com.learncamel.springboot.route;


import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@SpringBootTest
@RunWith(CamelSpringBootRunner.class)
@ActiveProfiles("prod")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SimpleAppCamelRouteTest {

    @Autowired
    Environment environment;

    @Autowired
    ProducerTemplate producerTemplate;


    @BeforeClass
    public static void initialCleanUp() throws IOException {

        System.out.println(" ********  CleanUp for Input Directory - START");
        FileUtils.cleanDirectory(new File("data/prod/input"));
        FileUtils.deleteDirectory(new File("data/prod/input/error"));
        System.out.println(" ********  CleanUp for Input Directory - DONE");

        System.out.println(" ********  Delete Output Directory - START");
        FileUtils.deleteDirectory(new File("data/prod/output"));
        System.out.println(" ********  Delete Output Directory - DONE");
    }

    @Test
    public void testMoveFile() throws InterruptedException {

        String fileContent= "id,type,itemCode,itemName,itemPrice,itemQuantity\n" +
                "01,add,111,Apple IPhone,1000,5\n" +
                "02,add,211,Apple IPad,500,7\n" +
                "03,add,311,Apple TV,200,11\n" +
                "04,add,411,Apple AirPod,175,5\n" +
                "05,add,511,Apple iMac,2550,1";

        String fileName = "appleProductPurchase.txt";

        producerTemplate.sendBodyAndHeader
                (environment.getProperty("fromRoute"), fileContent, Exchange.FILE_NAME, fileName);

        Thread.sleep(5000);

        File outputFile = new File("data/prod/output/"+fileName);

        assertTrue(outputFile.exists());
    }

    @Test
    public void testMoveFileDBRecord_INSERT() throws InterruptedException, IOException {

        String fileContent= "id,type,itemCode,itemName,itemPrice,itemQuantity\n" +
                "01,add,111,Apple IPhone,1000,5\n" +
                "02,add,211,Apple IPad,500,7\n" +
                "03,add,311,Apple TV,200,11\n" +
                "04,add,411,Apple AirPod,175,5\n" +
                "05,add,511,Apple iMac,2550,1";

        String fileName = "appleProductPurchase.txt";

        producerTemplate.sendBodyAndHeader
                (environment.getProperty("fromRoute"), fileContent, Exchange.FILE_NAME, fileName);

        Thread.sleep(5000);

        String expectedOutputMessage = "Data update successfully!";
        String actualOutputMessage = new String(Files.readAllBytes(Paths.get("data/prod/output/SuccessMessage.txt")));

        assertEquals(expectedOutputMessage,actualOutputMessage);

    }

    @Test
    public void testMoveFileDBRecord_UPDATE() throws InterruptedException, IOException {

        String fileContent= "id,type,itemCode,itemName,itemPrice,itemQuantity\n" +
                "04,update,411,Apple AirPod,180,5\n" +
                "05,update,511,Apple iMac,2750,1\n"+
                "06,add,611,Apple Homepad,350,1";

        String fileName = "appleProductPurchase.txt";

        producerTemplate.sendBodyAndHeader
                (environment.getProperty("fromRoute"), fileContent, Exchange.FILE_NAME, fileName);

        Thread.sleep(5000);

        String expectedOutputMessage = "Data update successfully!";
        String actualOutputMessage = new String(Files.readAllBytes(Paths.get("data/prod/output/SuccessMessage.txt")));

        assertEquals(expectedOutputMessage,actualOutputMessage);

    }


    @Test
    public void testMoveFileDBRecord_DELETE() throws InterruptedException, IOException {

        String fileContent= "id,type,itemCode,itemName,itemPrice,itemQuantity\n" +
                                "06,delete,611,Apple Homepad,2550,1";

        String fileName = "appleProductPurchase.txt";

        producerTemplate.sendBodyAndHeader
                (environment.getProperty("fromRoute"), fileContent, Exchange.FILE_NAME, fileName);

        Thread.sleep(5000);

        String expectedOutputMessage = "Data update successfully!";
        String actualOutputMessage = new String(Files.readAllBytes(Paths.get("data/prod/output/SuccessMessage.txt")));

        assertEquals(expectedOutputMessage,actualOutputMessage);

    }


    @Test
    public void testMoveFileDBRecord_INSERT_Exception() throws InterruptedException, IOException {

        String fileContent= "id,type,itemCode,itemName,itemPrice,itemQuantity\n" +
                "01,add,,Apple IPhone,1000,5";

        String fileName = "appleProductPurchase.txt";

        producerTemplate.sendBodyAndHeader
                (environment.getProperty("fromRoute"), fileContent, Exchange.FILE_NAME, fileName);

        Thread.sleep(5000);




        String expectedOutputMessage = "Data update successfully!";
        //String actualOutputMessage = new String(Files.readAllBytes(Paths.get("data/prod/output/SuccessMessage.txt")));

        //assertEquals(expectedOutputMessage,actualOutputMessage);

        File outFile = new File("data/prod/output/appleProductPurchase.txt");
        assertTrue(outFile.exists());

        /*File successFile = new File("data/prod/output/SuccessMessage.txt");
        assertFalse(successFile.exists());*/

        File errorDirectory = new File("data/prod/input/error");
        assertTrue(errorDirectory.exists());


    }

}
