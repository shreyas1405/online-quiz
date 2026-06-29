package com.quiz.config;

import com.quiz.model.*;
import com.quiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
            .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
            .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_STUDENT")));

        if (!userRepository.existsByEmail("admin@quiz.com")) {
            User admin = new User();
            admin.setFullName("Admin User");
            admin.setEmail("admin@quiz.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.getRoles().add(adminRole);
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("student@quiz.com")) {
            User student = new User();
            student.setFullName("Sample Student");
            student.setEmail("student@quiz.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.getRoles().add(studentRole);
            userRepository.save(student);
        }

        if (quizRepository.count() == 0) {
            User admin = userRepository.findByEmail("admin@quiz.com").orElseThrow();

            Quiz quiz = new Quiz();
            quiz.setTitle("Java Fundamentals");
            quiz.setDescription("Test your knowledge of core Java programming concepts including OOP, data types, and control flow.");
            quiz.setTopic("Programming");
            quiz.setDurationMinutes(15);
            quiz.setPassingScore(60);
            quiz.setCreatedBy(admin);
            quiz = quizRepository.save(quiz);

            Question q1 = new Question();
            q1.setQuiz(quiz);
            q1.setQuestionText("Which keyword is used to define a class in Java?");
            q1.setQuestionType(QuestionType.SINGLE_CHOICE);
            q1.setOrderIndex(0); q1.setMarks(2);
            q1 = questionRepository.save(q1);
            optionRepository.save(new Option(null, q1, "class", true));
            optionRepository.save(new Option(null, q1, "define", false));
            optionRepository.save(new Option(null, q1, "struct", false));
            optionRepository.save(new Option(null, q1, "object", false));

            Question q2 = new Question();
            q2.setQuiz(quiz);
            q2.setQuestionText("Which of the following are valid Java access modifiers? (Select all that apply)");
            q2.setQuestionType(QuestionType.MULTIPLE_CHOICE);
            q2.setOrderIndex(1); q2.setMarks(3);
            q2 = questionRepository.save(q2);
            optionRepository.save(new Option(null, q2, "public", true));
            optionRepository.save(new Option(null, q2, "private", true));
            optionRepository.save(new Option(null, q2, "protected", true));
            optionRepository.save(new Option(null, q2, "hidden", false));

            Question q3 = new Question();
            q3.setQuiz(quiz);
            q3.setQuestionText("What is the default value of an int variable in Java?");
            q3.setQuestionType(QuestionType.SHORT_ANSWER);
            q3.setOrderIndex(2); q3.setMarks(2);
            q3.setCorrectAnswer("0");
            questionRepository.save(q3);

            Question q4 = new Question();
            q4.setQuiz(quiz);
            q4.setQuestionText("Which data structure does ArrayList implement internally?");
            q4.setQuestionType(QuestionType.SINGLE_CHOICE);
            q4.setOrderIndex(3); q4.setMarks(2);
            q4 = questionRepository.save(q4);
            optionRepository.save(new Option(null, q4, "LinkedList", false));
            optionRepository.save(new Option(null, q4, "Dynamic Array", true));
            optionRepository.save(new Option(null, q4, "Stack", false));
            optionRepository.save(new Option(null, q4, "Queue", false));

            Question q5 = new Question();
            q5.setQuiz(quiz);
            q5.setQuestionText("Explain the concept of inheritance in Java. Provide a real-world example.");
            q5.setQuestionType(QuestionType.LONG_ANSWER);
            q5.setOrderIndex(4); q5.setMarks(5);
            questionRepository.save(q5);
        }
    }
}
