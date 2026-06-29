package com.quiz.service;

import com.quiz.model.*;
import com.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    public List<Quiz> getAllActiveQuizzes() {
        return quizRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAllByOrderByCreatedAtDesc();
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    @Transactional
    public Quiz createQuiz(Quiz quiz, User createdBy) {
        quiz.setCreatedBy(createdBy);
        return quizRepository.save(quiz);
    }

    @Transactional
    public Quiz updateQuiz(Long id, Quiz updated) {
        Quiz existing = getQuizById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setTopic(updated.getTopic());
        existing.setDurationMinutes(updated.getDurationMinutes());
        existing.setPassingScore(updated.getPassingScore());
        existing.setActive(updated.isActive());
        return quizRepository.save(existing);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }

    @Transactional
    public Question addQuestion(Long quizId, Question question,
                                Map<String, String> optionTexts,
                                Map<String, Boolean> optionCorrects,
                                String imagePath) {
        Quiz quiz = getQuizById(quizId);
        question.setQuiz(quiz);
        question.setImagePath(imagePath);
        int nextIndex = questionRepository.countByQuizId(quizId);
        question.setOrderIndex(nextIndex);
        Question saved = questionRepository.save(question);

        if (question.getQuestionType() == QuestionType.SINGLE_CHOICE
                || question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            for (Map.Entry<String, String> entry : optionTexts.entrySet()) {
                String key = entry.getKey();
                String text = entry.getValue();
                if (text != null && !text.isBlank()) {
                    boolean isCorrect = optionCorrects.getOrDefault(key, false);
                    Option opt = new Option();
                    opt.setQuestion(saved);
                    opt.setOptionText(text);
                    opt.setCorrect(isCorrect);
                    optionRepository.save(opt);
                }
            }
        }
        return saved;
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        questionRepository.deleteById(questionId);
    }

    public List<Question> getQuestionsForQuiz(Long quizId) {
        return questionRepository.findByQuizIdOrderByOrderIndexAsc(quizId);
    }
}
