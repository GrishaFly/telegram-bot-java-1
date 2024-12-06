package project.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import project.model.User;
import project.service.MessageHandler;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final MongoCollection<Document> users;
    private final MongoCollection<Document> reminders;

    private DatabaseManager() {
        MongoClientConnection connection = MongoClientConnection.getInstance();
        this.users = connection.getUsers();
        this.reminders = connection.getReminders();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public boolean isUserExist(User user) {
        Document userDoc = users.find(Filters.eq("_id", user.getId())).first();
        if (userDoc == null) {
            addUser(user);
            return false;
        }
        return true;
    }
    public boolean isUserExist(Update update) {
        Document userDoc = users.find(Filters.eq("_id", update.getMessage().getFrom().getId())).first();
        if (userDoc == null) {
            return false;
        }
        return true;
    }

    public void addUser(User user) {
        Document userDoc = new Document("_id", user.getId())
                .append("username", user.getUsername())
                .append("firstName", user.getFirstName())
                .append("timezone", user.getTimeZone().getId())
                .append("city", user.getCity())
                .append("hasSetCity", user.hasSetCity())
                .append("isAdmin", user.isAdmin())
                .append("isBanned", user.isBanned())
                .append("registrationDate", new Date());
        
        users.insertOne(userDoc);
    }

    public void updateUser(User user) {
        users.updateOne(
            Filters.eq("_id", user.getId()),
            Updates.combine(
                Updates.set("username", user.getUsername()),
                Updates.set("firstName", user.getFirstName()),
                Updates.set("timezone", user.getTimeZone().getId()),
                Updates.set("isAdmin", user.isAdmin()),
                Updates.set("isBanned", user.isBanned())
            )
        );
    }



    public User getUser(long userId) {
        Document userDoc = users.find(Filters.eq("_id", userId)).first();
        if (userDoc == null) {
            return null;
        }

//        String timezone = userDoc.get("timezone", String.class);
//        if (timezone == null) {
//            timezone = "UTC";
//        }

        return new User(
                userDoc.getLong("_id"),
                userDoc.getString("username"),
                userDoc.getString("firstName"),
                ZoneId.of(userDoc.getString("timezone")),
                userDoc.getString("city"),
                userDoc.getBoolean("isAdmin", false),
                userDoc.getBoolean("isBanned", false),
                userDoc.getBoolean("hasSetCity", false)
        );
    }

    public void updateUserCity(User user, String city, ZoneId timeZone) {
        users.updateOne(
            Filters.eq("_id", user.getId()),
            Updates.combine(
                Updates.set("city", city),
                Updates.set("timezone", timeZone.getId()),
                Updates.set("hasSetCity", true)
            )
        );
    }

    public void addReminder(String userId, String title, String text, String createdAt, String remindAt) {
        Document reminderDoc = new Document("userId", userId)
                .append("title", title)
                .append("text", text)
                .append("createdAt", createdAt)
                .append("remindAt", remindAt)
                .append("status", "active")
                .append("isRepeating", false)
                .append("isEnabled", true);
        
        reminders.insertOne(reminderDoc);
    }

    public void getAllReminders(String userId, MessageHandler messageHandler) {
        List<Document> userReminders = new ArrayList<>();
        reminders.find(Filters.eq("userId", userId)).into(userReminders);

        if (userReminders.isEmpty()) {
            messageHandler.sendTextMessage(userId, "📝 У вас пока нет напоминаний");
            return;
        }

        StringBuilder message = new StringBuilder("*Ваши напоминания:*\n\n");
        int index = 1;
        for (Document reminder : userReminders) {
            String status = reminder.getString("status");
            
            message.append(String.format("*%d)* %s - %s\n",
                index++,
                reminder.getString("title"),
                    reminder.getString("text")
            ));
            message.append(String.format("⏰ *%s*\n",
                reminder.getString("remindAt")
            ));
            message.append("\n");
        }

        // Добавляем подсказку
        //message.append("\n💡 *Статусы напоминаний:*\n");

        
        messageHandler.sendTextMessage(userId, message.toString(), true);
    }

    public void getAllUsers(List<Document> usersList) {
        users.find().into(usersList);
    }







    public void closeConnection() {
        MongoClientConnection connection = MongoClientConnection.getInstance();
        connection.close();
    }
}
