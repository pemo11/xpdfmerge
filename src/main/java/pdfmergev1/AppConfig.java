/*
** file: AppConfig.java
** Umgang mit einer Config-Datei
`*/

package pdfmergev1;

import java.util.*;
import java.util.Properties;
import java.io.FileInputStream;

public class AppConfig {
        Properties configFile;

        AppConfig() {
            configFile = new java.util.Properties();
            try {
                String curPath = System.getProperty("user.dir");
                String configPath = curPath + "/" + "app.config";
                FileInputStream stream = new FileInputStream(configPath);
                configFile.load(stream);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        public String getProperty(String key) {
            String value = this.configFile.getProperty(key);
            return value;
        }
}
