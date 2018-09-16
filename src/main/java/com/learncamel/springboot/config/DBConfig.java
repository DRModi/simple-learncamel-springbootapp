package com.learncamel.springboot.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.beans.ConstructorProperties;

@Configuration
public class DBConfig {

    @Bean(name = "myDataSource")
    @ConfigurationProperties(prefix = "spring.datasourceConnInfo")
    public DataSource dataSource(){

        DataSource dataSource = DataSourceBuilder.create().build();

        return dataSource;
    }

}
