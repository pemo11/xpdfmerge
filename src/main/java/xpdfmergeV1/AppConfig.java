/*
** file: AppConfig.java
** Umgang mit einer Config-Datei
`*/

package xpdfmergeV1;

import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream;

public class AppConfig {
        Properties configFile;
        private String infoMessage = "";

        AppConfig() throws IOException {
            configFile = new java.util.Properties();
            String configPath = "";
            String curPath = "";
            try {
                curPath = System.getProperty("user.dir");
                configPath = curPath + "/" + "app.config";
                // TODO: Abfrage, ob Datei existiert
                FileInputStream stream = new FileInputStream(configPath);
                configFile.load(stream);
            } catch(Exception ex) {
                infoMessage = String.format("Fehler beim Zugriff auf %s.", configPath);
                XEFPdfMerge.logger.error(infoMessage);
                // IOException weiterreichen
                throw ex;
            }
        }

        public String getProperty(String key) {
            String value = this.configFile.getProperty(key);
            return value;
        }
}
