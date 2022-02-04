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
            String userPath = "";
            try {
                // efxapp.config soll sich im Benutzerprofilverzeichnis (Home-Verzeichnis) befinden
                // TODO: Besser unter /var/xpdfmerge?
                userPath = System.getProperty("user.home");
                configPath = userPath + "/" + "efxapp.config";
                // TODO: Abfrage, ob Datei existiert
                FileInputStream stream = new FileInputStream(configPath);
                configFile.load(stream);
                infoMessage = String.format("%s wurde geladen.", configPath);
                XEFPdfMerge.logger.info(infoMessage);
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
