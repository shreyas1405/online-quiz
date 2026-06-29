package com.quiz.controller;

import com.quiz.model.*;
import com.quiz.repository.QuizAttemptRepository;
import com.quiz.repository.UserAnswerRepository;
import com.quiz.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final QuizService quizService;
    private final UserService userService;
    private final AttemptService attemptService;
    private final QuizAttemptRepository attemptRepository;
    private final UserAnswerRepository userAnswerRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalQuizzes", quizService.getAllQuizzes().size());
        model.addAttribute("totalStudents", userService.countAllStudents());
        model.addAttribute("todayAttempts", attemptService.countTodayAttempts());
        model.addAttribute("recentAttempts", attemptRepository.findAll().stream()
            .sorted(Comparator.comparing(QuizAttempt::getStartedAt).reversed())
            .limit(10).toList());
        return "admin/dashboard";
    }

    @GetMapping("/quizzes")
    public String quizList(Model model) {
        model.addAttribute("quizzes", quizService.getAllQuizzes());
        return "admin/quiz-list";
    }

    @GetMapping("/quizzes/create")
    public String createQuizForm(Model model) {
        model.addAttribute("quiz", new Quiz());
        model.addAttribute("action", "create");
        return "admin/quiz-form";
    }

    @PostMapping("/quizzes/create")
    public String createQuiz(@ModelAttribute Quiz quiz, Authentication auth, RedirectAttributes ra) {
        User admin = userService.findByEmail(auth.getName());
        Quiz saved = quizService.createQuiz(quiz, admin);
        ra.addFlashAttribute("success", "Quiz created! Now add questions.");
        return "redirect:/admin/quizzes/" + saved.getId() + "/questions";
    }

    @GetMapping("/quizzes/{id}/edit")
    public String editQuizForm(@PathVariable Long id, Model model) {
        model.addAttribute("quiz", quizService.getQuizById(id));
        model.addAttribute("action", "edit");
        return "admin/quiz-form";
    }

    @PostMapping("/quizzes/{id}/edit")
    public String updateQuiz(@PathVariable Long id, @ModelAttribute Quiz quiz, RedirectAttributes ra) {
        quizService.updateQuiz(id, quiz);
        ra.addFlashAttribute("success", "Quiz updated successfully.");
        return "redirect:/admin/quizzes";
    }

    @PostMapping("/quizzes/{id}/delete")
    public String deleteQuiz(@PathVariable Long id, RedirectAttributes ra) {
        quizService.deleteQuiz(id);
        ra.addFlashAttribute("success", "Quiz deleted.");
        return "redirect:/admin/quizzes";
    }

    @GetMapping("/quizzes/{id}/questions")
    public String questionManager(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", quizService.getQuestionsForQuiz(id));
        model.addAttribute("questionTypes", QuestionType.values());
        model.addAttribute("newQuestion", new Question());
        return "admin/question-manager";
    }

    @PostMapping("/quizzes/{id}/questions/add")
    public String addQuestion(@PathVariable Long id,
                               @ModelAttribute Question question,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               @RequestParam Map<String, String> allParams,
                               RedirectAttributes ra) {
        Map<String, String> optionTexts = new LinkedHashMap<>();
        Map<String, Boolean> optionCorrects = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("optionText_")) {
                optionTexts.put(entry.getKey(), entry.getValue());
            }
            if (entry.getKey().startsWith("optionCorrect_")) {
                optionCorrects.put(entry.getKey().replace("optionCorrect_", "optionText_"), true);
            }
        }

        String imagePath = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String uploadsDirName = "uploads";
                java.io.File uploadsDir = new java.io.File(uploadsDirName);
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdirs();
                }
                String orgName = imageFile.getOriginalFilename();
                String cleanName = orgName != null ? orgName.replaceAll("[^a-zA-Z0-9.-]", "_") : "image";
                String fileName = java.util.UUID.randomUUID().toString() + "_" + cleanName;
                java.io.File destFile = new java.io.File(uploadsDir, fileName);
                imageFile.transferTo(destFile);
                imagePath = "/uploads/" + fileName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        quizService.addQuestion(id, question, optionTexts, optionCorrects, imagePath);
        ra.addFlashAttribute("success", "Question added successfully.");
        return "redirect:/admin/quizzes/" + id + "/questions";
    }

    @PostMapping("/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long questionId,
                                  @RequestParam Long quizId,
                                  RedirectAttributes ra) {
        quizService.deleteQuestion(questionId);
        ra.addFlashAttribute("success", "Question deleted.");
        return "redirect:/admin/quizzes/" + quizId + "/questions";
    }

    @GetMapping("/users")
    public String userList(Model model) {
        model.addAttribute("students", userService.getAllStudents());
        return "admin/user-list";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleUserEnabled(id);
        ra.addFlashAttribute("success", "User status updated.");
        return "redirect:/admin/users";
    }

    @GetMapping("/attempts/{attemptId}/review")
    public String reviewAttempt(@PathVariable Long attemptId, Model model) {
        QuizAttempt attempt = attemptService.getAttemptById(attemptId);
        List<UserAnswer> longAnswers = attempt.getUserAnswers().stream()
            .filter(ua -> ua.getQuestion().getQuestionType() == QuestionType.LONG_ANSWER)
            .toList();
        model.addAttribute("attempt", attempt);
        model.addAttribute("longAnswers", longAnswers);
        return "admin/review-attempt";
    }

    @PostMapping("/answers/{answerId}/grade")
    public String gradeAnswer(@PathVariable Long answerId,
                               @RequestParam int score,
                               @RequestParam(required = false) String feedback,
                               @RequestParam Long attemptId,
                               RedirectAttributes ra) {
        attemptService.gradeManualAnswer(answerId, score, feedback);
        ra.addFlashAttribute("success", "Answer graded successfully.");
        return "redirect:/admin/attempts/" + attemptId + "/review";
    }
}
