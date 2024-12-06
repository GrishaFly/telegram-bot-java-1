package project.service;

import project.model.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
Этот класс отвечает за управление сессиями пользователей.
 */

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
        Long userId = user.getId();
        if (userSessions.containsKey(userId)) {
            return userSessions.get(userId);
        } else {
            UserSession newSession = new UserSession();
            userSessions.put(userId, newSession);
            return newSession;
        }
    }

    public void updateSession(User user, UserSession session) {
        userSessions.put(user.getId(), session);
    }

    public void clearSession(User user) {
        userSessions.remove(user.getId());
    }
}
