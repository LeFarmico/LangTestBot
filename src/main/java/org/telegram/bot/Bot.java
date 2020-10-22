package org.telegram.bot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@AllArgsConstructor
@NoArgsConstructor
public class Bot extends TelegramLongPollingBot {

    private  static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public final Queue<Object> sendQueue = new ConcurrentLinkedQueue<>();
    public final Queue<Object> receiveQueue = new ConcurrentLinkedQueue<>();

    final int RECONNECT_PAUSE = 10000;

    @Getter
    @Setter
    String botName;
    @Getter
    @Setter
    String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        logger.info("Receive new Update. updateID: " + update.getUpdateId());

        receiveQueue.add(update);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void botConnect(){
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try{
            telegramBotsApi.registerBot(this);
            logger.info("TelegramAPI started. Look for messages");
        }catch (TelegramApiRequestException e) {
            logger.warn("Cant Connect. Pause "
                    + RECONNECT_PAUSE / 1000
                    + "sec and try again. Error: "
                    + e.getMessage());
        try {
            Thread.sleep(RECONNECT_PAUSE);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            return;
        }
        botConnect();
        }
    }
}
