package com.millerk97.ais.util;

import com.millerk97.ais.controller.FlowController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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


    public static String loadBearerToken() {
        try {
            return Files.readString(Path.of("src/main/resources/com/millerk97/bearertoken.txt"));
        } catch (IOException e) {
            FlowController.log("Could not retrieve bearer token");
        }
        return "";
    }
}
