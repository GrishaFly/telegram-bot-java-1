package project.logic;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import project.LogStatus;
import project.model.User;
import project.database.DatabaseManager;
import project.service.MessageHandler;
import project.service.ReminderService;
import project.service.UserSessionService;
import project.service.ConsoleLoggingService;
import project.util.TimeZoneFinder;

import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BotLogic implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final MessageHandler messageHandler;
    private final DatabaseManager databaseManager;
    private final ReminderService reminderService;
    private final ExecutorService executorService;
    private final ConsoleLoggingService loggingService;

    public BotLogic(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.databaseManager = DatabaseManager.getInstance();
        this.messageHandler = new MessageHandler(telegramClient);
        this.loggingService = new ConsoleLoggingService();
        this.reminderService = new ReminderService(databaseManager, messageHandler, UserSessionService.getInstance(), loggingService);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            executorService.submit(() -> processMessage(update));
        }
    }

    private void processMessage(Update update) {

        User user = databaseManager.getUser(update.getMessage().getFrom().getId());

        try {
            // Новый пользователь
            if (!databaseManager.isUserExist(update)) {
                user = new User(update);
                databaseManager.addUser(user);
                loggingService.log(user, "New user registered", LogStatus.INFO);
                messageHandler.sendTextMessage(user, "Добро пожаловать, " + user.getFirstName() + "! Это бот для напоминаний.\n \n" +
                        "Пожалуйста, укажите ваш город для установки правильного часового пояса.");
                return;
            }

            String messageText = update.getMessage().getText();
            loggingService.log(user, "Received message: " + messageText, LogStatus.INFO);

            // установка города
            if (!user.hasSetCity()) {
                handleCityInput(user, messageText);
                return;
            }

            if (messageText.equals("/start")) {
                messageHandler.sendStartKeyboard(user);
            } else {
                reminderService.handleCommand(user, messageText);
            }
        } catch (Exception e) {
            loggingService.log(user, "Error processing message: " + e.getMessage(), LogStatus.ERROR);
            messageHandler.sendTextMessage(user, "Произошла ошибка при обработке сообщения. Пожалуйста, попробуйте еще раз.");
            messageHandler.sendStartKeyboard(user);
        }
    }



    private void handleCityInput(User user, String cityName) {
        if (TimeZoneFinder.isValidCity(cityName)) {
            ZoneId timeZone = TimeZoneFinder.findTimeZoneByCity(cityName);
            if (timeZone != null) {
                databaseManager.updateUserCity(user, cityName, timeZone);
                messageHandler.sendTextMessage(user, "Отлично! Ваш город установлен: " + cityName + 
                    "\nЧасовой пояс: " + timeZone.getId() +
                        "\n\nТеперь вы можете создавать напоминания! Используйте команду /start для вызова меню.");
                   // "\n\nТеперь вы можете создавать напоминания! Используйте команду /help для просмотра доступных команд.");
            } else {
                String suggestions = TimeZoneFinder.getSuggestedCities(cityName);
                messageHandler.sendTextMessage(user, suggestions);
            }
        } else {
            String suggestions = TimeZoneFinder.getSuggestedCities(cityName);
            messageHandler.sendTextMessage(user, suggestions);
        }
    }

    public void close() {
        executorService.shutdown();
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }
}
