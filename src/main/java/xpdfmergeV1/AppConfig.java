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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.nio.file.Files.newBufferedWriter;

public class AppConfig {
        private Properties configFile;
        private String infoMessage = "";
        private String configPath;

        AppConfig() throws IOException {
            String userHome = "";
            configFile = new java.util.Properties();
            configPath  = "";
            try {
                // efxapp.config soll sich im Benutzerprofilverzeichnis (Home-Verzeichnis) befinden
                // TODO: Besser unter /var/xpdfmerge?
                userHome = System.getProperty("user.home");
                configPath = Paths.get(userHome, "efxapp.config").toString();
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
                        bufWriter.write("xJustizPfad=" + userHome);
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

        /**
         * Speichert die LRU-Liste in der Konfiguration
         * @param lruList Die LRU-Liste, die gespeichert werden soll
         * @param maxEntries Maximale Anzahl der zu speichernden Einträge
         */
        public void saveLruList(List<String> lruList, int maxEntries) {
            if (lruList == null || lruList.isEmpty()) {
                return;
            }

            // Speichere die Anzahl der LRU-Einträge
            int numEntries = Math.min(lruList.size(), maxEntries);
            setProperty("lruCount", String.valueOf(numEntries));

            // Speichere jeden Eintrag einzeln
            for (int i = 0; i < numEntries; i++) {
                setProperty("lruEntry" + i, lruList.get(i));
            }

            // Deutliche Debug-Ausgabe
            System.out.println("DEBUG: LRU-Liste mit " + numEntries + " Einträgen gespeichert!");
            XEFPdfMerge.logger.info(String.format("LRU-Liste mit %d Einträgen gespeichert", numEntries));
        }

        /**
         * Lädt die LRU-Liste aus der Konfiguration
         * @param maxEntries Maximale Anzahl der zu ladenden Einträge
         * @return Die geladene LRU-Liste
         */
        public List<String> loadLruList(int maxEntries) {
            List<String> result = new ArrayList<>();
            
            // Lese die Anzahl der gespeicherten Einträge
            String countStr = getProperty("lruCount");
            if (countStr == null || countStr.isEmpty()) {
                return result;
            }
            
            try {
                int count = Integer.parseInt(countStr);
                count = Math.min(count, maxEntries);
                
                // Lade jeden Eintrag einzeln
                for (int i = 0; i < count; i++) {
                    String entry = getProperty("lruEntry" + i);
                    if (entry != null && !entry.isEmpty()) {
                        result.add(entry);
                    }
                }
                
                // Deutliche Debug-Ausgabe
                System.out.println("DEBUG: LRU-Liste mit " + result.size() + " Einträgen geladen!");
                XEFPdfMerge.logger.info(String.format("LRU-Liste mit %d Einträgen geladen", result.size()));
            } catch (NumberFormatException ex) {
                XEFPdfMerge.logger.error("Fehler beim Laden der LRU-Liste: " + ex.getMessage());
            }
            
            return result;
        }
}
