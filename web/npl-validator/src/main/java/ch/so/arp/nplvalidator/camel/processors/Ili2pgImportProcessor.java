package ch.so.arp.nplvalidator.camel.processors;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2pg.PgMain;

@Component
public class Ili2pgImportProcessor implements Processor {
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
        String dbSchema = new File(dataFile.getParentFile().getAbsolutePath()).getName();

        Config settings = createConfig();
        settings.setFunction(Config.FC_IMPORT);
        settings.setDoImplicitSchemaImport(true);
        
        settings.setDbhost(dbHostNplso);
        settings.setDbport(dbPortNplso);
        settings.setDbdatabase(dbDatabaseNplso);
        settings.setDbschema(dbSchema); 
        settings.setDbusr(dbUserNplso);
        settings.setDbpwd(dbPwdNplso);

        String dburl = "jdbc:postgresql://" + settings.getDbhost() + ":" + settings.getDbport() + "/"
                + settings.getDbdatabase();
        settings.setDburl(dburl);

        settings.setNameOptimization(settings.NAME_OPTIMIZATION_TOPIC);
        settings.setValidation(false);
        settings.setModels(models);
        settings.setItfTransferfile(false);
        settings.setXtffile(dataFile.getAbsolutePath());

        Ili2db.readSettingsFromDb(settings);
        Ili2db.run(settings, null);

//        String fileName = (String) exchange.getIn().getHeaders().get(Exchange.FILE_NAME);
    }

    private Config createConfig() {
        Config settings = new Config();
        new PgMain().initConfig(settings);
        return settings;
    }

}
