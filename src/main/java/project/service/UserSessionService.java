package project.service;

import project.model.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionService {
    private static UserSessionService instance;
    private final Map<Long, UserSession> userSessions;

    private UserSessionService() {
        userSessions = new ConcurrentHashMap<>();
    }

    public static synchronized UserSessionService getInstance() {
        if (instance == null) {
            instance = new UserSessionService();
        }
        return instance;
    }

    public UserSession getSession(User user) {
        return userSessions.computeIfAbsent(user.getId(), k -> new UserSession());
    }

    public void updateSession(User user, UserSession session) {
        userSessions.put(user.getId(), session);
    }

    public void clearSession(User user) {
        userSessions.remove(user.getId());
    }
}
