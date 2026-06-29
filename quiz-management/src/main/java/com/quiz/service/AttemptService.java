package com.quiz.service;

import com.quiz.model.*;
import com.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttemptService {
    private final QuizAttemptRepository attemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuizRepository quizRepository;

    @Transactional
    public QuizAttempt startAttempt(User user, Long quizId) {
        Optional<QuizAttempt> existing = attemptRepository
            .findByUserIdAndQuizIdAndStatus(user.getId(), quizId, AttemptStatus.IN_PROGRESS);
        if (existing.isPresent()) return existing.get();

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        return attemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttempt submitAttempt(Long attemptId, Map<String, String[]> formParams, boolean timedOut) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Attempt not found"));
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) return attempt;

        Quiz quiz = attempt.getQuiz();
        List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndexAsc(quiz.getId());

        int totalScore = 0;
        int totalMarks = 0;

        for (Question question : questions) {
            totalMarks += question.getMarks();
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setAttempt(attempt);
            userAnswer.setQuestion(question);

            String paramKey = "q_" + question.getId();
            String[] values = formParams.get(paramKey);

            switch (question.getQuestionType()) {
                case SINGLE_CHOICE -> {
                    if (values != null && values.length > 0 && !values[0].isBlank()) {
                        Long optionId = Long.parseLong(values[0]);
                        Option selected = optionRepository.findById(optionId).orElse(null);
                        userAnswer.setSelectedOption(selected);
                        boolean correct = selected != null && selected.isCorrect();
                        userAnswer.setIsCorrect(correct);
                        userAnswer.setMarksObtained(correct ? question.getMarks() : 0);
                        if (correct) totalScore += question.getMarks();
                    } else {
                        userAnswer.setIsCorrect(false);
                        userAnswer.setMarksObtained(0);
                    }
                }
                case MULTIPLE_CHOICE -> {
                    if (values != null && values.length > 0) {
                        Set<Option> selectedOpts = new HashSet<>();
                        for (String v : values) {
                            if (!v.isBlank()) {
                                optionRepository.findById(Long.parseLong(v)).ifPresent(selectedOpts::add);
                            }
                        }
                        userAnswer.setSelectedOptions(selectedOpts);
                        List<Option> correctOptions = optionRepository.findByQuestionIdAndCorrectTrue(question.getId());
                        Set<Long> correctIds = new HashSet<>();
                        for (Option o : correctOptions) correctIds.add(o.getId());
                        Set<Long> selectedIds = new HashSet<>();
                        for (Option o : selectedOpts) selectedIds.add(o.getId());
                        boolean correct = correctIds.equals(selectedIds);
                        userAnswer.setIsCorrect(correct);
                        userAnswer.setMarksObtained(correct ? question.getMarks() : 0);
                        if (correct) totalScore += question.getMarks();
                    } else {
                        userAnswer.setIsCorrect(false);
                        userAnswer.setMarksObtained(0);
                    }
                }
                case SHORT_ANSWER -> {
                    if (values != null && values.length > 0 && !values[0].isBlank()) {
                        String textAns = values[0].trim();
                        userAnswer.setTextAnswer(textAns);
                        String expected = question.getCorrectAnswer();
                        boolean correct = expected != null && textAns.equalsIgnoreCase(expected.trim());
                        userAnswer.setIsCorrect(correct);
                        userAnswer.setMarksObtained(correct ? question.getMarks() : 0);
                        if (correct) totalScore += question.getMarks();
                    } else {
                        userAnswer.setIsCorrect(false);
                        userAnswer.setMarksObtained(0);
                    }
                }
                case LONG_ANSWER -> {
                    if (values != null && values.length > 0 && !values[0].isBlank()) {
                        userAnswer.setTextAnswer(values[0].trim());
                    }
                    userAnswer.setIsCorrect(null);
                    userAnswer.setMarksObtained(0);
                }
            }
            userAnswerRepository.save(userAnswer);
        }

        attempt.setScore(totalScore);
        attempt.setTotalMarks(totalMarks);
        attempt.setPercentage(totalMarks > 0 ? (totalScore * 100) / totalMarks : 0);
        attempt.setPassed(attempt.getPercentage() >= quiz.getPassingScore());
        attempt.setStatus(timedOut ? AttemptStatus.TIMED_OUT : AttemptStatus.COMPLETED);
        attempt.setCompletedAt(LocalDateTime.now());
        return attemptRepository.save(attempt);
    }

    public QuizAttempt getAttemptById(Long id) {
        return attemptRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Attempt not found"));
    }

    public List<QuizAttempt> getUserAttempts(Long userId) {
        return attemptRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    public long countTodayAttempts() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return attemptRepository.countAttemptsSince(todayStart);
    }

    @Transactional
    public void gradeManualAnswer(Long userAnswerId, int score, String feedback) {
        UserAnswer answer = userAnswerRepository.findById(userAnswerId)
            .orElseThrow(() -> new RuntimeException("Answer not found"));
        answer.setManualScore(score);
        answer.setAdminFeedback(feedback);
        userAnswerRepository.save(answer);

        QuizAttempt attempt = answer.getAttempt();
        int totalScore = 0;
        for (UserAnswer ua : attempt.getUserAnswers()) {
            if (ua.getQuestion().getQuestionType() == QuestionType.LONG_ANSWER) {
                totalScore += (ua.getManualScore() != null ? ua.getManualScore() : 0);
            } else {
                totalScore += (ua.getMarksObtained() != null ? ua.getMarksObtained() : 0);
            }
        }
        attempt.setScore(totalScore);
        attempt.setPercentage(attempt.getTotalMarks() > 0 ? (totalScore * 100) / attempt.getTotalMarks() : 0);
        attempt.setPassed(attempt.getPercentage() >= attempt.getQuiz().getPassingScore());
        attemptRepository.save(attempt);
    }
}
