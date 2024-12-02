package io.proj3ct.VictorinyOOPbot.service;

import io.proj3ct.VictorinyOOPbot.model.Answers;
import io.proj3ct.VictorinyOOPbot.model.AnswersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class AnswersService {

    @Autowired
    private AnswersRepository answersRepository;

    public Optional<Answers> getAnswerById(Long id) {
        return answersRepository.findById(id);
    }

    public boolean isAnswerCorrect(Long id) {
        Optional<Answers> answer = getAnswerById(id);
        if (answer.isEmpty()) {
            log.info("Answer not found for ID: " + id);
        }
        return answer.map(Answers::isCorrect).orElse(false);
    }

}