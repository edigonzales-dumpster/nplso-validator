package ch.so.arp.nplvalidator.camel.processors;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
public class PublishProcessor implements Processor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${app.dbHostNplso}")
    private String dbHostNplso;
    
    @Value("${app.dbPortNplso}")
    private String dbPortNplso;

    @Value("${app.dbDatabaseNplso}")
    private String dbDatabaseNplso;

    @Value("${app.dbUserNplso}")
    private String dbUserNplso;

    @Value("${app.dbPwdNplso}")
    private String dbPwdNplso;
    
    @Value("${app.models}")
    private String models;

    @Override
    public void process(Exchange exchange) throws Exception {
        File dataFile = exchange.getIn().getBody(File.class);
        String dbSchema = (String) exchange.getIn().getHeaders().get("DBSCHEMA");

        System.out.println(dbSchema);

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("dataStore");
        doc.appendChild(rootElement);
      
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(dbSchema));
        rootElement.appendChild(name);

        Element type = doc.createElement("type");
        type.appendChild(doc.createTextNode("PostGIS"));
        rootElement.appendChild(type);

        Element enabled = doc.createElement("enabled");
        enabled.appendChild(doc.createTextNode("true"));
        rootElement.appendChild(enabled);
        
        Element connectionParameters = doc.createElement("connectionParameters");

        Element port = doc.createElement("port");
        port.appendChild(doc.createTextNode(dbPortNplso));
        connectionParameters.appendChild(port);

        Element user = doc.createElement("user");
        user.appendChild(doc.createTextNode(dbUserNplso));
        connectionParameters.appendChild(user);

        Element passwd = doc.createElement("passwd");
        passwd.appendChild(doc.createTextNode(dbPwdNplso));
        connectionParameters.appendChild(passwd);
        
        Element dbtype = doc.createElement("dbtype");
        dbtype.appendChild(doc.createTextNode("postgis"));
        connectionParameters.appendChild(dbtype);

        Element host = doc.createElement("host");
        host.appendChild(doc.createTextNode(dbHostNplso));
        connectionParameters.appendChild(host);

        Element database = doc.createElement("database");
        database.appendChild(doc.createTextNode(dbDatabaseNplso));
        connectionParameters.appendChild(database);

        Element schema = doc.createElement("schema");
        schema.appendChild(doc.createTextNode(dbSchema));
        connectionParameters.appendChild(schema);

        rootElement.appendChild(connectionParameters);

        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
       
        String xmlFileName = Paths.get(dataFile.getAbsoluteFile().getParent(), "datastore.xml").toFile().getAbsolutePath();
        StreamResult result = new StreamResult(new File(xmlFileName));
        
        // Output to console for testing
        //StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
    }

}
