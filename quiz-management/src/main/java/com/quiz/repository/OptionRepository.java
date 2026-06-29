package com.quiz.repository;

import com.quiz.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findByQuestionId(Long questionId);
    List<Option> findByQuestionIdAndCorrectTrue(Long questionId);
}
