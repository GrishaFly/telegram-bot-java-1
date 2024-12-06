package project.logic;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import project.config.ConfigLoader;


public class Main {
    public static void main(String[] args) {
        String botToken = ConfigLoader.getInstance().getTelegramBotToken();
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new BotLogic(botToken));
            System.out.println("MyAmazingBot successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
