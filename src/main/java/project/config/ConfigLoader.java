package project.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();
    private static ConfigLoader instance;

    private ConfigLoader() {
        loadProperties();
    }

    public static ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find application.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading application.properties", e);
        }
    }

    public String getTelegramBotToken() {
        return properties.getProperty("telegram.bot.token");
    }

    public String getMongoDbConnectionString() {
        return properties.getProperty("mongodb.connection.string");
    }

    public String getMongoDbDatabaseName() {
        return properties.getProperty("mongodb.database.name");
    }
}
