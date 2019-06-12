package ch.so.arp.nplvalidator.camel.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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

    @Override
    public void process(Exchange exchange) throws Exception {
        String dbSchema = (String) exchange.getIn().getHeaders().get("DBSCHEMA");

        System.out.println("DenormalizeTablesProcessor");


    }

}