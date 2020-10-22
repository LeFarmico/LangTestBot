package org.telegram.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.bot.Bot;
import org.telegram.command.Command;
import org.telegram.command.ParsedCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


public class SystemHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(SystemHandler.class.getName());
    private final String END_LINE = "\n";

    public SystemHandler(Bot bot) {
        super(bot);
    }

    @Override
    public String operate(String chatId, ParsedCommand parsedCommand, Update update) {
        Command command = parsedCommand.getCommand();

        switch (command){
            case START:
                bot.sendQueue.add(getStartMessage(chatId));
                break;
            case HELP:
                bot.sendQueue.add(getHelpMessage(chatId));
                break;
            case ID:
                return "Your telegramID: " + update.getMessage().getFrom().getId();
        }
        //Ничего не вернет если команда не распознана
        return "";
    }
    private SendMessage getStartMessage(String chatID){
        //Создаем отправитель сообщений
        SendMessage sendMessage = new SendMessage();
        //Указываем ID куда отправляем сообщение
        sendMessage.setChatId(chatID);
        //???
        sendMessage.enableMarkdown(true);

        StringBuilder stringText = new StringBuilder();
        stringText.append("Привет я бот Артёма").append(END_LINE);
        stringText.append("Я создан чтобы помочь изучать языки").append(END_LINE);
        stringText.append("Чтобы узнать что я умею - введи команду [/help](/help)");

        sendMessage.setText(stringText.toString());
        return sendMessage;
    }
    private SendMessage getHelpMessage(String chatID){
        //Создаем отправитель сообщений
        SendMessage sendMessage = new SendMessage();
        //Указываем ID куда отправляем сообщение
        sendMessage.setChatId(chatID);
        //???
        sendMessage.enableMarkdown(true);

        StringBuilder stringText = new StringBuilder();
        stringText.append("*Это вспомогательное сообщение - здесь назодятся всё что я умею.*").append(END_LINE).append(END_LINE);
        stringText.append("[/start](/start) - приветственное сообщение").append(END_LINE);
        stringText.append("[/help](/help) - узнать все что я умею").append(END_LINE);
        stringText.append("[/langtest](/langtest) - запуск теста").append(END_LINE);
        stringText.append("[/timetorepeat](/timetorepeat) - установить время, через которое будет приходить новый тест").append(END_LINE);
        stringText.append("[/wordsintest](/wordsintest) - Установить количество слов в тесте").append(END_LINE);
        stringText.append("[/addword](/addword) - добавить слово").append(END_LINE);

        sendMessage.setText(stringText.toString());
        return sendMessage;
    }
}
