package com.quiz.repository;

import com.quiz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByActiveTrueOrderByCreatedAtDesc();
    List<Quiz> findAllByOrderByCreatedAtDesc();
}
