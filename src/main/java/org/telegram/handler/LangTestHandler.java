package org.telegram.handler;

import org.telegram.ability.LangTest;
import org.telegram.bot.Bot;
import org.telegram.command.Command;
import org.telegram.command.ParsedCommand;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class LangTestHandler extends AbstractHandler {
    private static final Logger logger = Logger.getLogger(LangTestHandler.class.getName());
    private static int rep = 1;

    public LangTestHandler(Bot bot) { super(bot); }

    @Override
    public String operate(String chatId, ParsedCommand parsedCommand, Update update) {
        Command command = parsedCommand.getCommand();

        switch (command){
            case LANGTEST:
                runLangTest(bot, chatId);
                logger.info("Command [" + command.toString() + "] started");
                break;
            case RIGHTANSWER:
                rightAnswer(chatId, update);
                logger.info("Command [" + command.toString() + "] started");
                break;
            case WRONGANSWER:
                wrongAnswer(chatId, update);
                logger.info("Command [" + command.toString() + "] started");
        }
        return null;
    }
    private void runLangTest(Bot bot, String chatId){
            LangTest langTest = new LangTest(bot, chatId);
            langTest.run();
    }
    private void rightAnswer(final String chatId, final Update update){
        int repeatCount = 3;
        bot.sendQueue.add(getRightAnswerQuery(update));
        bot.sendQueue.add(removeButtonClick(chatId, update));
        bot.sendQueue.add(getRightAnswerMessage(chatId));

        if(rep < repeatCount) {
            runLangTest(bot, chatId);
            rep++;
        }else if(rep == repeatCount){
            class TestSandler extends TimerTask {
                @Override
                public void run() {
                    runLangTest(bot, chatId);
                    rep = 1;
                }
            }
            TimerTask timerTask = new TestSandler();
            Timer timer = new Timer(true);
            timer.schedule(timerTask, 10*100*10);
        }
    }
    private void wrongAnswer(String chatId, Update update){
        bot.sendQueue.add(getWrongAnswerQuery(update));
        bot.sendQueue.add(getWrongAnswerMessage(chatId));
    }

    private SendMessage getRightAnswerMessage(String chatId){
        //Создаем отправитель сообщений
        SendMessage sendMessage = new SendMessage();
        //Указываем ID куда отправляем сообщение
        sendMessage.setChatId(chatId);
        //???
        sendMessage.enableMarkdown(true);

        sendMessage.setText("Верный ответ!");
        return sendMessage;
    }
    private SendMessage getWrongAnswerMessage(String chatId){
        //Создаем отправитель сообщений
        SendMessage sendMessage = new SendMessage();
        //Указываем ID куда отправляем сообщение
        sendMessage.setChatId(chatId);
        //???
        sendMessage.enableMarkdown(true);

        sendMessage.setText("Не верный ответ! Попробуйте еще раз");
        return sendMessage;
    }

    private AnswerCallbackQuery getRightAnswerQuery(Update update){
        AnswerCallbackQuery callbackQuery = new AnswerCallbackQuery();
        callbackQuery.setCallbackQueryId(
                update
                        .getCallbackQuery()
                        .getId())
                .setText("Верный ответ");
        return  callbackQuery;
    }
    private AnswerCallbackQuery getWrongAnswerQuery(Update update){
        AnswerCallbackQuery callbackQuery = new AnswerCallbackQuery();
        callbackQuery.setCallbackQueryId(
                update
                        .getCallbackQuery()
                        .getId())
                .setText("Вы ошиблись");
        return  callbackQuery;
    }

    private EditMessageReplyMarkup removeButtonClick(String chatId, Update update) {
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        edit.setChatId(chatId).setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return edit;
    }
}
