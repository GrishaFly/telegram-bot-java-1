package project.service;

import project.model.Reminders;
import project.model.User;
import project.database.DatabaseManager;
import org.bson.Document;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ReminderService {
    private final DatabaseManager databaseManager;
    private final MessageHandler messageHandler;
    private final UserSessionService sessionService;
    private final ConsoleLoggingService loggingService;

    public ReminderService(DatabaseManager databaseManager, MessageHandler messageHandler,
                           UserSessionService sessionService, ConsoleLoggingService loggingService) {
        this.databaseManager = databaseManager;
        this.messageHandler = messageHandler;
        this.sessionService = sessionService;
        this.loggingService = loggingService;
    }

    public void handleCommand(User user, String command) {
        //loggingService.log(user, "Command: " + command);

        // Проверяем, не забанен ли пользователь
        if (user.isBanned()) {
            messageHandler.sendTextMessage(user, "Извините, вы заблокированы в системе. Обратитесь к администратору.");
            return;
        }

        // Обработка обычных команд
        switch (command) {
            case "Создать напоминание":
                startReminderCreation(user, sessionService.getSession(user));
                break;
            case "Мои напоминания":
                showReminders(user);
                break;
            default:
                UserSession session = sessionService.getSession(user);
                if (session != null && session.getReminder() != null) {
                    handleReminderInput(user, command, session);
                } else {
                    messageHandler.sendTextMessage(user, "❓ Неизвестная команда");
                    messageHandler.sendStartKeyboard(user);
                }
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
        messageHandler.sendStartKeyboard(user);
    }

    private void handleReminderInput(User user, String input, UserSession session) {
        if (session == null || session.getReminder() == null) {
            messageHandler.sendTextMessage(user, "Пожалуйста, начните создание напоминания заново.");
            messageHandler.sendStartKeyboard(user);
            return;
        }

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
                messageHandler.sendStartKeyboard(user);
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
        messageHandler.sendTextMessage(user, "Введите дату напоминания (например: сегодня, завтра, в понедельник, через 2 дня):");
        sessionService.updateSession(user, session);
    }

    private void handleDateInput(User user, String input, UserSession session) {
        loggingService.log(user, "Date input: " + input);

        if (!session.getReminder().isValidDate(input)) {
            try {
                session.getReminder().isValidDate(input);
            } catch (IllegalArgumentException e) {
                loggingService.log(user, "Invalid date format: " + e.getMessage());
                messageHandler.sendTextMessage(user, e.getMessage());
                return;
            }
            messageHandler.sendTextMessage(user, "Некорректный формат даты. Используйте:\n" +
                    "- dd/MM/yy HH:mm (например, 25/12/23 15:30)\n" +
                    "- завтра в HH:mm (например, завтра в 14:32 или 14.32)\n" +
                    "- сегодня, завтра, послезавтра\n" +
                    "- день недели (например, понедельник)\n" +
                    "- через X дней/часов/минут");
            return;
        }

        try {
            session.getReminder().createReminder(input);
            loggingService.log(user, "Reminder created successfully");
            messageHandler.sendTextMessage(user, "Напоминание успешно создано!");
            sessionService.clearSession(user);
        } catch (Exception e) {
            loggingService.log(user, "Error creating reminder: " + e.getMessage());
            messageHandler.sendTextMessage(user, "Произошла ошибка при создании напоминания: " + e.getMessage());
            sessionService.clearSession(user);
        }
    }


}
