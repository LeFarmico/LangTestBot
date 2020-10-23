package org.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.bot.Bot;
import org.telegram.service.MessageReciever;
import org.telegram.service.MessageSender;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.nio.charset.Charset;


public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final int PRIORITY_FOR_SENDER = 1;
    private static final int PRIORITY_FOR_RECEIVER = 3;
    private static final String BOT_ADMIN = "505567555";


    public static void main(String[] args) {
        System.out.println(Charset.defaultCharset().name());
        ApiContextInitializer.init();
        Bot langTestBot = new Bot("My_personal_help_bot", "1120439674:AAGGxeQ1uP4T2bMvdA3ESu1BcTEdeE3g-yU");

        MessageReciever messageReciever = new MessageReciever(langTestBot);
        MessageSender messageSender = new MessageSender(langTestBot);

        try{
            langTestBot.botConnect();
            log.info("Bot connected");
//            sendStartReport(langTestBot);
        }catch (Exception e){
            log.error(e.getMessage());
        }

        Thread receiver = new Thread(messageReciever);
        receiver.setDaemon(true);
        receiver.setName("MsgReciever");
        receiver.setPriority(PRIORITY_FOR_RECEIVER);
        receiver.start();

        Thread sender = new Thread(messageSender);
        sender.setDaemon(true);
        sender.setName("MsgReciever");
        sender.setPriority(PRIORITY_FOR_SENDER);
        sender.start();

    }
    private  static void sendStartReport(Bot bot){
        SendMessage message = new SendMessage();
        message.setChatId(BOT_ADMIN);
        message.setText("Запустился");
        bot.sendQueue.add(message);
    }
}