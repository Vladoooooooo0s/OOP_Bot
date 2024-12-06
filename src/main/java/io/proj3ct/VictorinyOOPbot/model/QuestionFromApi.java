package io.proj3ct.VictorinyOOPbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionFromApi {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("question")
    private String question;

    @JsonProperty("answers")
    private Map<String, String> answers;

    @JsonProperty("correct_answers")
    private Map<String, String> correctAnswers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }

    public Map<String, String> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Map<String, String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    @Override
    public String toString() {
        return "QuestionFromApi{" +
                "question='" + question + '\'' +
                ", answers=" + answers +
                ", correctAnswers=" + correctAnswers +
                '}';
    }
}
