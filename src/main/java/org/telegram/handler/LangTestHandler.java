package org.telegram.handler;

import com.opencsv.CSVWriter;
import org.telegram.ability.LangTest;
import org.telegram.bot.Bot;
import org.telegram.command.Command;
import org.telegram.command.ParsedCommand;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class LangTestHandler extends AbstractHandler {
    private static final Logger logger = Logger.getLogger(LangTestHandler.class.getName());
    private static int rep = 1;
    static int repeatCount = 3;
    static int repeatTimeMin = 1;

    Path path = Paths.get("src/main/java/org/telegram/laguageWords/English.csv");
    File CSVFile = path.toFile();


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
                break;
            case WORDSINTEST:
                setWordsInTest(chatId, parsedCommand);
                break;
            case TIMETOREPEAT:
                setTimeBetweenTests(chatId, parsedCommand);
                break;
            case ADDWORD:
                addWord(chatId, parsedCommand, CSVFile);
                break;
        }
        return null;
    }
    private void runLangTest(Bot bot, String chatId){
            LangTest langTest = new LangTest(bot, chatId);
            langTest.run();
    }
    private void rightAnswer(final String chatId, final Update update){

        bot.sendQueue.add(getRightAnswerQuery(update));
        bot.sendQueue.add(removeButtonClick(chatId, update));
        bot.sendQueue.add(getRightAnswerMessage(chatId));

        if(rep < repeatCount) {
            runLangTest(bot, chatId);
            rep++;
        }else if(rep == repeatCount){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId).setText("Следующий тест начнется через: " + repeatTimeMin + " минут.");
            bot.sendQueue.add(sendMessage);
            class TestSandler extends TimerTask {
                @Override
                public void run() {
                    runLangTest(bot, chatId);
                    rep = 1;
                }
            }
            TimerTask timerTask = new TestSandler();
            Timer timer = new Timer(true);
            timer.schedule(timerTask, 10*100*60*repeatTimeMin);
        }
    }
    private void wrongAnswer(String chatId, Update update){
        bot.sendQueue.add(getWrongAnswerQuery(update));
        bot.sendQueue.add(getWrongAnswerMessage(chatId));
    }
    private void setWordsInTest(String chatId, ParsedCommand parsedCommand){

        //Создаем отправитель сообщений
        SendMessage sendMessage = new SendMessage();
        //Указываем ID куда отправляем сообщение
        sendMessage.setChatId(chatId);
        //???
        sendMessage.enableMarkdown(true);

        if (!parsedCommand.getText().equals("")){
            int count = Integer.parseInt(parsedCommand.getText());
            repeatCount = Math.max(count, 1);
            sendMessage.setText("Колличество слов в тесте равно: " + repeatCount);
        }else {
            ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
            forceReplyKeyboard.setSelective(true);
            sendMessage.setReplyMarkup(forceReplyKeyboard);
            sendMessage.setText("Введите желаемое колличество слов в тесте.");
        }
        bot.sendQueue.add(sendMessage);
    }
    private void setTimeBetweenTests(String chatId, ParsedCommand parsedCommand){
        //Создаем отправитель сообщений
        SendMessage sendMessage = new SendMessage();
        //Указываем ID куда отправляем сообщение
        sendMessage.setChatId(chatId);
        //???
        sendMessage.enableMarkdown(true);

        if (!parsedCommand.getText().equals("")){
            int count = Integer.parseInt(parsedCommand.getText());
            repeatTimeMin = Math.max(count, 1);
            sendMessage.setText("Время мужде тестами равно: " + repeatTimeMin + " минут.");
        }else {
            ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
            forceReplyKeyboard.setSelective(true);
            sendMessage.setReplyMarkup(forceReplyKeyboard);
            sendMessage.setText("Введите желаемое время между тестами (в минутах).");
        }
        bot.sendQueue.add(sendMessage);
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

    private void addWord(String chatId, ParsedCommand parsedCommand, File CSVFile){
        //Создаем отправитель сообщений
        SendMessage sendMessage = new SendMessage();
        //Указываем ID куда отправляем сообщение
        sendMessage.setChatId(chatId);
        //???
        sendMessage.enableMarkdown(true);

        if (!parsedCommand.getText().equals("")){
            String[] words = parsedCommand.getText().split(" ");
            if (words.length < 2){
                sendMessage.setText("Не верное колличество слов, попробуйте еще раз.");
                bot.sendQueue.add(sendMessage);
            }else if (words.length == 2){
                try {
                        CSVWriter csvWriter = new CSVWriter(
                                new FileWriter(CSVFile, true),
                                '\t',
                                ' ',
                                ' ',
                                "\n");
                        csvWriter.writeNext(words);
                        csvWriter.close();
                        sendMessage.setText("Добавлено слово: " + words[0] + " - " + words[1]);
                        bot.sendQueue.add(sendMessage);
                    }catch (IOException e){
                    logger.warning(e.toString());
                }
            }else {
                sendMessage.setText("Не верные данные, попробуйте еще раз.");
                bot.sendQueue.add(sendMessage);
            }
        }else {
            ForceReplyKeyboard forceReplyKeyboard = new ForceReplyKeyboard();
            forceReplyKeyboard.setSelective(true);
            sendMessage.setReplyMarkup(forceReplyKeyboard);
            sendMessage.setText("Введите новое слово в словар, в формате: Слово Перевод.");
            bot.sendQueue.add(sendMessage);
        }
    }
    private EditMessageReplyMarkup removeButtonClick(String chatId, Update update) {
        EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        edit.setChatId(chatId).setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        return edit;
    }
}

