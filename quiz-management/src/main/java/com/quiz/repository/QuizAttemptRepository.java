package com.quiz.repository;

import com.quiz.model.AttemptStatus;
import com.quiz.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);
    List<QuizAttempt> findByQuizId(Long quizId);
    Optional<QuizAttempt> findByUserIdAndQuizIdAndStatus(Long userId, Long quizId, AttemptStatus status);

    @Query("SELECT COUNT(a) FROM QuizAttempt a WHERE a.startedAt >= :since")
    long countAttemptsSince(@Param("since") LocalDateTime since);

    long countByStatus(AttemptStatus status);
}
