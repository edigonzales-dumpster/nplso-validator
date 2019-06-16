package ch.so.arp.nplvalidator.camel.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.so.arp.nplvalidator.geoserver.HTTPUtils;

@Component
public class PublishProcessor implements Processor {
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

    @Value("${app.models}")
    private String models;

    @Value("${app.gsHost}")
    private String gsHost;

    @Value("${app.gsPort}")
    private String gsPort;

    @Value("${app.gsWorkspace}")
    private String gsWorkspace;

    @Value("${app.gsUser}")
    private String gsUser;

    @Value("${app.gsPwd}")
    private String gsPwd;

    @Value("${app.tableNameGrundnutzung}")
    private String tableNameGrundnutzung;

    @Override
    public void process(Exchange exchange) throws Exception {
        File dataFile = exchange.getIn().getBody(File.class);
        String dbSchema = (String) exchange.getIn().getHeaders().get("DBSCHEMA");

        // Create namespace.
        String namespaceFileName = Paths.get(dataFile.getAbsoluteFile().getParent(), "namespace.xml").toFile()
                .getAbsolutePath();
        File namespaceFile = this.writeNamespaceXml(namespaceFileName, dbSchema);
        String namespaceFileContent = new String(Files.readAllBytes(Paths.get(namespaceFile.getAbsolutePath())));
        String namespaceResponse = HTTPUtils.postXml("http://" + gsHost + ":" + gsPort + "/geoserver/rest/namespaces", namespaceFileContent, gsUser, gsPwd);
        log.info(namespaceResponse);
        if (namespaceResponse == null) {
            throw new Exception("Error creating namespace.");
        } 

        // Create datastore.
        String dataStoreFileName = Paths.get(dataFile.getAbsoluteFile().getParent(), "datastore.xml").toFile()
                .getAbsolutePath();
        File dataStoreFile = this.writeDataStoreXml(dataStoreFileName, dbSchema);
        String dataStoreFileContent = new String(Files.readAllBytes(Paths.get(dataStoreFile.getAbsolutePath())));
        String dataStoreResponse = HTTPUtils.postXml("http://" + gsHost + ":" + gsPort + "/geoserver/rest/workspaces/"+dbSchema+"/datastores", dataStoreFileContent, gsUser, gsPwd);
        log.info(dataStoreResponse);
        if (dataStoreResponse == null) {
            throw new Exception("Error creating datastore.");
        } 

        // Create featuretypes.
        String featureTypeFileName = Paths.get(dataFile.getAbsoluteFile().getParent(), "featuretype.xml").toFile()
                .getAbsolutePath();
        File featureTypeFile = this.writeFeatureTypeXml(featureTypeFileName, tableNameGrundnutzung, tableNameGrundnutzung);
        String featureTypeFileContent = new String(Files.readAllBytes(Paths.get(featureTypeFile.getAbsolutePath())));
        String featureTypeResponse = HTTPUtils.postXml("http://" + gsHost + ":" + gsPort + "/geoserver/rest/workspaces/"+dbSchema+"/featuretypes", featureTypeFileContent, gsUser, gsPwd);
        log.info(featureTypeResponse);
        if (featureTypeResponse == null) {
            throw new Exception("Error creating featuretype.");
        }         

        // Assign style to featuretype.
        String styleFileName = Paths.get(dataFile.getAbsoluteFile().getParent(), "style.xml").toFile()
                .getAbsolutePath();
        File styleFile = this.writeStyleXml(styleFileName, tableNameGrundnutzung, tableNameGrundnutzung);
        String styleFileContent = new String(Files.readAllBytes(Paths.get(styleFile.getAbsolutePath())));
        String styleResponse = HTTPUtils.putXml("http://" + gsHost + ":" + gsPort + "/geoserver/rest/workspaces/"+dbSchema+"/layers/" + tableNameGrundnutzung, styleFileContent, gsUser, gsPwd);
        log.info(styleResponse);
        if (featureTypeResponse == null) {
            throw new Exception("Error applying style.");
        }  
    }

    private File writeStyleXml(String xmlFileName, String layerName, String styleName) throws TransformerException, ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("layer");
        doc.appendChild(rootElement);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(layerName)); 
        rootElement.appendChild(name);

        Element defaultStyle = doc.createElement("defaultStyle");

        Element styleNameEl = doc.createElement("name");
        styleNameEl.appendChild(doc.createTextNode(styleName)); 
        defaultStyle.appendChild(styleNameEl);

        rootElement.appendChild(defaultStyle);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(new File(xmlFileName));    
        transformer.transform(source, result);
        
        return new File(xmlFileName);
    }

    private File writeNamespaceXml(String xmlFileName, String dbSchema) throws TransformerException, ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("namespace");
        doc.appendChild(rootElement);

        Element prefix = doc.createElement("prefix");
        prefix.appendChild(doc.createTextNode(dbSchema)); 
        rootElement.appendChild(prefix);

        Element uri = doc.createElement("uri");
        uri.appendChild(doc.createTextNode("http://arp.so.ch/"+dbSchema)); 
        rootElement.appendChild(uri);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(new File(xmlFileName));    
        transformer.transform(source, result);
        
        return new File(xmlFileName);
    }

    private File writeFeatureTypeXml(String xmlFileName, String layerName, String nativeName) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("featureType");
        doc.appendChild(rootElement);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(layerName)); 
        rootElement.appendChild(name);

        Element nativeNameEl = doc.createElement("nativeName");
        nativeNameEl.appendChild(doc.createTextNode(nativeName)); 
        rootElement.appendChild(nativeNameEl);

        Element title = doc.createElement("title");
        title.appendChild(doc.createTextNode(layerName)); 
        rootElement.appendChild(title);

        Element enabled = doc.createElement("enabled");
        enabled.appendChild(doc.createTextNode("true")); 
        rootElement.appendChild(enabled);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(new File(xmlFileName));    
        transformer.transform(source, result);
        
        return new File(xmlFileName);
    }

    private File writeDataStoreXml(String xmlFileName, String dbSchema) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("dataStore");
        doc.appendChild(rootElement);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(dbSchema)); // dbSchema
        rootElement.appendChild(name);

        Element type = doc.createElement("type");
        type.appendChild(doc.createTextNode("PostGIS"));
        rootElement.appendChild(type);

        Element enabled = doc.createElement("enabled");
        enabled.appendChild(doc.createTextNode("true"));
        rootElement.appendChild(enabled);

        Element connectionParameters = doc.createElement("connectionParameters");

        Map<String, String> elements = new HashMap<String, String> ();
        elements.put("port", "5432");
        elements.put("user", dbUser);
        elements.put("passwd", dbPwd);
        elements.put("dbtype", "postgis");
        elements.put("host", dbHost);
        elements.put("database", dbDatabase);
        elements.put("schema", dbSchema); //dbSchema
        elements.put("Evictor run periodicity", "300");
        elements.put("Max open prepared statements", "50");
        elements.put("Batch insert size", "1");
        elements.put("preparedStatements", "true");
        elements.put("Loose bbox", "true");
        elements.put("Estimated extends", "true");
        elements.put("fetch size", "1000");
        elements.put("Expose primary keys", "false");
        elements.put("validate connections", "true");
        elements.put("Support on the fly geometry simplification", "true");
        elements.put("Connection timeout", "20");
        elements.put("create database", "false");
        elements.put("min connections", "1");
        elements.put("max connections", "10");
        elements.put("Evictor tests per run", "3");
        elements.put("Test while idle", "3");
        elements.put("Max connection idle time", "300");

        for (Map.Entry<String,String> el : elements.entrySet()) {
            Element element = doc.createElement("entry");
            element.setAttribute("key", el.getKey());                                
            element.appendChild(doc.createTextNode(el.getValue())); 
            connectionParameters.appendChild(element);
        }

        rootElement.appendChild(connectionParameters);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult result = new StreamResult(new File(xmlFileName));

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
        
        return new File(xmlFileName);
    }
}
