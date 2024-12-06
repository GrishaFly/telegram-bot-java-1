package project.model;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.ZoneId;

public class User {
    private long id;
    private String username;
    private String firstName;
    private ZoneId timeZone;
    private String city;
    private boolean isAdmin;
    private boolean isBanned;
    private boolean hasSetCity;

    public User(Update update) {
        this.id = update.getMessage().getFrom().getId();
        this.username = update.getMessage().getFrom().getUserName();
        this.firstName = update.getMessage().getFrom().getFirstName();
        this.timeZone = ZoneId.of("UTC"); // По умолчанию UTC
        this.isAdmin = false;
        this.isBanned = false;
        this.hasSetCity = false;
    }

    public User(long id, String username, String firstName, ZoneId timeZone, String city, boolean isAdmin, boolean isBanned, boolean hasSetCity) {
        this.id = id;
        this.username = username;
        this.firstName = firstName != null ? firstName : "";
        this.timeZone = timeZone;
        this.city = city;
        this.isAdmin = isAdmin;
        this.isBanned = isBanned;
        this.hasSetCity = hasSetCity;
    }

    public long getId() {
        return id;
    }

    public long getChatId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean hasSetCity() {
        return hasSetCity;
    }

    public void setHasSetCity(boolean hasSetCity) {
        this.hasSetCity = hasSetCity;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
    }
}