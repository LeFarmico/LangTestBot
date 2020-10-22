package org.telegram.ability;

import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.bot.Bot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@ToString
public class LangTest implements Runnable {
    private  static final Logger logger = LoggerFactory.getLogger(LangTest.class);
    private static final Set<Integer> usedLines = new HashSet<>();

    Bot bot;
    String chatID;
    long repeatTime;

    Random random = new Random();
    Path path = Paths.get("src/main/java/org/telegram/laguageWords/English.csv");
    File CSVFile = path.toFile();

    public LangTest(Bot bot, String chatID) {
        this.bot = bot;
        this.chatID = chatID;
    }

    @Override
    public void run() {
        logger.info("RUN. " + toString());
        final AnswerDescription answers = getAnswerDescriptions();

        InlineKeyboardButton correctButton = new InlineKeyboardButton()
                .setText(answers.correct[1])
                .setCallbackData("/rightanswer");
        InlineKeyboardButton wrongButton1 = new InlineKeyboardButton()
                .setText(answers.wrong1[1])
                .setCallbackData("/wronganswer");
        InlineKeyboardButton wrongButton2 = new InlineKeyboardButton()
                .setText(answers.wrong2[1])
                .setCallbackData("/wronganswer");

        List<InlineKeyboardButton> answersButtons = new ArrayList<>();
        answersButtons.add(correctButton);
        answersButtons.add(wrongButton1);
        answersButtons.add(wrongButton2);

        Collections.shuffle(answersButtons);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(answersButtons);

        InlineKeyboardMarkup answersButtonsMarkup = new InlineKeyboardMarkup().setKeyboard(rowList);

        bot.sendQueue.add(askTheWord(chatID, answersButtonsMarkup, answers));

    }

    private SendMessage askTheWord(String chatID, InlineKeyboardMarkup answerButtons, AnswerDescription correctAnswer){
        return new SendMessage()
                .setChatId(chatID)
                .setText("Выберите верный перевод слова: " + correctAnswer.correct[0])
                .setReplyMarkup(answerButtons);
    }
    private String[] getParseCSVLine(int lineNumber){
        try (FileReader fileReader = new FileReader(CSVFile)) {
            LineNumberReader lineNumberReader = new LineNumberReader(fileReader);

            String line = lineNumberReader.readLine();
            while (lineNumberReader.getLineNumber() != lineNumber) {
            line = lineNumberReader.readLine();
            }
            lineNumberReader.close();
            String splitter = "\t";
            return line.split(splitter);
        }catch (Exception e) {
            logger.error("File not found",e);
        }
        return new String[0];
    }

    private AnswerDescription getAnswerDescriptions(){
        int numberOfLines = getNumberOfLines(path);
        while (true) {
            int numberOfCorrectWord = random.nextInt(numberOfLines)+1;
            int randomNumber1 = random.nextInt(numberOfLines )+1;
            int randomNumber2 = random.nextInt(numberOfLines )+1;


            int numberOfWrong = randomNumber1 == numberOfCorrectWord?
                    randomNumber1+1 : randomNumber1;

            int numberOfWrong2 = randomNumber2 == numberOfCorrectWord || randomNumber2 == numberOfWrong?
                    randomNumber2+1 : randomNumber2;

            boolean isAlreadyUsed = usedLines.stream().anyMatch(s -> s == numberOfCorrectWord);
            if(usedLines.size() == numberOfLines)
                usedLines.clear();
            if (!isAlreadyUsed){
                usedLines.add(numberOfCorrectWord);

                return new AnswerDescription(
                        getParseCSVLine(numberOfCorrectWord),
                        getParseCSVLine(numberOfWrong),
                        getParseCSVLine(numberOfWrong2));
            }
        }
    }

    private static class AnswerDescription{
        @Getter
        String[] correct;
        @Getter
        String[] wrong1;
        @Getter
        String[] wrong2;

        public AnswerDescription(String[] correctAnswer, String[] wrong1, String[] wrongAnswer2) {
            this.correct = correctAnswer;
            this.wrong1 = wrong1;
            this.wrong2 = wrongAnswer2;
        }
    }

    private int getNumberOfLines (Path path){
        try{
            return (int) Files.lines(path).count();
        }catch (IOException e){
            logger.error("IO Exception",e);
        }
        return -1;
    }
}
