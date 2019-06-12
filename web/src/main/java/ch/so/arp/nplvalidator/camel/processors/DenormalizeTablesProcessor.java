package ch.so.arp.nplvalidator.camel.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DenormalizeTablesProcessor implements Processor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.dbHost}")
    private String dbHost;

    @Value("${app.dbPort}")
    private String dbPort;

    @Value("${app.dbDatabase}")
    private String dbDatabase;

    @Value("${app.dbUser}")
    private String dbUser;

    @Value("${app.dbPwd}")
    private String dbPwd;

    @Value("classpath:sql/create-tables.sql")
    Resource sqlResource;

    @Override
    public void process(Exchange exchange) throws Exception {
        String dbSchema = (String) exchange.getIn().getHeaders().get("DBSCHEMA");

        System.out.println("DenormalizeTablesProcessor");

        String sql = null;
        InputStream is = sqlResource.getInputStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            sql = reader.lines().collect(Collectors.joining("\n"));
        }
            
        log.info(sql);
        log.info(sql);


        String dbUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbDatabase;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd)) {

        } catch (SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }


    }

}