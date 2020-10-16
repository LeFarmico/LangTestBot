package org.telegram.service;

import org.telegram.bot.Bot;
import org.telegram.command.Command;
import org.telegram.command.ParsedCommand;
import org.telegram.command.Parser;
import org.telegram.handler.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.logging.Logger;

public class MessageReciever implements  Runnable{
    private static final Logger logger = Logger.getLogger(MessageSender.class.getName());
    private final int WAIT_FOR_NEW_MESSAGE = 1000;
    private Bot bot;
    Parser parser;

    public MessageReciever(Bot bot) {
        this.bot = bot;
        parser = new Parser(bot.getBotName());
    }

    @Override
    public void run() {
        logger.info("[STARTED] MsgReciever.  Bot class: " + bot);
        while (true){
            for (Object object = bot.receiveQueue.poll(); object != null; object = bot.receiveQueue.poll()){
                logger.info("New object for analyze in queue " + object);
                analyze(object);
            }
            try {
                Thread.sleep(WAIT_FOR_NEW_MESSAGE);
            }catch (InterruptedException e){
                logger.warning("Catch interrupt. Exit " + e);
            }
        }
    }
    private void analyze(Object object){
        if (object instanceof Update) {
            Update update = (Update) object;
            logger.info("Update reciever: " + update.toString());
            analyzeForUpdateType(update);
        }else{
            logger.warning("Cant operate type of object: " + object.toString());
        }
    }
    private void analyzeForUpdateType(Update update){
        Message message = update.getMessage();
        Long chatId;
        ParsedCommand parsedCommand;
        if (message == null){
            message = update.getCallbackQuery().getMessage();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            parsedCommand = parser.getParsedCommand(update.getCallbackQuery().getData());
        }else{
            chatId = update.getMessage().getChatId();
            parsedCommand = parser.getParsedCommand(message.getText());
        }

        AbstractHandler handlerForCommand = getHandlerForCommand(parsedCommand.getCommand());

        String operationResult = handlerForCommand.operate(chatId.toString(), parsedCommand, update);

        if(!"".equals(operationResult)){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(operationResult);
            bot.sendQueue.add(sendMessage);
        }
    }
    private AbstractHandler getHandlerForCommand(Command command){
        if(command == null){
            logger.warning("Null command accepted. This is not good scenario.");
            return new DefaultHandler(bot);
        }
        switch (command){
            case START:
            case HELP:
            case ID:
                SystemHandler systemHandler = new SystemHandler(bot);
                logger.info("Handler for command[" + command.toString() + "] is");
                return systemHandler;
            case LANGTEST:
            case RIGHTANSWER:
            case WRONGANSWER:
            case WORDSINTEST:
                LangTestHandler langTestHandler = new LangTestHandler(bot);
                logger.info("Handler for command[" + command.toString() + "] is");
                return langTestHandler;
            default:
                logger.info("Handler for command[" + command.toString() + "] not Set. Return DefaultHandler");
                return new DefaultHandler(bot);
        }
    }
}
