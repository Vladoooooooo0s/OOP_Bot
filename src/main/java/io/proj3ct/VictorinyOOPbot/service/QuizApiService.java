package io.proj3ct.VictorinyOOPbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.proj3ct.VictorinyOOPbot.model.QuestionFromApi;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class QuizApiService {
    private static final String API_KEY = "mQlGgUEpyxsetSKyL9uUoMf8nJcQQq1feM8cnUhG";
    private static final String BASE_URL = "https://quizapi.io/api/v1";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public QuizApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        // Включаем форматирование (pretty-print)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Получает вопросы из API и сохраняет их в файл с красивым форматированием.
     */
    public List<QuestionFromApi> fetchQuizQuestions(int quizId) throws Exception {
        String url = BASE_URL + "/questions?apiKey=" + API_KEY + "&quiz_id=" + quizId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Десериализуем JSON в список объектов QuestionFromApi
        List<QuestionFromApi> questions = objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, QuestionFromApi.class));

        // Записываем вопросы в файл с красивым форматированием
        try (FileWriter file = new FileWriter("questions.json")) {
            objectMapper.writeValue(file, questions);  // Записываем JSON в файл
        } catch (IOException e) {
            e.printStackTrace();
        }

        return questions; // Возвращаем список вопросов
    }


    /**
     * Проверяет правильность ответа.
     *
     * @param userAnswerKey Ключ ответа (например, "answer_a", "answer_b").
     * @param correctAnswers Map с правильными ответами из API.
     * @return true, если ответ верный, иначе false.
     */
    public boolean checkApiAnswer(String userAnswerKey, Map<String, String> correctAnswers) {
        // Строим правильный ключ для поиска правильного ответа
        String correctKey = userAnswerKey + "_correct";
        String isCorrect = correctAnswers.get(correctKey);

        // Если ответ правильный, возвращаем true
        return "true".equalsIgnoreCase(isCorrect); // Убедимся, что строка "true" корректно сравнивается
    }
}
