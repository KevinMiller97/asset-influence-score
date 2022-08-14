package com.millerk97.ais.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    public static String loadProperty(String property) throws IOException {
        try (InputStream input = PropertiesLoader.class.getResourceAsStream("/com/millerk97/application.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                throw new IOException("Could not load application.properties!");
            }
            prop.load(input);
            // get the property value and print it out
            return prop.getProperty(property);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        throw new IOException("Key " + property + " does not exist");
    }
}
