package com.learncamel.springboot.processor;

import com.learncamel.springboot.domain.Item;
import com.learncamel.springboot.exception.DataException;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component
public class BuildSQLProcessor implements org.apache.camel.Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        log.info("Enter into the BuildSQL Processor Class");

        if(exchange.getIn().getBody()!=null)
        {

            log.info("Exchange body is not null");




            StringBuilder sqlQuery = new StringBuilder();
            Item item = exchange.getIn().getBody(Item.class);



            if(ObjectUtils.isEmpty(item.getItemCode())){
                throw new DataException(" @@@@@@@@ SKU# is NULL or EMPTY for "+item.getItemName());
            }


            if("add".equalsIgnoreCase(item.getType())){

                log.info("Add Product");

                sqlQuery.append("INSERT INTO ITEMS (SKU, ITEM_DESCRIPTION, PRICE) VALUES "+"('");
                sqlQuery.append(item.getItemCode()+"','"+item.getItemName()+"',"+item.getItemPrice()+")");

            }else if("update".equalsIgnoreCase(item.getType())){

                log.info("Update Product");

                sqlQuery.append("UPDATE ITEMS SET PRICE=" + item.getItemPrice());
                sqlQuery.append(" where SKU = '"+item.getItemCode()+"'");


            }else if("delete".equalsIgnoreCase(item.getType())){

                log.info("Delete Product");

                sqlQuery.append("DELETE FROM ITEMS where SKU = '"+item.getItemCode()+"'");

            }

            log.info("** Constructed Query is : "+sqlQuery);

            exchange.getIn().setBody(sqlQuery);
        }

    }
}
