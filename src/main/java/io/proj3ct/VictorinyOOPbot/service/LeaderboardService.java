package io.proj3ct.VictorinyOOPbot.service;

import io.proj3ct.VictorinyOOPbot.model.GameSession;
import io.proj3ct.VictorinyOOPbot.model.User;
import io.proj3ct.VictorinyOOPbot.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    @Autowired
    private UserRepository userRepository;

    // Метод для обновления баллов пользователя
    public void updateUserScore(long chatId) {
        // Ищем пользователя по chatId
        User user = userRepository.findByChatId(chatId).orElse(null);

        if (user != null) {
            // Увеличиваем текущий счёт на 1
            user.setCurrentScore(user.getCurrentScore() + 1);

            // Обновляем максимальный счёт, если текущий счёт больше
            if (user.getCurrentScore() > user.getMaxScore()) {
                user.setMaxScore(user.getCurrentScore());
            }

            // Сохраняем изменения
            userRepository.save(user);
        }
    }


    // Метод для получения списка лидеров
    public List<User> getLeaderboard() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);

        return users.stream()
                .sorted((u1, u2) -> Integer.compare(u2.getMaxScore(), u1.getMaxScore()))
                .collect(Collectors.toList());
    }
}
