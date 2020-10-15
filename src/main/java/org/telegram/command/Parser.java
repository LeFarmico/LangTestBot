package org.telegram.command;



import org.telegram.utilites.Pair;

import java.util.logging.Logger;

// При инициализации парсера мы передаем в конструкторе имя бота,
// для того чтобы парсер умел отличать свои команды от чужих.
// Дальше, просто вызываем публичный метод, в аргументах которого передаем текст сообщения,
// в ответ должена вернуться команда и текст сообщения идущий после команды.

public class Parser {
    private static final Logger log = Logger.getLogger(Parser.class.getName());
    private static final String PREFIX_FOR_COMMAND = "/";
    private static final String DELIMITER_COMMAND_BOTNAME = "@";
    private String botName;

    public Parser(String botName){
        //Название бота, чтобы понять, что обращение к нему
        this.botName = botName;
    }

    public ParsedCommand getParsedCommand(String text){
        String trimText = "";
        //Проверка на nullPointedExeption
        if (text != null) trimText = text.trim();
        ParsedCommand result = new ParsedCommand(Command.NONE, trimText);

        //Возвращаем пустую команду, если она пустая
        if("".equals(trimText)) return result;

        //Отделяем команду от текста
        Pair<String,String> commandAndText = getDelimitedCommand(trimText);
        //Проверяем команду
        if(isCommand(commandAndText.first)){
            //Проверяем для какого бота
            if(isCommandForMe(commandAndText.first)){
                String commandForParse = cutCommandFromText(commandAndText.first);
                Command commandFromText = getCommandFromText(commandForParse);
                //Если команда бля бота возвращаем команду и текст
                result.setText(commandAndText.second);
                result.setCommand(commandFromText);
            }else{
                //Если команда не для бота возвращаем текст и команде NOTFORME
                result.setText(commandAndText.second);
                result.setCommand(Command.NOTFORME);
            }
        }
        return result;
    }

    private Pair<String, String> getDelimitedCommand(String trimText){
        Pair<String, String> commandText;

        if (trimText.contains(" ")) {
            int indexOfSpace = trimText.indexOf(" ");
            commandText = new Pair<String, String>(trimText.substring(0, indexOfSpace), trimText.substring(indexOfSpace+1));
        }else commandText = new Pair<String, String>(trimText,"");
        return commandText;
    }
    private boolean isCommand(String text){
        return text.startsWith(PREFIX_FOR_COMMAND);
    }
    private boolean isCommandForMe(String command){
        if(command.contains(DELIMITER_COMMAND_BOTNAME)){
            String botNameForEquals = command.substring(command.indexOf(DELIMITER_COMMAND_BOTNAME)+1);
            return  botNameForEquals.equals(botName);
        }
        return true;//!!!
    }
    private String cutCommandFromText(String text){
        return  text.contains(DELIMITER_COMMAND_BOTNAME)?
                text.substring(1, text.indexOf(DELIMITER_COMMAND_BOTNAME)) :
                text.substring(1);
    }
    private Command getCommandFromText (String text){
        String uppercaseCommand = text.toUpperCase();
        Command command = Command.NONE;
        try {
            command = Command.valueOf(uppercaseCommand);
        }catch (IllegalArgumentException e){
            log.info("Can't parse command: " + text);
        }
        return command;
    }
}
