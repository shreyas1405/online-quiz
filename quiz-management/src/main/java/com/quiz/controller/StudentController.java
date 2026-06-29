package com.quiz.controller;

import com.quiz.model.*;
import com.quiz.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {
    private final QuizService quizService;
    private final AttemptService attemptService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        List<Quiz> quizzes = quizService.getAllActiveQuizzes();
        List<QuizAttempt> recentAttempts = attemptService.getUserAttempts(user.getId())
            .stream().limit(5).toList();
        model.addAttribute("user", user);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("recentAttempts", recentAttempts);
        return "student/dashboard";
    }

    @GetMapping("/quiz/{quizId}/start")
    public String quizStartPage(@PathVariable Long quizId, Authentication auth, Model model) {
        Quiz quiz = quizService.getQuizById(quizId);
        User user = userService.findByEmail(auth.getName());
        List<QuizAttempt> previousAttempts = attemptService.getUserAttempts(user.getId())
            .stream().filter(a -> a.getQuiz().getId().equals(quizId)).toList();
        model.addAttribute("quiz", quiz);
        model.addAttribute("user", user);
        model.addAttribute("previousAttempts", previousAttempts);
        return "student/quiz-start";
    }

    @PostMapping("/quiz/{quizId}/start")
    public String startQuiz(@PathVariable Long quizId, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        QuizAttempt attempt = attemptService.startAttempt(user, quizId);
        return "redirect:/student/quiz/attempt/" + attempt.getId() + "/take";
    }

    @GetMapping("/quiz/attempt/{attemptId}/take")
    public String takeQuiz(@PathVariable Long attemptId, Authentication auth, Model model) {
        QuizAttempt attempt = attemptService.getAttemptById(attemptId);
        User user = userService.findByEmail(auth.getName());
        if (!attempt.getUser().getId().equals(user.getId())) return "redirect:/student/dashboard";
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            return "redirect:/student/quiz/attempt/" + attemptId + "/result";
        }
        List<Question> questions = quizService.getQuestionsForQuiz(attempt.getQuiz().getId());
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", attempt.getQuiz());
        model.addAttribute("questions", questions);
        model.addAttribute("durationSeconds", attempt.getQuiz().getDurationMinutes() * 60);
        return "student/quiz-take";
    }

    @PostMapping("/quiz/attempt/{attemptId}/submit")
    public String submitQuiz(@PathVariable Long attemptId,
                              Authentication auth,
                              HttpServletRequest request,
                              @RequestParam(required = false) String timedOut) {
        QuizAttempt attempt = attemptService.getAttemptById(attemptId);
        User user = userService.findByEmail(auth.getName());
        if (!attempt.getUser().getId().equals(user.getId())) return "redirect:/student/dashboard";
        Map<String, String[]> params = request.getParameterMap();
        boolean isTimedOut = "true".equals(timedOut);
        attemptService.submitAttempt(attemptId, params, isTimedOut);
        return "redirect:/student/quiz/attempt/" + attemptId + "/result";
    }

    @GetMapping("/quiz/attempt/{attemptId}/result")
    public String viewResult(@PathVariable Long attemptId, Authentication auth, Model model) {
        QuizAttempt attempt = attemptService.getAttemptById(attemptId);
        User user = userService.findByEmail(auth.getName());
        if (!attempt.getUser().getId().equals(user.getId())) return "redirect:/student/dashboard";
        model.addAttribute("attempt", attempt);
        model.addAttribute("quiz", attempt.getQuiz());
        model.addAttribute("userAnswers", attempt.getUserAnswers());
        return "student/quiz-result";
    }

    @GetMapping("/history")
    public String history(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        List<QuizAttempt> attempts = attemptService.getUserAttempts(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("attempts", attempts);
        return "student/history";
    }
}
