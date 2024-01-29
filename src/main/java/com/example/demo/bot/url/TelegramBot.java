package com.example.demo.bot.url;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class TelegramBot extends TelegramLongPollingBot {
    public String answer;
    int rating;
    private final static String PATH_TO_FILE_WITH_URL = "src/main/resources/url.txt";
    private final static String PATH_TO_FILE_WITH_RATING = "src/main/resources/rating.txt";

    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        Long id = update.getMessage().getChatId();
        System.out.println(id);
        if (message.equals("Go") || message.equals("/start")) {
            try {
                ifAnsverGo(update.getMessage().getChatId(), getRandomURL());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (message.equalsIgnoreCase("да") || message.equalsIgnoreCase("нет")) {
            try {
                ifAnsverYesOrNo(id, message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            SendMessage sendMessage1 = sendMessages(id, "Нет такой команды, попробуй Go");
            try {
                execute(sendMessage1);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    private String getRandomURL() {
        try {
            // Читаем все строки из файла
            List<String> lines = Files.readAllLines(Paths.get(PATH_TO_FILE_WITH_URL));

            // Выбираем случайную строку
            if (!lines.isEmpty()) {
                Random random = new Random();
                int randomIndex = random.nextInt(lines.size());
                System.out.println("Index=" + randomIndex);
                answer = randomIndex >= 50 ? "да" : "нет";
                System.out.println("answ=" + answer);
                return lines.get(randomIndex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void ifAnsverYesOrNo(Long id, String message) throws TelegramApiException {
        ReplyKeyboardMarkup keyboardMarkup1 = new ReplyKeyboardMarkup();
        keyboardMarkup1.setSelective(true);
        keyboardMarkup1.setResizeKeyboard(true);
        keyboardMarkup1.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("Go"));
        keyboardRows.add(row);
        keyboardMarkup1.setKeyboard(keyboardRows);

        if (message.equals(answer)) {
            try {
                Rating(id, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            SendMessage sendMessage = sendMessages(id, "Ура ура! Ты прав. Твой счёт: " + rating);
            sendMessage.setReplyMarkup(keyboardMarkup1);
            execute(sendMessage);
        } else {
            SendMessage sendMessage = sendMessages(id, "К сожалению нет... Компьютер тебя обыграл");
            sendMessage.setReplyMarkup(keyboardMarkup1);
            execute(sendMessage);
        }
    }

    public void Rating(Long id, int points) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(PATH_TO_FILE_WITH_RATING));
        Map<Long, Integer> ratingMap = new HashMap<>();

        // Заполнение карты из файла
        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String currentId = parts[0].trim();
                int currentRating = Integer.parseInt(parts[1].trim());
                ratingMap.put(Long.valueOf(currentId), currentRating);
            }
        }

        // Обновление рейтинга
        if (ratingMap.containsKey(id)) {
            int currentPoints = ratingMap.get(id);
            ratingMap.put(id, currentPoints + points);

        } else {
            // Если id отсутствует, добавляем новую запись с рейтингом 1
            ratingMap.put(id, 1);
        }
        rating = ratingMap.get(id);

        // Сохранение нового рейтинга в файл
        List<String> updatedLines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : ratingMap.entrySet()) {
            String line = entry.getKey() + ":" + entry.getValue();
            updatedLines.add(line);
        }
        Files.write(Paths.get(PATH_TO_FILE_WITH_RATING), updatedLines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void ifAnsverGo(Long chatId, String imageUrl) throws TelegramApiException {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(imageUrl));
        execute(sendPhoto);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton("да"));
        row.add(new KeyboardButton("нет"));
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);

        SendMessage sendMessage = sendMessages(chatId, "Это нарисовала нейросеть?");
        sendMessage.setReplyMarkup(keyboardMarkup);
        execute(sendMessage);
    }

    public static SendMessage sendMessages(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {
        return "noypalivoGame_bot";
    }

    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public String getBotToken() {
        return "Api_Key";
    }
}

