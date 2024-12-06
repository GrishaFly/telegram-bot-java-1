package project.service;

import project.model.Reminders;
import project.model.User;
import project.database.DatabaseManager;

public class CommandHandlerService {
    private final DatabaseManager databaseManager;
    private final MessageHandler messageHandler;
    private final UserSessionService sessionService;

    public CommandHandlerService(DatabaseManager databaseManager, MessageHandler messageHandler,
                               UserSessionService sessionService) {
        this.databaseManager = databaseManager;
        this.messageHandler = messageHandler;
        this.sessionService = sessionService;
    }

    public void handleCommand(User user, String command) {
        UserSession session = sessionService.getSession(user);

        switch (command) {
            case "/start":
                messageHandler.sendStartKeyboard(user);
                break;
            case "Создать напоминание":
                startReminderCreation(user, session);
                break;
            case "Мои напоминания":
                showReminders(user);
                break;
            default:
                handleUserInput(user, command, session);
        }
    }

    private void startReminderCreation(User user, UserSession session) {
        session.setCurrentStep("title");
        session.setReminder(new Reminders(user));
        messageHandler.sendTextMessage(user, "Введите заголовок напоминания:");
        sessionService.updateSession(user, session);
    }

    private void showReminders(User user) {
        messageHandler.sendTextMessage(user, "Ваши напоминания:");
        databaseManager.getAllReminders(String.valueOf(user.getId()), messageHandler);
    }

    private void handleUserInput(User user, String input, UserSession session) {
        switch (session.getCurrentStep()) {
            case "title":
                handleTitleInput(user, input, session);
                break;
            case "text":
                handleTextInput(user, input, session);
                break;
            case "date":
                handleDateInput(user, input, session);
                break;
            default:
                messageHandler.sendTextMessage(user, "Неизвестная команда. Пожалуйста, начните заново.");
                sessionService.clearSession(user);
        }
    }

    private void handleTitleInput(User user, String input, UserSession session) {
        session.getReminder().setTitle(input);
        session.setCurrentStep("text");
        messageHandler.sendTextMessage(user, "Введите текст напоминания:");
        sessionService.updateSession(user, session);
    }

    private void handleTextInput(User user, String input, UserSession session) {
        session.getReminder().setText(input);
        session.setCurrentStep("date");
        messageHandler.sendTextMessage(user, "Введите дату напоминания (формат: dd/MM/yy или dd/MM/yy HH:mm):");
        sessionService.updateSession(user, session);
    }

    private void handleDateInput(User user, String input, UserSession session) {
        if (!session.getReminder().isValidDate(input)) {
            messageHandler.sendTextMessage(user, "Некорректный формат даты. Введите дату напоминания:");
            return;
        }
        session.getReminder().createReminder(input);
        messageHandler.sendTextMessage(user, "Напоминание успешно создано!");
        sessionService.clearSession(user);
    }
}
