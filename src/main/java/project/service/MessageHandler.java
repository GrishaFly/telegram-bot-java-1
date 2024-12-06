package project.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import project.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageHandler {
    private final TelegramClient telegramClient;

    public MessageHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendTextMessage(User user, String messageText) {
        sendTextMessage(String.valueOf(user.getChatId()), messageText);
    }

    public void sendTextMessage(String chatId, String messageText) {
        sendTextMessage(chatId, messageText, false);
    }

    public void sendTextMessage(User user, String messageText, boolean enableMarkdown) {
        sendTextMessage(String.valueOf(user.getChatId()), messageText, enableMarkdown);
    }

    public void sendTextMessage(String chatId, String messageText, boolean enableMarkdown) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageText)
                .parseMode(enableMarkdown ? "Markdown" : null)
                .build();
        sendMessageAsync(message);
    }

    public void sendStartKeyboard(User user) {
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton("Создать напоминание"));
        keyboardRow.add(new KeyboardButton("Мои напоминания"));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(keyboardRow);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);

        SendMessage message = SendMessage.builder()
                .chatId(user.getChatId())
                .text("Выберите действие:")
                .replyMarkup(replyKeyboardMarkup)
                .build();

        sendMessageAsync(message);
    }

    private void sendMessageAsync(SendMessage message) {
        CompletableFuture.runAsync(() -> {
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        });
    }

//    public void sendWelcomeMessage(User user, boolean isExistingUser) {
//        if (!isExistingUser) {
//            sendTextMessage(user, "Добро пожаловать, " + user.getFirstName() + "! Это бот для напоминаний.");
//        }
//        sendStartKeyboard(user);
//    }

//    public void handleNonTextMessage(User user) {
//        sendTextMessage(user, "Извините, я обрабатываю только текстовые сообщения.");
//    }
}
