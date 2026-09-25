package org.example.service;

import java.io.InputStream;
import java.util.Properties;

public final class ApiKeys {

    private static final String NOME = "GIPHY_API_KEY";

    private ApiKeys() {
    }

    public static String giphy() {
        String chave = System.getenv(NOME);
        if (chave != null && !chave.isBlank()) {
            return chave.trim();
        }

        chave = System.getProperty(NOME);
        if (chave != null && !chave.isBlank()) {
            return chave.trim();
        }

        try (InputStream in = ApiKeys.class.getClassLoader().getResourceAsStream("giphy.properties")) {
            if (in == null) {
                return null;
            }
            Properties props = new Properties();
            props.load(in);
            chave = props.getProperty(NOME);
            if (chave != null && !chave.isBlank()) {
                return chave.trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
