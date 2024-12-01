package io.proj3ct.VictorinyOOPbot.model;

import java.util.HashSet;
import java.util.Set;

public class GameSession {
    private Long currentCategoryId;
    private int correctAnswersCount = 0;
    private final Set<Long> askedQuestions = new HashSet<>();

    public Long getCurrentCategoryId() {
        return currentCategoryId;
    }

    public void setCurrentCategoryId(Long currentCategoryId) {
        this.currentCategoryId = currentCategoryId;
    }

    public int getCorrectAnswersCount() {
        return correctAnswersCount;
    }

    public void incrementCorrectAnswers() {
        correctAnswersCount++;
    }

    public Set<Long> getAskedQuestions() {
        return askedQuestions;
    }

    public void addAskedQuestion(Long questionId) {
        askedQuestions.add(questionId);
    }
}
