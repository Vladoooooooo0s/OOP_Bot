package io.proj3ct.VictorinyOOPbot.service;

import io.proj3ct.VictorinyOOPbot.config.BotConfig;
import io.proj3ct.VictorinyOOPbot.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private QuestionsService questionsService;

    @Autowired
    private AnswersService answersService;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private QuizApiService quizApiService;

    final BotConfig config;

    private final Map<Long, GameSession> userSessions = new ConcurrentHashMap<>();

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


    private void finishGame(long chatId) {
        GameSession session = userSessions.get(chatId);
        if (session != null) {
            sendMessage(chatId, "Игра завершена. Ты ответил правильно на " + session.getCorrectAnswersCount() + " вопрос(ов).", false);
            userSessions.remove(chatId);
        }
        sendCategoryOptions(chatId);
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            GameSession session = userSessions.computeIfAbsent(chatId, id -> new GameSession());

            if (callbackData.startsWith("CATEGORY_")) {
                long categoryId = Long.parseLong(callbackData.split("_")[1]);
                session.setCurrentCategoryId(categoryId);


                if (categoryId == -1L) { // Категория из API
                    sendQuestionFromApi(chatId);
                } else {
                    Category selectedCategory = categoryService.getCategoryById(categoryId);
                    sendMessage(chatId, "Ты выбрал категорию: " + selectedCategory.getName(), true);
                    sendQuestion(chatId, categoryId);
                }

            } else if (callbackData.startsWith("ANSWER_")) {
                long answerId = Long.parseLong(callbackData.split("_")[1]);
                checkAnswer(chatId, answerId);

            } else if (callbackData.equals("FINISH_GAME")) {
                finishGame(chatId);

            } else if (callbackData.startsWith("API_ANSWER_")) {
                String userAnswerKey = callbackData.split("_")[2];

                // Проверяем правильность ответа
                boolean isCorrect = quizApiService.checkApiAnswer(userAnswerKey, session.getCurrentApiQuestion().getCorrectAnswers());

                if (isCorrect) {
                    sendMessage(chatId, "Верно! Молодец!", false);
                } else {
                    sendMessage(chatId, "Неправильно. Попробуй ещё раз!", false);
                }
            }

        } else if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if ("Завершить игру".equals(messageText)) {
                finishGame(chatId);
            } else {
                switch (messageText) {
                    case "/start":
                        registerUser(update.getMessage());
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/categories":
                        sendCategoryOptions(chatId);
                        break;
                    default:
                        sendMessage(chatId, "Invalid request", false);
                }
            }
        }
    }


    private void registerUser(Message msg){
        if(userRepository.findById(msg.getChatId()).isEmpty()){
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
        }
    }


    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет, " + name + "! 👋\n" +
                "Чтобы начать, выбери категорию вопросов, которая тебе интересна. " +
                "После этого я буду задавать вопросы, а ты сможешь отвечать на них! 🚀\n" +
                "Удачи!";
        sendMessage(chatId, answer, false);
        sendCategoryOptions(chatId);

        log.info("Replied to user " + name);
    }


    private void sendMessage(long chatId, String textToSend, boolean showFinishButton) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        List<KeyboardRow> keyboard = new ArrayList<>();
        if (showFinishButton) {
            KeyboardRow row = new KeyboardRow();
            row.add("Завершить игру");
            keyboard.add(row);
        }

        if (!keyboard.isEmpty()) {
            ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
            replyMarkup.setKeyboard(keyboard);
            replyMarkup.setResizeKeyboard(true);
            replyMarkup.setOneTimeKeyboard(false);
            message.setReplyMarkup(replyMarkup);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occured " + e.getMessage());
        }
    }


    private void sendCategoryOptions(long chatId) {
        List<Category> categories = categoryService.getAllCategories();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выбери категорию:");

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Category category : categories) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(category.getName());
            button.setCallbackData("CATEGORY_" + category.getId());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            buttons.add(row);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(buttons);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void sendQuestion(long chatId, long categoryId) {
        GameSession session = userSessions.get(chatId);

        if (session == null) {
            sendMessage(chatId, "Ошибка! Сессия не найдена.", false);
            return;
        }

        List<Questions> questions = questionsRepository.findByCategoryId(categoryId);
        if (questions.isEmpty() || session.getAskedQuestions().size() == questions.size()) {
            sendMessage(chatId, "В этой категории больше нет вопросов.", false);
            finishGame(chatId);
            return;
        }

        Questions question = questionsService.getRandomQuestionByCategoryId(categoryId, session.getAskedQuestions());
        if (question == null) {
            sendMessage(chatId, "В этой категории больше нет вопросов.", false);
            finishGame(chatId);
            return;
        }

        session.addAskedQuestion(question.getId());

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(question.getText());

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Answers answer : question.getAnswers()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(answer.getText());
            button.setCallbackData("ANSWER_" + answer.getId());

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            buttons.add(row);
        }

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(buttons);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void checkAnswer(long chatId, long answerId) {
        GameSession session = userSessions.get(chatId);
        if (session == null) {
            sendMessage(chatId, "Ошибка! Сессия не найдена.", false);
            return;
        }

        long categoryId = session.getCurrentCategoryId();
        boolean isCorrect;

        // Если категория ID -1 (API-опрос), проверяем ответ через QuizApiService
        if (categoryId == -1) {
            QuestionFromApi questionFromApi = session.getCurrentApiQuestion(); // Получаем текущий вопрос из сессии

            if (questionFromApi == null) {
                sendMessage(chatId, "Ошибка! Вопрос не найден.", false);
                return;
            }

            // Проверяем правильность ответа через QuizApiService
            isCorrect = quizApiService.checkApiAnswer("answer_" + answerId, questionFromApi.getCorrectAnswers());
        } else {
            // Старая логика для других категорий
            isCorrect = answersService.isAnswerCorrect(answerId);
        }

        // Обработка результата
        if (isCorrect) {
            session.incrementCorrectAnswers();
            sendMessage(chatId, "Правильный ответ! 🎉", true);
        } else {
            sendMessage(chatId, "Неправильно. Попробуй ответить на следующий вопрос.", true);
        }

        // Отправка следующего вопроса
        if (categoryId == -1) {
            sendQuestionFromApi(chatId); // Отправка вопроса из API
        } else {
            sendQuestion(chatId, categoryId); // Отправка обычного вопроса
        }
    }



    private void sendQuestionFromApi(long chatId) {
        try {
            List<QuestionFromApi> questions = quizApiService.fetchQuizQuestions(15); // ID квиза

            if (questions.isEmpty()) {
                sendMessage(chatId, "Не удалось загрузить вопросы из API.", false);
                return;
            }

            // Выбираем случайный вопрос
            QuestionFromApi question = questions.get(new Random().nextInt(questions.size()));

            // Сохраняем вопрос в сессии
            GameSession session = userSessions.computeIfAbsent(chatId, id -> new GameSession());
            session.setCurrentApiQuestion(question);

            // Проверка на уже заданный вопрос
            if (session.getAskedApiQuestions().contains(String.valueOf(question.getId()))) {
                sendQuestionFromApi(chatId); // Рекурсивно вызываем метод, чтобы задать новый вопрос
                return;
            }

            session.addAskedApiQuestion(String.valueOf(question.getId()));

            // Отправляем вопрос пользователю
            StringBuilder messageText = new StringBuilder("Вопрос: " + question.getQuestion() + "\n\n");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            // Создаем кнопки для ответов
            for (Map.Entry<String, String> entry : question.getAnswers().entrySet()) {
                if (entry.getValue() != null) {
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(entry.getValue());
                    button.setCallbackData("API_ANSWER_" + entry.getKey()); // CallbackData для ответа

                    List<InlineKeyboardButton> row = new ArrayList<>();
                    row.add(button);
                    rows.add(row);
                }
            }

            markup.setKeyboard(rows);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(messageText.toString());
            message.setReplyMarkup(markup);

            execute(message);
        } catch (Exception e) {
            sendMessage(chatId, "Ошибка при загрузке вопроса: " + e.getMessage(), false);
        }
    }



}