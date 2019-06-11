package ch.so.arp.nplvalidator.camel.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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

    @Value("${app.dbHost4Geoserver")
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

    @Override
    public void process(Exchange exchange) throws Exception {
        File dataFile = exchange.getIn().getBody(File.class);
        String dbSchema = (String) exchange.getIn().getHeaders().get("DBSCHEMA");
        String dataStoreFileName = Paths.get(dataFile.getAbsoluteFile().getParent(), "datastore.xml").toFile().getAbsolutePath();

        File dataStoreFile = this.writeDataStoreXml(dataStoreFileName, dbSchema);
        String dataStoreFileContent = new String(Files.readAllBytes(Paths.get(dataStoreFile.getAbsolutePath())));
        
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                // TODO: parameterize it
                new AuthScope("localhost", 8080),
                new UsernamePasswordCredentials(gsUser, gsPwd));

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
        // TODO: parameterize it
        HttpPost httpPost = new HttpPost("http://localhost:8080/geoserver/rest/workspaces/"+gsWorkspace+"/datastores");
        httpPost.setHeader("Content-Type", "application/xml");
        StringEntity stringEntity = new StringEntity(dataStoreFileContent);
        httpPost.setEntity(stringEntity);
        
        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                // TODO: more error message information
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };

        String responseBody = httpclient.execute(httpPost, responseHandler);
        System.out.println("----------------------------------------");
        System.out.println(responseBody);

        
        // TODO: publish layer and assign style

    }
    
    private File writeDataStoreXml(String xmlFileName, String dbSchema) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("dataStore");
        doc.appendChild(rootElement);

        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode("fubar1")); // dbSchema
        rootElement.appendChild(name);

        Element type = doc.createElement("type");
        type.appendChild(doc.createTextNode("PostGIS"));
        rootElement.appendChild(type);

        Element enabled = doc.createElement("enabled");
        enabled.appendChild(doc.createTextNode("true"));
        rootElement.appendChild(enabled);

        Element connectionParameters = doc.createElement("connectionParameters");

        // TODO: loop over map

        Element port = doc.createElement("entry");
        port.setAttribute("key", "port");
        port.appendChild(doc.createTextNode("5432")); // dbPortNplso
        connectionParameters.appendChild(port);

        Element user = doc.createElement("entry");
        user.setAttribute("key", "user");
        user.appendChild(doc.createTextNode(dbUser));
        connectionParameters.appendChild(user);

        Element passwd = doc.createElement("entry");
        passwd.setAttribute("key", "passwd");
        passwd.appendChild(doc.createTextNode(dbPwd));
        connectionParameters.appendChild(passwd);
        
        Element dbtype = doc.createElement("entry");
        dbtype.setAttribute("key", "dbtype");        
        dbtype.appendChild(doc.createTextNode("postgis"));
        connectionParameters.appendChild(dbtype);

        Element host = doc.createElement("entry");
        host.setAttribute("key", "host");                
        host.appendChild(doc.createTextNode("postgres")); // dbHostNplso
        connectionParameters.appendChild(host);

        Element database = doc.createElement("entry");
        database.setAttribute("key", "database");                
        database.appendChild(doc.createTextNode(dbDatabase));
        connectionParameters.appendChild(database);

        Element schema = doc.createElement("entry");
        schema.setAttribute("key", "schema");                        
        schema.appendChild(doc.createTextNode("npl_test1")); // dbSchema
        connectionParameters.appendChild(schema);

        Element evictorRun = doc.createElement("entry");
        evictorRun.setAttribute("key", "Evictor run periodicity");                                
        evictorRun.appendChild(doc.createTextNode("300")); 
        connectionParameters.appendChild(evictorRun);
        
        Element maxOpenPrepStatements = doc.createElement("entry");
        maxOpenPrepStatements.setAttribute("key", "Max open prepared statements");                                
        maxOpenPrepStatements.appendChild(doc.createTextNode("50")); 
        connectionParameters.appendChild(maxOpenPrepStatements);

        Element encodeFunctions = doc.createElement("entry");
        encodeFunctions.setAttribute("key", "encode functions");                                
        encodeFunctions.appendChild(doc.createTextNode("false")); 
        connectionParameters.appendChild(encodeFunctions);

        Element batchSize = doc.createElement("entry");
        batchSize.setAttribute("key", "Batch insert size");                                
        batchSize.appendChild(doc.createTextNode("1")); 
        connectionParameters.appendChild(batchSize);

        Element preparedStatements = doc.createElement("entry");
        preparedStatements.setAttribute("key", "preparedStatements");                                
        preparedStatements.appendChild(doc.createTextNode("true")); 
        connectionParameters.appendChild(preparedStatements);

        Element looseBbox = doc.createElement("entry");
        looseBbox.setAttribute("key", "Loose bbox");                                
        looseBbox.appendChild(doc.createTextNode("true")); 
        connectionParameters.appendChild(looseBbox);

        Element estimatedExtends = doc.createElement("entry");
        estimatedExtends.setAttribute("key", "Estimated extends");                                
        estimatedExtends.appendChild(doc.createTextNode("true")); 
        connectionParameters.appendChild(estimatedExtends);

        Element fetchSize = doc.createElement("entry");
        fetchSize.setAttribute("key", "fetch size");                                
        fetchSize.appendChild(doc.createTextNode("1000")); 
        connectionParameters.appendChild(fetchSize);

        Element exposePk = doc.createElement("entry");
        exposePk.setAttribute("key", "Expose primary keys");                                
        exposePk.appendChild(doc.createTextNode("false")); 
        connectionParameters.appendChild(exposePk);

        Element validateConnections = doc.createElement("entry");
        validateConnections.setAttribute("key", "validate connections");                                
        validateConnections.appendChild(doc.createTextNode("true")); 
        connectionParameters.appendChild(validateConnections);

        Element geometrySimplification = doc.createElement("entry");
        geometrySimplification.setAttribute("key", "Support on the fly geometry simplification");                                
        geometrySimplification.appendChild(doc.createTextNode("true")); 
        connectionParameters.appendChild(geometrySimplification);

        Element connectionTimeout = doc.createElement("entry");
        connectionTimeout.setAttribute("key", "Connection timeout");                                
        connectionTimeout.appendChild(doc.createTextNode("true")); 
        connectionParameters.appendChild(connectionTimeout);

        Element createDatabase = doc.createElement("entry");
        createDatabase.setAttribute("key", "create database");                                
        createDatabase.appendChild(doc.createTextNode("false")); 
        connectionParameters.appendChild(createDatabase);

        Element minConnections = doc.createElement("entry");
        minConnections.setAttribute("key", "min connections");                                
        minConnections.appendChild(doc.createTextNode("1")); 
        connectionParameters.appendChild(minConnections);
        
        Element maxConnections = doc.createElement("entry");
        maxConnections.setAttribute("key", "max connections");                                
        maxConnections.appendChild(doc.createTextNode("10")); 
        connectionParameters.appendChild(maxConnections);
        
        Element evictorTests = doc.createElement("entry");
        evictorTests.setAttribute("key", "Evictor tests per run");                                
        evictorTests.appendChild(doc.createTextNode("3")); 
        connectionParameters.appendChild(evictorTests);
        
        Element testWhileIdle = doc.createElement("entry");
        testWhileIdle.setAttribute("key", "Test while idle");                                
        testWhileIdle.appendChild(doc.createTextNode("true")); 
        connectionParameters.appendChild(testWhileIdle);
        
        Element maxConnectionIdleTime = doc.createElement("entry");
        maxConnectionIdleTime.setAttribute("key", "Max connection idle time");                                
        maxConnectionIdleTime.appendChild(doc.createTextNode("300")); 
        connectionParameters.appendChild(maxConnectionIdleTime);

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
