package project.model;

import project.database.DatabaseManager;
import project.util.DateTimeParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reminders {
    private String title;
    private String text;
    private LocalDateTime dateTime;
    private final User user;
    private final DatabaseManager databaseManager;

    public Reminders(User user) {
        this.user = user;
        this.databaseManager = DatabaseManager.getInstance();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isValidDate(String dateStr) {
        return DateTimeParser.isValidDate(dateStr, user.getTimeZone());

    }

    public void createReminder(String dateStr) {
        this.dateTime = DateTimeParser.parseUserInput(dateStr, user.getTimeZone());
        String userId = String.valueOf(user.getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        String formattedDateTime = dateTime.format(formatter);
        String createdAt = LocalDateTime.now(user.getTimeZone()).format(formatter);

        databaseManager.addReminder(userId, title, text, createdAt, formattedDateTime);
    }
}
