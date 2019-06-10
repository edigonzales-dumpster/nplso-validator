package ch.so.arp.nplvalidator.camel;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.so.arp.nplvalidator.camel.processors.Ili2pgImportProcessor;
import ch.so.arp.nplvalidator.camel.processors.PublishProcessor;

@Component
public class CamelRoute extends RouteBuilder {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Ili2pgImportProcessor ili2pgImportProcessor;

    @Autowired
    PublishProcessor publishProcessor;

    @Override
    public void configure() throws Exception {
        from("direct:nplValidator")
        //.process(ili2pgImportProcessor)
        // TODO: create denormalized tables etc. processor 
        
        .process(publishProcessor)
        .log(LoggingLevel.INFO, "Hallo Welt.")
        .to("file:///Users/stefan/tmp/");

    }

}
