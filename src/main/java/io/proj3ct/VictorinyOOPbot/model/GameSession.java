package io.proj3ct.VictorinyOOPbot.model;

import java.util.HashSet;
import java.util.Set;

public class GameSession {
    private Long currentCategoryId;
    private int correctAnswersCount = 0;
    private final Set<Long> askedQuestions = new HashSet<>();
    private QuestionFromApi currentApiQuestion;

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

    public QuestionFromApi getCurrentApiQuestion() {
        return currentApiQuestion;
    }

    public void setCurrentApiQuestion(QuestionFromApi currentApiQuestion) {
        this.currentApiQuestion = currentApiQuestion;
    }

    private final Set<String> askedApiQuestions = new HashSet<>();

    public Set<String> getAskedApiQuestions() {
        return askedApiQuestions;
    }

    public void addAskedApiQuestion(String questionId) {
        askedApiQuestions.add(questionId);
    }

    private Long currentQuestionId;

    public Long getCurrentQuestionId() {
        return currentQuestionId;
    }

    public void setCurrentQuestionId(Long currentQuestionId) {
        this.currentQuestionId = currentQuestionId;
    }
}
