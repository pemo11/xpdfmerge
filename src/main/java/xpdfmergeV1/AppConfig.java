/*
** file: AppConfig.java
** Umgang mit einer Config-Datei
`*/

package xpdfmergeV1;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.file.Files.newBufferedWriter;

public class AppConfig {
        private Properties configFile;
        private String infoMessage = "";
        private String configPath;

        AppConfig() throws IOException {
            String userPath = "";
            configFile = new java.util.Properties();
            configPath  = "";
            try {
                // efxapp.config soll sich im Benutzerprofilverzeichnis (Home-Verzeichnis) befinden
                // TODO: Besser unter /var/xpdfmerge?
                userPath = System.getProperty("user.home");
                configPath = userPath + "/" + "efxapp.config";
                // TODO: Abfrage, ob Datei existiert
                File file = new File(configPath);
                if (file.exists()) {
                    FileInputStream stream = new FileInputStream(configPath);
                    configFile.load(stream);
                    stream.close();
                    infoMessage = String.format("AppConfig() - %s wurde geladen.", configPath);
                } else {
                    FileOutputStream stream = new FileOutputStream(configPath);
                    Charset charset = Charset.forName("UTF8");
                    try (BufferedWriter bufWriter = Files.newBufferedWriter(file.toPath(), charset)) {
                        bufWriter.write("xJustizPfad=" + userPath);
                        infoMessage = String.format("AppConfig() - %s war nicht vorhanden und wurde angelegt.", configPath);
                    } catch (IOException ex) {
                        infoMessage = String.format("AppConfig() - Fehler beim Anlegen von %s !!!", configPath);
                    }
                }
                XEFPdfMerge.logger.info(infoMessage);
            } catch(Exception ex) {
                infoMessage = String.format("AppConfig() - Fehler beim Zugriff auf %s.", configPath);
                XEFPdfMerge.logger.error(infoMessage);
                // IOException weiterreichen
                throw ex;
            }
        }

        public String getProperty(String key) {
            String value = this.configFile.getProperty(key);
            return value;
        }

        public void setProperty(String key, String value) {
            this.configFile.setProperty(key, value);
            Path propertyFile = Paths.get(configPath);
            try {
                Writer propWriter = Files.newBufferedWriter(propertyFile);
                this.configFile.store(propWriter, "Application Properties");
            } catch (IOException ex) {
                infoMessage = String.format("AppConfig->setProperty() - Fehler beim Zugriff auf %s.", configPath);
                XEFPdfMerge.logger.error(infoMessage);
            }
        }
}
