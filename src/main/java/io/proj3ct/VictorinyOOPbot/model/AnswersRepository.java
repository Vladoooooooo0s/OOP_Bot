package io.proj3ct.VictorinyOOPbot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswersRepository extends JpaRepository<Answers, Long> {
    List<Answers> findByQuestionId(Long questionId);
}