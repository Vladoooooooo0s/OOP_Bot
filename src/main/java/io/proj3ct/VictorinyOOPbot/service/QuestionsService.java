package io.proj3ct.VictorinyOOPbot.service;

import io.proj3ct.VictorinyOOPbot.model.Questions;
import io.proj3ct.VictorinyOOPbot.model.QuestionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionsService {

    @Autowired
    private QuestionsRepository questionsRepository;

    public Questions getRandomQuestionByCategoryId(Long categoryId, Set<Long> askedQuestions) {
        List<Questions> questions = questionsRepository.findByCategoryId(categoryId);

        if (questions.isEmpty()) {
            return null;
        }

        System.out.println("Total questions in category: " + questions.size());

        List<Questions> availableQuestions = questions.stream()
                .filter(question -> !askedQuestions.contains(question.getId()))
                .collect(Collectors.toList());

        System.out.println("Available questions: " + availableQuestions.size());
        for (Questions q : availableQuestions) {
            System.out.println("Available question ID: " + q.getId());
        }

        if (availableQuestions.isEmpty()) {
            return null;
        }

        int randomIndex = new Random().nextInt(availableQuestions.size());
        return availableQuestions.get(randomIndex);
    }

    public List<Questions> getQuestionsByCategoryId(Long categoryId) {
        return questionsRepository.findByCategoryId(categoryId);
    }
}