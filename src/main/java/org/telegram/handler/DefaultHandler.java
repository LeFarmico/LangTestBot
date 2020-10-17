package org.telegram.handler;

import com.opencsv.CSVWriter;
import org.telegram.bot.Bot;
import org.telegram.command.ParsedCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultHandler extends AbstractHandler {
    public DefaultHandler(Bot bot) {
        super(bot);
    }

    Path path = Paths.get("src/main/java/org/telegram/laguageWords/English.csv");
    File CSVFile = path.toFile();

    @Override
    public String operate(String chatId, ParsedCommand parsedCommand, Update update) {
        setWordsInTest(chatId, update);
        setTimeTests(chatId, update);
        addMessage(chatId, update);
        return "";
    }
    private void setWordsInTest(String chatId, Update update){
        if(update.getMessage().isReply()){
            String msg = update.getMessage().getReplyToMessage().getText();
            if( msg.equals("Введите желаемое колличество слов в тесте.")){
                //Создаем отправитель сообщений
                SendMessage sendMessage = new SendMessage();
                //Указываем ID куда отправляем сообщение
                sendMessage.setChatId(chatId);
                //???
                sendMessage.enableMarkdown(true);

                int count = Integer.parseInt(update.getMessage().getText());
                LangTestHandler.repeatCount = Math.max(count, 1);

                sendMessage.setText("Колличество слов в тесте равно: " + LangTestHandler.repeatCount);

                bot.sendQueue.add(sendMessage);
            }
        }
    }
    private void setTimeTests(String chatId, Update update){
        if(update.getMessage().isReply()) {
            String msg = update.getMessage().getReplyToMessage().getText();
            if (msg.equals("Введите желаемое время между тестами (в минутах).")) {
                //Создаем отправитель сообщений
                SendMessage sendMessage = new SendMessage();
                //Указываем ID куда отправляем сообщение
                sendMessage.setChatId(chatId);
                //???
                sendMessage.enableMarkdown(true);

                int count = Integer.parseInt(update.getMessage().getText());
                LangTestHandler.repeatTimeMin = Math.max(count, 1);

                sendMessage.setText("Время мужде тестами равно: " + LangTestHandler.repeatTimeMin + " минут.");

                bot.sendQueue.add(sendMessage);
            }
        }
    }
    private void addMessage(String chatId, Update update){
        if(update.getMessage().isReply()) {
            String msg = update.getMessage().getReplyToMessage().getText();
            if (msg.equals("Введите новое слово в словар, в формате: Слово Перевод.")) {
                //Создаем отправитель сообщений
                SendMessage sendMessage = new SendMessage();
                //Указываем ID куда отправляем сообщение
                sendMessage.setChatId(chatId);
                //???
                sendMessage.enableMarkdown(true);

                String[] words = update.getMessage().getText().split(" ");
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
                        e.printStackTrace();
                    }
                }else {
                    sendMessage.setText("Не верные данные, попробуйте еще раз.");
                    bot.sendQueue.add(sendMessage);
                }
            }
        }
    }
}
