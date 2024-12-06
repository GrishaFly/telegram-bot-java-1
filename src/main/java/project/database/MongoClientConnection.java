package project.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import project.config.ConfigLoader;

public class MongoClientConnection {
    private static MongoClientConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> users;
    private MongoCollection<Document> reminders;

    private MongoClientConnection() {
        connectToMongoDB();
    }

    public static synchronized MongoClientConnection getInstance() {
        if (instance == null) {
            instance = new MongoClientConnection();
        }
        return instance;
    }

    private void connectToMongoDB() {
        ConfigLoader config = ConfigLoader.getInstance();
        String connectionString = config.getMongoDbConnectionString();
        String databaseName = config.getMongoDbDatabaseName();
        
        if (connectionString == null || connectionString.isEmpty()) {
            throw new RuntimeException("MongoDB connection string is not configured. Please set mongodb.connection.string in application.properties");
        }
        if (databaseName == null || databaseName.isEmpty()) {
            throw new RuntimeException("MongoDB database name is not configured. Please set mongodb.database.name in application.properties");
        }

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        try {
            //перенести в ConsoleLoggingService

            System.out.println("Connecting to MongoDB...");
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);
            
            // Проверяем подключение
            database.runCommand(new Document("ping", 1));
            System.out.println("✅ Successfully connected to MongoDB!");
            System.out.println("📊 Database: " + databaseName);
            
            // Инициализируем коллекции
            users = database.getCollection("users");
            reminders = database.getCollection("reminders");
            
            // Выводим статистику коллекций
            System.out.println("📈 Collections status:");
            System.out.println(" • Users: " + users.countDocuments() + " documents");
            System.out.println(" • Reminders: " + reminders.countDocuments() + " documents");
        } catch (MongoException e) {
            System.err.println("❌ Failed to connect to MongoDB: " + e.getMessage());
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    public MongoDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("Database connection is not established. Please connect first.");
        }
        return database;
    }

    public MongoCollection<Document> getUsers() {
        if (users == null) {
            throw new IllegalStateException("Users collection is not initialized.");
        }
        return users;
    }

    public MongoCollection<Document> getReminders() {
        if (reminders == null) {
            throw new IllegalStateException("Reminders collection is not initialized.");
        }
        return reminders;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            users = null;
            reminders = null;
        }
    }
}
