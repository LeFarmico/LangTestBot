package org.telegram.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.bot.Bot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Message;


public class MessageSender implements  Runnable{
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class.getName());
    private final int SENDER_SLEEP_TIME = 1000;
    private Bot bot;

    //Передаем объект класса Bot из него мы будем брать объекты для отправки сообщений
    //и с его же помощью будем их отправлять. (через execute)
    public MessageSender(Bot bot) {
        this.bot = bot;
    }

    //Наследование интерфейса Runnable для многопоточности:
    @Override
    public void run() {
        logger.info("[STARTED] MsgSender.  Bot class: " + bot);
        try{
            //бесконечный цикл проверяет очередь на отправку и вызывает команду send
            while (true){
                for (Object object = bot.sendQueue.poll(); object != null; object = bot.sendQueue.poll()){
                    logger.info("Get new msg to send " + object.toString());
                    send(object);
                }
                try {
                    Thread.sleep(SENDER_SLEEP_TIME);

                } catch (InterruptedException e) {
                    logger.debug(e.getMessage());
                }
            }
        }catch (Exception e){
            logger.debug(e.getMessage());
        }
    }

    //Тут реализовываем новые типы сообщений и отправляем их пользователю
    private void send(Object object){
        try {
            MessageType messageType = messageType(object);
            //После подтверждения типа сообщения, реализовываем execute согласно типу
            switch (messageType){
                case EXECUTE:
                    BotApiMethod<Message> message = (BotApiMethod<Message>) object;
                    logger.info("Use Execute for " + object);
                    bot.execute(message);
                    break;
                case STICKER:
                    SendSticker sendSticker = (SendSticker) object;
                    logger.info("Use SendSticker for: " + object);
                    bot.execute(sendSticker);
                    break;
                default:
                    logger.warn("Cant detect type of object. ", object);
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    //Тут добавляем новые типы сообщений
    enum MessageType{
        STICKER, EXECUTE, NOT_DETECTED
    }

    //Тут обрабатываем типы сообщений по Объекту в свойствах метода
    private MessageType messageType(Object o){
        if (o instanceof SendSticker) return MessageType.STICKER;
        if (o instanceof BotApiMethod) return MessageType.EXECUTE;
        return MessageType.NOT_DETECTED;
    }
}
